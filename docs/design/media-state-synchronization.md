# Technical Design Document: Media State Synchronization

| Document Info | |
|---------------|---|
| Author | dwlhm |
| Created | 2026-01-13 |
| Status | Implemented |
| Related Files | `TabSessionManager.kt`, `MainScreen.kt`, `MediaPlaybackManager.kt` |

## 1. Problem Statement

### 1.1 Masalah yang Ditemukan

**Bug:** Setelah user klik notification media dan kembali ke browser, tombol play/pause di notification tidak sinkron dengan state yang ditampilkan di website.

**Contoh:**
- Notification menampilkan tombol PLAY (berarti state = PAUSED)
- Tapi di website, video terlihat sedang berjalan (state seharusnya = PLAYING)

### 1.2 Akar Masalah

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    NORMAL FLOW (Works Correctly)                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   User clicks Play     GeckoView fires      Callback dispatches        │
│   on website      →    onPlay callback  →   MediaStateChangedEvent  → │
│                                                                         │
│                        → Manager receives → Service updates →           │
│                          event              notification                │
│                                                                         │
│   Result: Notification shows PAUSE button ✓                            │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│              PROBLEMATIC FLOW (Returning from Notification)              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   User clicks          Navigate to         GeckoView does NOT          │
│   notification    →    browser        →    fire callback               │
│                                             (state unchanged)           │
│                                                                         │
│   Why? Because from GeckoView's perspective, the media state           │
│   never changed. It was PLAYING before, still PLAYING now.             │
│                                                                         │
│   Result: Notification might show stale state ✗                        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.3 Kapan Bug Ini Terjadi?

| Skenario | Notification Updated? | Alasan |
|----------|----------------------|--------|
| User pause dari notification | ✓ Ya | Callback `onPause` fired |
| User play dari notification | ✓ Ya | Callback `onPlay` fired |
| User pause dari website | ✓ Ya | Callback `onPause` fired |
| User kembali dari notification (state sama) | ✗ Tidak | Tidak ada callback karena state tidak berubah |
| App di-restart | ✗ Tidak | State hilang, perlu re-initialization |

---

## 2. Solution Design

### 2.1 Prinsip yang Digunakan

#### 2.1.1 State Tracking Pattern

**Definisi:** Menyimpan copy dari state terakhir yang diketahui untuk digunakan saat source of truth tidak dapat diquery.

```kotlin
// Track state setiap kali berubah
private var lastKnownMediaState: BrowserMediaState? = null

override fun onMediaStateChanged(state: BrowserMediaState) {
    lastKnownMediaState = state  // <- Tracking
    dispatchEvent(state)
}
```

**Kapan digunakan:**
- Saat external system (GeckoView) tidak menyediakan API untuk query current state
- Saat callbacks adalah satu-satunya cara mendapat informasi state
- Saat perlu "replay" state tanpa trigger dari external system

#### 2.1.2 Manual Sync / Force Refresh Pattern

**Definisi:** Mekanisme untuk memaksa sinkronisasi state secara manual ketika automatic sync tidak bekerja.

```kotlin
fun syncMediaStateFromNotification(tabId: String) {
    // Tidak menunggu callback dari GeckoView
    // Langsung dispatch event dengan last known state
    eventDispatcher.dispatch(
        MediaStateChangedEvent(state = lastKnownMediaState)
    )
}
```

**Kapan digunakan:**
- Saat kembali dari navigation yang tidak trigger automatic refresh
- Saat external system tidak fire events untuk state yang "sama"
- Saat perlu ensure consistency setelah certain operations

#### 2.1.3 Defensive Programming

**Definisi:** Mengantisipasi edge cases dan failure modes secara proaktif.

```kotlin
fun syncMediaStateFromNotification(tabId: String) {
    val currentTab = selectedTab.value ?: return          // Guard: no tab
    if (currentTab.id != tabId) return                    // Guard: wrong tab
    if (!currentTab.session.hasActiveMedia) return        // Guard: no media
    
    val state = lastKnownMediaState ?: return             // Guard: no tracked state
    val mediaSession = lastKnownMediaSession ?: return    // Guard: no session
    
    // Safe to proceed
    eventDispatcher.dispatch(...)
}
```

### 2.2 Implementasi

#### File yang Dimodifikasi

```
feature/tabmanager/
└── TabSessionManager.kt
    ├── + lastKnownMediaState: BrowserMediaState?
    ├── + lastKnownMediaSession: BrowserMediaSession?
    ├── + syncMediaStateFromNotification(tabId: String)
    └── ~ observeSelectedTab() - track state di callbacks

app/src/main/java/.../ui/
└── MainScreen.kt
    └── ~ LaunchedEffect - panggil sync saat kembali dari notification
```

