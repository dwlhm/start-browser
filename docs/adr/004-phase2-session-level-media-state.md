# ADR-004: Phase 2 - Session-Level Media State

| Status | Proposed |
|--------|----------|
| Tanggal | 2026-01-13 |
| Deciders | dwlhm |
| Kategori | Core Architecture |
| Depends On | [ADR-003](003-media-state-management-refactoring.md) |
| Priority | Next Sprint |

## Konteks

### Latar Belakang

ADR-003 mengidentifikasi masalah fundamental dalam arsitektur media state management:

**State Fragmentation:**
```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      CURRENT STATE LOCATIONS                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  TabSessionManager                                                          │
│  ├── lastKnownMediaState: BrowserMediaState?      ← Workaround (Phase 1)   │
│  ├── lastKnownMediaSession: BrowserMediaSession?  ← Workaround (Phase 1)   │
│  └── callback._mediaSession: BrowserMediaSession? ← PROBLEM: Resets!       │
│                                                                             │
│  GeckoBrowserSession                                                        │
│  └── _hasActiveMedia: Boolean                     ← Partial tracking       │
│                                                                             │
│  MediaPlaybackManager                                                       │
│  ├── activeMediaTabId: String?                                              │
│  └── currentMediaSession: BrowserMediaSession?                              │
│                                                                             │
│  MediaPlaybackService                                                       │
│  └── currentState: MediaPlaybackState?                                      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Masalah dengan Phase 1 (Current Implementation)

Phase 1 menggunakan **workaround** berupa:
1. `lastKnownMediaSession` sebagai fallback di `TabSessionManager`
2. Debounce deactivation di `MediaPlaybackManager`
3. Initial state di-include dalam `ACTION_INITIALIZE`

**Limitasi Phase 1:**
- State masih tersebar di banyak tempat
- Fallback pattern menambah kompleksitas
- `callback._mediaSession` masih bisa reset saat callback di-recreate
- Potential edge cases yang belum tercover

### Tujuan Phase 2

Memindahkan media session tracking ke **session level** (`BrowserSession`) sehingga:
1. State tidak hilang saat callback di-recreate
2. Single source of truth untuk media session per tab
3. Eliminasi kebutuhan workaround/fallback

---

## Keputusan

### Arsitektur Baru

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      PROPOSED STATE LOCATION                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  BrowserSession (Interface)                                                 │
│  └── activeMediaSession: BrowserMediaSession?  ← NEW: Single source        │
│                                                                             │
│  GeckoBrowserSession (Implementation)                                       │
│  ├── _hasActiveMedia: Boolean                                               │
│  └── _activeMediaSession: BrowserMediaSession? ← Backing field             │
│                                                                             │
│  TabSessionManager                                                          │
│  └── callback TIDAK perlu track _mediaSession  ← SIMPLIFIED                │
│                                                                             │
│  MediaPlaybackManager                                                       │
│  └── (unchanged - receives events)                                          │
│                                                                             │
│  MediaPlaybackService                                                       │
│  └── (unchanged - manages notification)                                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Perubahan Interface

#### 1. BrowserSession Interface

```kotlin
// File: core/browser/src/main/java/com/dwlhm/browser/BrowserSession.kt

interface BrowserSession {
    // ... existing properties ...
    
    /**
     * Media session yang sedang aktif di session ini.
     * 
     * Lifecycle:
     * - Set saat onMediaActivated() dari GeckoView
     * - Null saat onMediaDeactivated() dari GeckoView
     * 
     * Ini adalah single source of truth untuk media session per tab.
     * Tidak perlu tracking di callback karena property ini persist
     * selama session hidup.
     */
    val activeMediaSession: BrowserMediaSession?
}
```

#### 2. GeckoBrowserSession Implementation

```kotlin
// File: engine/gecko/src/main/java/com/dwlhm/gecko/api/GeckoBrowserSession.kt

