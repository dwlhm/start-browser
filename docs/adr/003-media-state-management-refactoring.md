# ADR-003: Media State Management Refactoring

| Status | Accepted |
|--------|----------|
| Tanggal | 2026-01-13 |
| Deciders | dwlhm |
| Kategori | Core Architecture |
| Supersedes | - |

## Konteks

### Bug yang Ditemukan

Saat user klik notification dan kembali ke browser (tab yang sama dengan media playing):
1. Notification menjadi bisa di-dismiss (swipe away)
2. Padahal sebelumnya notification memiliki `ongoing = true` sebagai blocker
3. Tombol play/pause tidak sinkron dengan state di website

### Analisis Root Cause

#### Problem 1: Callback Instance Reset

```kotlin
// Di TabSessionManager.observeSelectedTab()
selectedTab.onEach { tabHandle ->
    // ...
    tabHandle.session.sessionCallback = object : BrowserSessionCallback {
        private var _mediaSession: BrowserMediaSession? = null  // ← RESET KE NULL!
        
        override fun onMediaStateChanged(state: BrowserMediaState) {
            if (_mediaSession == null) return  // ← EVENT DROPPED!
            // ...
        }
    }
}
```

**Masalah:** Setiap kali `selectedTab` flow emit, callback BARU dibuat dengan `_mediaSession = null`.

**Akibat:** Event `onMediaStateChanged` dan `onMediaMetadataChanged` di-drop karena guard clause.

#### Problem 2: Media Deactivation Race Condition

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    TIMELINE SAAT KEMBALI KE BROWSER                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  T0: User clicks notification                                               │
│      │                                                                      │
│  T1: │ Navigate to browser                                                  │
│      │                                                                      │
│  T2: │ BrowserShellRoute re-renders                                         │
│      │                                                                      │
│  T3: │ viewHost.attach() called                                             │
│      │                                                                      │
│  T4: │ GeckoView.setSession() - session re-attached                         │
│      │                                                                      │
│  T5: │ GeckoView might fire onMediaDeactivated() ← RACE!                    │
│      │     │                                                                │
│      │     └──→ MediaDeactivatedEvent dispatched                            │
│      │           │                                                          │
│      │           └──→ MediaPlaybackManager stops service!                   │
│      │                                                                      │
│  T6: │ GeckoView fires onMediaActivated() ← TOO LATE                        │
│      │     │                                                                │
│      │     └──→ Service restarts with PAUSED state (default)                │
│      │                                                                      │
│  RESULT: Notification shows PAUSED, can be dismissed                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### Problem 3: State Fragmentation

State media tersebar di banyak tempat:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         STATE FRAGMENTATION                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  TabSessionManager                                                          │
│  ├── lastKnownMediaState: BrowserMediaState?      ← Class level            │
│  ├── lastKnownMediaSession: BrowserMediaSession?  ← Class level            │
│  └── callback._mediaSession: BrowserMediaSession? ← Instance level (RESET!)│
│                                                                             │
│  MediaPlaybackManager                                                       │
│  ├── isServiceRunning: Boolean                                              │
│  ├── activeMediaTabId: String?                                              │
│  └── currentMediaSession: BrowserMediaSession?                              │
│                                                                             │
│  MediaPlaybackService                                                       │
│  └── currentState: MediaPlaybackState?                                      │
│                                                                             │
│  MediaPlaybackServiceBridge (static)                                        │
│  ├── pendingMediaSession: BrowserMediaSession?                              │
│  └── pendingArtwork: Bitmap?                                                │
│                                                                             │
│  PROBLEM: 5 tempat berbeda dengan state yang bisa out-of-sync!              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### Problem 4: Guard Clause Inconsistency

```kotlin
// onMediaActivated - NO guard, always dispatches
override fun onMediaActivated(mediaSession: BrowserMediaSession) {
    _mediaSession = mediaSession
    eventDispatcher.dispatch(MediaActivatedEvent(...))
}

// onMediaDeactivated - NO guard, always dispatches
override fun onMediaDeactivated() {
    eventDispatcher.dispatch(MediaDeactivatedEvent(...))  // ← Service stops!
}

// onMediaStateChanged - HAS guard, might drop events
override fun onMediaStateChanged(state: BrowserMediaState) {
    if (_mediaSession == null) return  // ← DROPS event if callback was reset!
    eventDispatcher.dispatch(MediaStateChangedEvent(...))
}
```

**Inkonsistensi:** `onMediaDeactivated` selalu dispatch, tapi `onMediaStateChanged` bisa drop.

---

## Alternatif Solusi

### Alternatif 1: Fix Guard Clause + Debounce Deactivation

**Deskripsi:** 
- Hapus guard `_mediaSession == null` atau restore dari class-level state
- Tambahkan delay/debounce sebelum handle deactivation