#### Sequence Diagram

```
┌──────────┐     ┌──────────────┐     ┌──────────────────┐     ┌─────────────────┐
│Notification│    │  MainScreen  │     │TabSessionManager │     │MediaPlayback    │
│          │     │              │     │                  │     │Manager          │
└────┬─────┘     └──────┬───────┘     └────────┬─────────┘     └────────┬────────┘
     │                  │                      │                        │
     │ User clicks      │                      │                        │
     │ notification     │                      │                        │
     │─────────────────>│                      │                        │
     │                  │                      │                        │
     │                  │ Navigate to browser  │                        │
     │                  │─────────────────────>│                        │
     │                  │                      │                        │
     │                  │ syncMediaState       │                        │
     │                  │ FromNotification()   │                        │
     │                  │─────────────────────>│                        │
     │                  │                      │                        │
     │                  │                      │ Dispatch               │
     │                  │                      │ MediaStateChangedEvent │
     │                  │                      │───────────────────────>│
     │                  │                      │                        │
     │                  │                      │                        │ Update
     │                  │                      │                        │ notification
     │                  │                      │                        │────────┐
     │                  │                      │                        │        │
     │<─────────────────────────────────────────────────────────────────────────┘
     │ Notification                            │                        │
     │ updated                                 │                        │
     │                  │                      │                        │
```

---

## 3. Guidelines for Future Development

### 3.1 Checklist: Menambah Navigation dari Notification

Saat menambah navigation baru yang bisa dipicu dari notification:

- [ ] **Identifikasi state apa yang perlu di-sync**
  - Media state? Tab state? User preference?
  
- [ ] **Cek apakah external system akan fire callback**
  - Jika YA: tidak perlu manual sync
  - Jika TIDAK: implement state tracking + manual sync
  
- [ ] **Implement state tracking jika diperlukan**
  - Track state di setiap callback
  - Clear tracked state saat deactivated/destroyed
  
- [ ] **Panggil sync di navigation handler**
  - Cek kondisi: apakah ini return dari notification?
  - Cek kondisi: apakah state perlu di-sync?
  - Panggil sync method

### 3.2 Checklist: Menambah Callback Baru dari External System

Saat menambah callback baru (e.g., dari GeckoView):

- [ ] **Tentukan apakah state ini perlu di-track**
  - Apakah ada skenario di mana state ini perlu di-"replay"?
  - Apakah external system tidak punya query API?
  
- [ ] **Track state di callback jika perlu**
  ```kotlin
  override fun onNewCallback(data: Data) {
      lastKnownData = data  // Track
      dispatchEvent(data)
  }
  ```

- [ ] **Clear tracked state di deactivation callback**
  ```kotlin
  override fun onDeactivated() {
      lastKnownData = null  // Clear
      dispatchEvent(...)
  }
  ```

- [ ] **Tambahkan sync method jika ada use case**

### 3.3 Red Flags: Kapan Harus Waspada

| Situasi | Potensi Bug | Solusi |
|---------|-------------|--------|
| Navigation tidak trigger re-render | State UI bisa stale | Implement manual refresh |
| External callback hanya fire saat state berubah | State bisa out-of-sync saat return | State tracking + manual sync |
| Singleton/global state dengan multiple consumers | Race condition, stale data | Single source of truth, event-driven |
| Async operation tanpa confirmation | Operation bisa fail silently | Implement verification/sync |

### 3.4 Testing Scenarios

Untuk memastikan sinkronisasi state bekerja dengan benar:

| Test Case | Steps | Expected Result |
|-----------|-------|-----------------|
| Return to same tab (playing) | 1. Play media<br>2. Leave browser<br>3. Click notification | Notification shows PAUSE button |
| Return to same tab (paused) | 1. Play then pause media<br>2. Leave browser<br>3. Click notification | Notification shows PLAY button |
| Return to different tab | 1. Play media on Tab A<br>2. Switch to Tab B<br>3. Click notification for Tab A | Navigate to Tab A, notification synced |
| Media stops while away | 1. Play media<br>2. Leave browser<br>3. Media ends naturally<br>4. Click notification | Notification cleared or shows correct state |

---

## 4. Appendix

### 4.1 Why Not Query GeckoView Directly?

**Pertanyaan:** Kenapa tidak query state langsung dari GeckoView saat return dari notification?