class GeckoBrowserSession(
    private val session: GeckoSession,
) : BrowserSession {
    
    // Existing
    private var _hasActiveMedia: Boolean = false
    override val hasActiveMedia: Boolean get() = _hasActiveMedia
    
    // NEW: Session-level media session tracking
    private var _activeMediaSession: BrowserMediaSession? = null
    override val activeMediaSession: BrowserMediaSession?
        get() = _activeMediaSession
    
    init {
        session.mediaSessionDelegate = object : MediaSession.Delegate {
            override fun onActivated(session: GeckoSession, mediaSession: MediaSession) {
                _hasActiveMedia = true
                _activeMediaSession = GeckoMediaSession(mediaSession)  // Track here
                sessionCallback?.onMediaActivated(_activeMediaSession!!)
            }
            
            override fun onDeactivated(session: GeckoSession, mediaSession: MediaSession) {
                _hasActiveMedia = false
                _activeMediaSession = null  // Clear here
                sessionCallback?.onMediaDeactivated()
            }
            
            override fun onPlay(session: GeckoSession, mediaSession: MediaSession) {
                // Session sudah di-track di session level
                sessionCallback?.onMediaStateChanged(BrowserMediaState.PLAY)
            }
            
            override fun onPause(session: GeckoSession, mediaSession: MediaSession) {
                sessionCallback?.onMediaStateChanged(BrowserMediaState.PAUSE)
            }
            
            override fun onStop(session: GeckoSession, mediaSession: MediaSession) {
                sessionCallback?.onMediaStateChanged(BrowserMediaState.STOP)
            }
            
            // ... metadata handler unchanged
        }
    }
}
```

#### 3. TabSessionManager Callback (Simplified)

```kotlin
// File: feature/tabmanager/src/main/java/com/dwlhm/tabmanager/api/TabSessionManager.kt

class TabSessionManager(...) {
    // REMOVE these workaround fields:
    // private var lastKnownMediaState: BrowserMediaState? = null
    // private var lastKnownMediaSession: BrowserMediaSession? = null
    
    private fun observeSelectedTab() {
        selectedTab.onEach { tabHandle ->
            lastSelectedTab?.session?.sessionCallback = null
            
            if (tabHandle == null) {
                lastSelectedTab = null
                return@onEach
            }
            
            lastSelectedTab = tabHandle
            
            tabHandle.session.sessionCallback = object : BrowserSessionCallback {
                // TIDAK ADA _mediaSession di sini!
                
                override fun onTabInfoChanged(title: String, url: String) {
                    eventDispatcher.dispatch(
                        TabInfoChangedEvent(tabHandle.id, title, url)
                    )
                }
                
                override fun onMediaActivated(mediaSession: BrowserMediaSession) {
                    // Session di-track di BrowserSession level
                    eventDispatcher.dispatch(
                        MediaActivatedEvent(tabHandle.id, mediaSession)
                    )
                }
                
                override fun onMediaDeactivated() {
                    eventDispatcher.dispatch(
                        MediaDeactivatedEvent(tabHandle.id)
                    )
                }
                
                override fun onMediaMetadataChanged(mediaMetadata: BrowserMediaMetadata) {
                    // Ambil session dari session-level (single source of truth)
                    val session = tabHandle.session.activeMediaSession ?: return
                    eventDispatcher.dispatch(
                        MediaMetadataChangedEvent(tabHandle.id, mediaMetadata, session)
                    )
                }
                
                override fun onMediaStateChanged(state: BrowserMediaState) {
                    // Ambil session dari session-level (single source of truth)
                    val session = tabHandle.session.activeMediaSession ?: return
                    eventDispatcher.dispatch(
                        MediaStateChangedEvent(tabHandle.id, state, session)
                    )
                }
            }
        }.launchIn(scope)
    }
    