```kotlin
// TabSessionManager
override fun onMediaStateChanged(state: BrowserMediaState) {
    // Gunakan lastKnownMediaSession jika _mediaSession null
    val session = _mediaSession ?: lastKnownMediaSession ?: return
    lastKnownMediaState = state
    
    eventDispatcher.dispatch(
        MediaStateChangedEvent(tabId, state, session)
    )
}

// MediaPlaybackManager
private var deactivationJob: Job? = null

private fun handleMediaDeactivated(event: MediaDeactivatedEvent) {
    // Debounce deactivation untuk avoid race condition
    deactivationJob?.cancel()
    deactivationJob = scope.launch {
        delay(500)  // Wait 500ms
        if (event.tabId == activeMediaTabId) {
            stopService()
            clearState()
        }
    }
}

private fun handleMediaActivated(event: MediaActivatedEvent) {
    // Cancel pending deactivation
    deactivationJob?.cancel()
    // ... rest of activation logic
}
```

| Pros | Cons |
|------|------|
| Minimal code change | Masih ada state fragmentation |
| Quick fix | Delay bisa terasa oleh user |
| | Tidak solve root cause |

**Complexity:** Low
**Risk:** Medium (delay bisa menyebabkan edge cases lain)

---

### Alternatif 2: Single Source of Truth - Centralized Media State

**Deskripsi:**
Konsolidasikan semua state ke satu tempat (`MediaPlaybackManager`) sebagai single source of truth.

```kotlin
// Hapus state tracking dari TabSessionManager
// Hapus state dari callback

// MediaPlaybackManager menjadi SATU-SATUNYA tempat state
class MediaPlaybackManager(...) {
    // Single source of truth
    private val _mediaState = MutableStateFlow<MediaState?>(null)
    val mediaState: StateFlow<MediaState?> = _mediaState.asStateFlow()
    
    data class MediaState(
        val tabId: String,
        val session: BrowserMediaSession,
        val playbackState: BrowserMediaState,
        val metadata: BrowserMediaMetadata?
    )
    
    // Semua event handler update _mediaState
    // Service observe _mediaState untuk update notification
}
```

**Flow Baru:**

```
GeckoView → Callback → Event → MediaPlaybackManager._mediaState
                                        │
                                        ▼
                                 Service observes
                                        │
                                        ▼
                               Notification updated
```

| Pros | Cons |
|------|------|
| Clean architecture | Major refactoring |
| Single source of truth | Perlu rewrite banyak code |
| Predictable state | |
| Easier debugging | |

**Complexity:** High
**Risk:** Low (clean solution, tapi butuh waktu)

---

### Alternatif 3: Persistent Callback dengan Session-Level Media State

**Deskripsi:**
- Pindahkan `_mediaSession` tracking ke session level (bukan callback level)
- Callback tidak di-recreate saat tab tidak berubah

```kotlin
// BrowserSession interface
interface BrowserSession {
    // ... existing ...
    var activeMediaSession: BrowserMediaSession?  // NEW: session-level tracking
}

// GeckoBrowserSession
class GeckoBrowserSession(...) : BrowserSession {
    override var activeMediaSession: BrowserMediaSession? = null
    
    init {
        session.mediaSessionDelegate = object : MediaSession.Delegate {
            override fun onActivated(...) {
                activeMediaSession = GeckoMediaSession(mediaSession)
                sessionCallback?.onMediaActivated(activeMediaSession!!)
            }
            
            override fun onDeactivated(...) {
                activeMediaSession = null
                sessionCallback?.onMediaDeactivated()
            }
            
            override fun onPlay(...) {
                // Gunakan activeMediaSession, bukan callback-level _mediaSession
                sessionCallback?.onMediaStateChanged(BrowserMediaState.PLAY)
            }
        }
    }
}

// TabSessionManager callback tidak perlu track _mediaSession
tabHandle.session.sessionCallback = object : BrowserSessionCallback {
    // TIDAK ADA _mediaSession di sini
    
    override fun onMediaStateChanged(state: BrowserMediaState) {
        // Ambil session dari session-level
        val session = tabHandle.session.activeMediaSession ?: return
        eventDispatcher.dispatch(MediaStateChangedEvent(tabId, state, session))
    }
}
```

| Pros | Cons |
|------|------|
| Moderate code change | Perlu modify interface |
| State di tempat yang tepat | Masih multi-location state |
| Callback bisa di-reset tanpa kehilangan session | |

**Complexity:** Medium
**Risk:** Low

---

### Alternatif 4: Event Replay + Verification

**Deskripsi:**
- Saat kembali ke browser, query actual state dari GeckoView
- Replay events untuk ensure consistency

```kotlin
// BrowserSession interface
interface BrowserSession {
    // ... existing ...
    fun queryCurrentMediaState(): MediaQueryResult?
}

data class MediaQueryResult(
    val isActive: Boolean,
    val session: BrowserMediaSession?,
    val state: BrowserMediaState?
)

// Saat kembali dari notification
fun syncMediaStateFromNotification(tabId: String) {
    val currentTab = selectedTab.value ?: return
    
    // Query actual state
    val query = currentTab.session.queryCurrentMediaState() ?: return
    
    if (query.isActive && query.session != null) {
        // Dispatch current state
        eventDispatcher.dispatch(
            MediaStateChangedEvent(tabId, query.state!!, query.session)
        )
    }
}
```