**Jawaban:** GeckoView's MediaSession API adalah **callback-based**, bukan **query-based**.

```kotlin
// GeckoView MediaSession API - hanya callbacks
session.mediaSessionDelegate = object : MediaSession.Delegate {
    override fun onPlay(...) { }     // Callback saat play
    override fun onPause(...) { }    // Callback saat pause
    // Tidak ada: fun getCurrentState(): State
}
```

Ini adalah design choice dari Mozilla. Banyak browser APIs menggunakan pattern yang sama karena:
1. **Efficiency:** Tidak perlu polling
2. **Real-time:** Langsung dapat update saat berubah
3. **Battery-friendly:** Tidak ada periodic checks

Trade-off: Kita harus implement state tracking sendiri.

### 4.2 Alternative Solutions Considered

| Alternative | Pros | Cons | Decision |
|-------------|------|------|----------|
| **Polling state** | Always up-to-date | Battery drain, no API | ❌ Rejected |
| **Store state di SharedPreferences** | Persists across restart | Overkill, async I/O | ❌ Rejected |
| **State tracking in-memory** | Simple, fast, sufficient | Lost on process death | ✅ Accepted |
| **Force refresh notification on every navigation** | Always synced | Unnecessary updates | ❌ Rejected |

### 4.3 Related Patterns in Android

| Pattern | Description | Used By |
|---------|-------------|---------|
| **SavedStateHandle** | Track state across config changes | Jetpack ViewModel |
| **rememberSaveable** | Track state in Compose | Compose |
| **onSaveInstanceState** | Track state across process death | Activity/Fragment |
| **StateFlow** | Reactive state tracking | Kotlin Coroutines |

Our solution is similar to these patterns but simpler because:
- We only need to track during single session
- We don't need to survive process death
- State is only needed for sync, not restoration

---

## 5. Additional Fixes: Race Condition & Callback Reset

### 5.1 Problem: Notification Bisa Di-Dismiss

Setelah implementasi awal, ditemukan bug tambahan:
- Notification yang seharusnya `ongoing` bisa di-dismiss (swipe away)
- Ini terjadi karena notification kehilangan state PLAYING

### 5.2 Root Cause Analysis

#### Issue 1: Race Condition Saat View Re-attach

```
┌─────────────────────────────────────────────────────────────────────────┐
│  T0: User clicks notification                                           │
│  T1: View re-attached                                                   │
│  T2: GeckoView fires onMediaDeactivated() ← RACE!                      │
│       └──→ Service STOPS                                                │
│  T3: GeckoView fires onMediaActivated()                                │
│       └──→ Service restarts with PAUSED (default)                      │
│                                                                         │
│  Result: Notification shows PAUSED, can be dismissed ✗                 │
└─────────────────────────────────────────────────────────────────────────┘
```

#### Issue 2: Callback Instance Reset

```kotlin
// Setiap kali selectedTab emit, callback BARU dibuat
tabHandle.session.sessionCallback = object : BrowserSessionCallback {
    private var _mediaSession: BrowserMediaSession? = null  // ← RESET!
    
    override fun onMediaStateChanged(state: BrowserMediaState) {
        if (_mediaSession == null) return  // ← EVENT DROPPED!
    }
}
```

### 5.3 Solutions Implemented

#### Solution 1: Debounce Deactivation Pattern

**File:** `MediaPlaybackManager.kt`

```kotlin
companion object {
    private const val DEACTIVATION_DELAY_MS = 300L
}

private var deactivationJob: Job? = null

private fun handleMediaActivated(event: MediaActivatedEvent) {
    // Cancel pending deactivation - activation takes priority
    deactivationJob?.cancel()
    deactivationJob = null
    // ... rest of logic
}

private fun handleMediaDeactivated(event: MediaDeactivatedEvent) {
    // Debounce: wait sebelum stop service
    deactivationJob = scope.launch {
        delay(DEACTIVATION_DELAY_MS)
        // ... stop service logic
    }
}

private fun handleMediaStateChanged(event: MediaStateChangedEvent) {
    if (event.state == BrowserMediaState.PLAY) {
        // Cancel pending deactivation - media still active
        deactivationJob?.cancel()
        deactivationJob = null
    }
    // ... rest of logic
}
```

**Prinsip:** Deactivation di-delay untuk memberikan waktu bagi activation event membatalkannya.

#### Solution 2: Fallback Session Pattern

**File:** `TabSessionManager.kt`