    // syncMediaStateFromNotification juga di-simplify
    fun syncMediaStateFromNotification(tabId: String) {
        val currentTab = selectedTab.value ?: return
        if (currentTab.id != tabId) return
        
        // Ambil langsung dari session - tidak perlu lastKnown workaround
        val session = currentTab.session.activeMediaSession ?: return
        
        // Dispatch current state
        // Note: Kita perlu cara untuk tahu current state...
        // Option 1: Track state di BrowserSession juga
        // Option 2: Keep lastKnownMediaState untuk sync only
    }
}
```

---

## Implementasi Plan

### Step 1: Update BrowserSession Interface

```
File: core/browser/src/main/java/com/dwlhm/browser/BrowserSession.kt
Change: Add activeMediaSession property
Impact: Interface change - semua implementasi harus update
```

### Step 2: Update GeckoBrowserSession

```
File: engine/gecko/src/main/java/com/dwlhm/gecko/api/GeckoBrowserSession.kt
Change: 
  - Add _activeMediaSession backing field
  - Implement activeMediaSession property
  - Update mediaSessionDelegate callbacks
Impact: Engine layer only
```

### Step 3: Simplify TabSessionManager

```
File: feature/tabmanager/src/main/java/com/dwlhm/tabmanager/api/TabSessionManager.kt
Change:
  - Remove lastKnownMediaSession (use session.activeMediaSession)
  - Keep lastKnownMediaState for sync functionality
  - Simplify callback (remove _mediaSession)
Impact: Feature layer, cleaner code
```

### Step 4: Update Tests

```
Files: Test files for affected modules
Change: Update tests for new interface
```

---

## Migration Strategy

### Backward Compatibility

Phase 1 workarounds tetap berfungsi selama migration:

```kotlin
// TabSessionManager - during migration
override fun onMediaStateChanged(state: BrowserMediaState) {
    // Try new approach first
    val session = tabHandle.session.activeMediaSession
        // Fallback to workaround if activeMediaSession not yet implemented
        ?: lastKnownMediaSession
        ?: return
    
    lastKnownMediaState = state
    eventDispatcher.dispatch(MediaStateChangedEvent(tabId, state, session))
}
```

### Rollout Plan

1. **Week 1:** Implement Step 1 & 2 (Interface + GeckoSession)
2. **Week 2:** Implement Step 3 (TabSessionManager)
3. **Week 3:** Testing & bug fixes
4. **Week 4:** Remove Phase 1 workarounds

---

## Konsekuensi

### Positif

1. **Single Source of Truth**
   - Media session hanya di-track di satu tempat (`BrowserSession`)
   - Tidak ada state fragmentation

2. **Eliminasi Workarounds**
   - Tidak perlu `lastKnownMediaSession` fallback
   - Callback bisa di-reset tanpa kehilangan session

3. **Cleaner Code**
   - Callback lebih simple (tidak track state sendiri)
   - Lebih mudah di-maintain

4. **Better Testability**
   - State mudah di-mock di session level
   - Less dependencies in callbacks

### Negatif

1. **Interface Change**
   - Semua `BrowserSession` implementations harus update
   - Currently only `GeckoBrowserSession`, tapi future engines juga

2. **Migration Complexity**
   - Perlu careful rollout
   - Potential regressions during migration

### Trade-offs

| Aspect | Phase 1 | Phase 2 |
|--------|---------|---------|
| Complexity | Workarounds | Clean |
| Risk | Low (quick fix) | Medium (interface change) |
| Maintenance | Higher (multiple tracking) | Lower (single source) |
| Edge cases | Potential misses | Better coverage |

---

## Open Questions

### Q1: Apakah perlu track `activeMediaState` juga di session level?

**Context:** `syncMediaStateFromNotification` perlu tahu current state.

**Options:**
1. Track state di `BrowserSession` (full session-level tracking)
2. Keep `lastKnownMediaState` di `TabSessionManager` (hybrid)

**Recommendation:** Option 2 - keep it simple, state tracking untuk sync saja.

### Q2: Bagaimana handle multiple media sessions per tab?

**Context:** Satu tab bisa punya multiple `<video>` elements.

**Current behavior:** GeckoView hanya report satu active media session.

**Decision:** Follow GeckoView behavior - satu active session per tab.

---

## Referensi

- [ADR-003: Media State Management Refactoring](003-media-state-management-refactoring.md)
- [Design Doc: Media State Synchronization](../design/media-state-synchronization.md)
- [GeckoView MediaSession API](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/)

## Changelog

| Tanggal | Perubahan |
|---------|-----------|
| 2026-01-13 | Initial proposal |