| Pros | Cons |
|------|------|
| Always accurate | GeckoView mungkin tidak support query API |
| Clean solution | Perlu investigate API availability |
| | Async complexity |

**Complexity:** Medium-High (tergantung API availability)
**Risk:** High (API might not exist)

---

## Keputusan

### Rekomendasi: Kombinasi Alternatif 1 + 3

**Langkah 1 (Quick Fix):** Implementasi Alternatif 1
- Debounce deactivation untuk prevent race condition
- Gunakan class-level `lastKnownMediaSession` sebagai fallback

**Langkah 2 (Proper Fix):** Implementasi Alternatif 3
- Pindahkan media session tracking ke session level
- Membuat state lebih predictable

**Alasan:**
1. Langkah 1 bisa dilakukan cepat untuk fix bug sekarang
2. Langkah 2 memberikan solusi jangka panjang yang clean
3. Tidak perlu major refactoring (Alternatif 2)
4. Tidak bergantung pada API yang mungkin tidak ada (Alternatif 4)

---

## Implementasi

### Phase 1: Quick Fix (Immediate)

```kotlin
// 1. MediaPlaybackManager - Debounce deactivation
private var deactivationJob: Job? = null
private val DEACTIVATION_DELAY = 300L

private fun handleMediaDeactivated(event: MediaDeactivatedEvent) {
    if (event.tabId != activeMediaTabId) return
    
    // Debounce - wait sebelum benar-benar stop
    deactivationJob?.cancel()
    deactivationJob = scope.launch {
        delay(DEACTIVATION_DELAY)
        stopService()
        clearState()
    }
}

private fun handleMediaActivated(event: MediaActivatedEvent) {
    // Cancel pending deactivation
    deactivationJob?.cancel()
    deactivationJob = null
    
    // ... existing logic
}

private fun handleMediaStateChanged(event: MediaStateChangedEvent) {
    // Cancel pending deactivation jika ada activity
    if (event.state == BrowserMediaState.PLAY) {
        deactivationJob?.cancel()
        deactivationJob = null
    }
    
    // ... existing logic
}

// 2. TabSessionManager - Fallback ke class-level session
override fun onMediaStateChanged(state: BrowserMediaState) {
    val session = _mediaSession ?: lastKnownMediaSession
    if (session == null) return
    
    lastKnownMediaState = state
    
    eventDispatcher.dispatch(
        MediaStateChangedEvent(tabHandle.id, state, session)
    )
}
```

### Phase 2: Proper Fix (Next Sprint)

1. Tambahkan `activeMediaSession` ke `BrowserSession` interface
2. Implementasi di `GeckoBrowserSession`
3. Hapus `_mediaSession` dari callback
4. Update semua callback handlers

---

## Konsekuensi

### Positif

1. **Bug Fixed:** Notification tidak lagi bisa di-dismiss saat media playing
2. **Race Condition Handled:** Debounce mencegah rapid deactivate/activate
3. **State More Reliable:** Fallback ke class-level session

### Negatif

1. **Slight Delay:** 300ms delay sebelum service stop (usually tidak terasa)
2. **Complexity:** Sedikit lebih kompleks dengan debounce logic

### Risiko & Mitigasi

| Risiko | Mitigasi |
|--------|----------|
| Delay terasa oleh user | Gunakan delay pendek (300ms) |
| Deactivation tidak terjadi | Tetap proses deactivation, hanya delay |
| Memory leak dari session reference | Clear references saat tab closed |

---

## Testing Checklist

- [ ] Media playing → leave browser → click notification → notification tetap ongoing
- [ ] Media playing → pause dari notification → notification bisa dismiss
- [ ] Media playing → stop dari website → notification dismiss setelah delay
- [ ] Rapid tab switching dengan media → no crash, state consistent
- [ ] App backgrounded dengan media → notification tetap ada
- [ ] App killed → notification cleared properly

---

## Referensi

- [ADR-001: Session Lifecycle Management](001-session-lifecycle-management.md)
- [ADR-002: Media Notification Architecture](002-media-notification-architecture.md)
- [ADR-004: Phase 2 - Session-Level Media State](004-phase2-session-level-media-state.md)
- [GeckoView MediaSession Documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/)

## Changelog

| Tanggal | Perubahan |
|---------|-----------|
| 2026-01-13 | Initial proposal |
| 2026-01-13 | **Accepted** - Phase 1 implemented (debounce + fallback session) |
| 2026-01-13 | Fix race condition: initial state di-include dalam ACTION_INITIALIZE |
| 2026-01-13 | Phase 2 documented in [ADR-004](004-phase2-session-level-media-state.md) |