```kotlin
// Class-level tracking (survives callback reset)
private var lastKnownMediaSession: BrowserMediaSession? = null

// Di callback
override fun onMediaStateChanged(state: BrowserMediaState) {
    // Fallback ke lastKnownMediaSession jika _mediaSession null
    val session = _mediaSession ?: lastKnownMediaSession
    if (session == null) return
    
    // ... dispatch event with session
}
```

**Prinsip:** Jika instance-level session null (karena callback reset), gunakan class-level fallback.

### 5.4 Why 300ms Delay?

| Delay | Pros | Cons |
|-------|------|------|
| 100ms | Faster response | Might not be enough for slow devices |
| 300ms | Handles most race conditions | Slight delay (usually unnoticeable) |
| 500ms | Very safe | Noticeable delay |

**300ms dipilih karena:**
- Cukup pendek untuk tidak terasa oleh user
- Cukup panjang untuk handle race condition pada kebanyakan device
- Balance antara responsiveness dan reliability

### 5.5 Testing Scenarios (Updated)

| Test Case | Steps | Expected Result |
|-----------|-------|-----------------|
| View re-attach race | 1. Play media<br>2. Leave browser<br>3. Click notification rapidly | Notification stays ongoing |
| Callback reset | 1. Play media<br>2. Switch tabs<br>3. Switch back | Media state synced correctly |
| Deactivation cancelled | 1. Play media<br>2. Trigger deactivate<br>3. Play again within 300ms | Service doesn't stop |

---

## 6. Additional Fix: Initial State Race Condition

### 6.1 Problem

Setelah fix sebelumnya, masih ada issue: notification pertama kali muncul dengan state yang salah.

### 6.2 Root Cause

```
Timeline Race Condition:
────────────────────────
T0: GeckoView fires onActivated
T1: GeckoView fires onPlay (almost simultaneously)
T2: MediaActivatedEvent dispatched
T3: MediaStateChangedEvent(PLAY) dispatched
T4: handleMediaStateChanged → startServiceWithInitialState (ACTION_INITIALIZE)
T5: handleMediaStateChanged → updateServiceState (ACTION_UPDATE_STATE)
T6: Service receives ACTION_UPDATE_STATE
    BUT currentState is still null! → handleUpdateState returns early
T7: Service receives ACTION_INITIALIZE
    → Creates state with PAUSE (default)
    → Notification shows PAUSE ✗
```

**Issue:** Intent adalah async. `ACTION_UPDATE_STATE` bisa sampai SEBELUM `ACTION_INITIALIZE` diproses.

### 6.3 Solution: Include Initial State in ACTION_INITIALIZE

**Sebelum:**
```kotlin
// Manager
startServiceWithInitialState(tabId)  // No state
updateServiceState(event.state)       // Separate intent - race condition!

// Service handleInitialize
currentState = MediaPlaybackState(
    tabId = tabId,
    mediaSession = mediaSession
    // playbackState defaults to PAUSE!
)
```

**Sesudah:**
```kotlin
// Manager
startServiceWithInitialState(tabId, event.state)  // State included
// No separate updateServiceState needed

// Service handleInitialize
val initialState = intent.getStringExtra(EXTRA_STATE)?.let {
    BrowserMediaState.valueOf(it)
} ?: BrowserMediaState.PAUSE

currentState = MediaPlaybackState(
    tabId = tabId,
    mediaSession = mediaSession,
    playbackState = initialState  // Correct state from start
)
```

### 6.4 Additional Change: handleMediaActivated

`handleMediaActivated` tidak lagi start service karena:
1. Saat `onMediaActivated` fire, kita belum tahu state sebenarnya (PLAY/PAUSE)
2. State sebenarnya datang dari `MediaStateChangedEvent`
3. Biarkan `handleMediaStateChanged` yang start service dengan state yang benar

```kotlin
private fun handleMediaActivated(event: MediaActivatedEvent) {
    // Cancel pending deactivation
    deactivationJob?.cancel()
    
    // Update internal state
    activeMediaTabId = event.tabId
    currentMediaSession = event.mediaSession
    
    // Setup bridge
    MediaPlaybackServiceBridge.setMediaSession(event.mediaSession)
    
    // TIDAK start service - biarkan MediaStateChangedEvent yang handle
}
```

---

## 7. Changelog

| Date | Change |
|------|--------|
| 2026-01-13 | Initial implementation - fix notification sync bug |
| 2026-01-13 | Added debounce deactivation + fallback session patterns (ADR-003) |
| 2026-01-13 | Fix initial state race condition - include state in ACTION_INITIALIZE |