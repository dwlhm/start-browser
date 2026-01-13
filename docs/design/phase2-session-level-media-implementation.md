# Technical Design: Phase 2 - Session-Level Media State Implementation

> **Dokumen ini menjelaskan detail teknis implementasi Phase 2 dari ADR-004.**

## 1. Overview

### 1.1 Tujuan Dokumen

Dokumen ini memberikan panduan teknis lengkap untuk mengimplementasikan session-level media state tracking. Berbeda dengan ADR yang fokus pada keputusan arsitektur, dokumen ini fokus pada **HOW** - bagaimana mengimplementasikan perubahan tersebut.

### 1.2 Problem Statement

**Current Architecture (Phase 1):**

```
GeckoView Callback
       │
       ▼
TabSessionManager.callback
├── _mediaSession: BrowserMediaSession?  ← Resets when callback recreated!
├── lastKnownMediaSession (workaround)
└── lastKnownMediaState (workaround)
       │
       ▼
   EventDispatcher
       │
       ▼
MediaPlaybackManager
```

**Masalah:**
- `_mediaSession` di callback bisa `null` saat callback instance baru dibuat
- Workaround `lastKnown*` menambah kompleksitas
- State tersebar di banyak tempat

**Target Architecture (Phase 2):**

```
GeckoView Callback
       │
       ▼
GeckoBrowserSession
└── activeMediaSession: BrowserMediaSession?  ← Persists!
       │
       ├──────────────────┐
       ▼                  ▼
 (session property)   Callback dispatches events
                          │
                          ▼
                     EventDispatcher
                          │
                          ▼
                   MediaPlaybackManager
```

---

## 2. Detailed Implementation

### 2.1 Layer 1: Core Browser Interface

**File:** `core/browser/src/main/java/com/dwlhm/browser/BrowserSession.kt`

```kotlin
/**
 * Interface yang merepresentasikan browser session.
 * 
 * Browser session adalah unit dasar dari browsing - satu session = satu tab.
 * Session ini meng-encapsulate semua state terkait tab tersebut termasuk:
 * - URL dan title
 * - Navigation state
 * - Media playback state (NEW in Phase 2)
 */
interface BrowserSession {
    // === Existing Properties ===
    
    /** Apakah session ini memiliki media yang sedang aktif */
    val hasActiveMedia: Boolean
    
    /** Navigation capability (back/forward) */
    val canGoBack: Boolean
    val canGoForward: Boolean
    
    /** Callback untuk events dari session */
    var sessionCallback: BrowserSessionCallback?
    
    // === NEW: Phase 2 Media Properties ===
    
    /**
     * Media session yang sedang aktif di browser session ini.
     * 
     * ## Lifecycle
     * 
     * ```
     * null ──onMediaActivated──▶ BrowserMediaSession
     *                                    │
     *                                    │ (active)
     *                                    ▼
     *                           ┌─────────────────┐
     *                           │ onPlay/onPause  │
     *                           │ (state changes) │
     *                           └─────────────────┘
     *                                    │
     *               onMediaDeactivated   │
     *                         │◀─────────┘
     *                         ▼
     *                       null
     * ```
     * 
     * ## Usage
     * 
     * ```kotlin
     * // Di callback, gunakan session.activeMediaSession sebagai source of truth
     * override fun onMediaStateChanged(state: BrowserMediaState) {
     *     val session = browserSession.activeMediaSession ?: return
     *     eventDispatcher.dispatch(MediaStateChangedEvent(tabId, state, session))
     * }
     * ```
     * 
     * ## Thread Safety
     * 
     * Property ini HANYA diakses dari main thread karena semua
     * GeckoView callbacks berjalan di main thread.
     * 
     * @return BrowserMediaSession yang aktif, atau null jika tidak ada media aktif
     */
    val activeMediaSession: BrowserMediaSession?
    
    // === Existing Methods ===
    
    fun setUrl(url: String)
    fun goBack()
    fun goForward()
    fun reload()
    fun close()
}
```

**Rationale untuk desain:**
- Property adalah `val` (read-only dari luar) - hanya GeckoSession yang bisa set
- Nullable karena media tidak selalu aktif
- Dokumentasi lifecycle penting untuk pemahaman developer lain

---

### 2.2 Layer 2: Gecko Engine Implementation

**File:** `engine/gecko/src/main/java/com/dwlhm/gecko/api/GeckoBrowserSession.kt`

```kotlin
/**
 * Implementasi BrowserSession menggunakan GeckoView.
 * 
 * Class ini adalah bridge antara GeckoView API dan abstraksi browser kita.
 * Semua GeckoView-specific code ada di sini.
 */
class GeckoBrowserSession(
    private val session: GeckoSession,
    private val runtime: GeckoRuntime,
) : BrowserSession {
    
    // === Existing State ===
    
    private var _hasActiveMedia: Boolean = false
    override val hasActiveMedia: Boolean get() = _hasActiveMedia
    
    private var _canGoBack: Boolean = false
    override val canGoBack: Boolean get() = _canGoBack
    
    private var _canGoForward: Boolean = false
    override val canGoForward: Boolean get() = _canGoForward
    
    override var sessionCallback: BrowserSessionCallback? = null
    
    // === NEW: Phase 2 Media State ===
    
    /**
     * Backing field untuk activeMediaSession.
     * 
     * Diset di mediaSessionDelegate callbacks.
     * Hanya diakses dari main thread (GeckoView guarantee).
     */
    private var _activeMediaSession: BrowserMediaSession? = null
    
    /**
     * Media session yang sedang aktif.
     * 
     * Ini adalah SINGLE SOURCE OF TRUTH untuk media session di tab ini.
     * Callback (TabSessionManager) harus menggunakan property ini
     * bukan tracking sendiri.
     */
    override val activeMediaSession: BrowserMediaSession?
        get() = _activeMediaSession
    
    // === Initialization ===
    
    init {
        setupMediaSessionDelegate()
        setupNavigationDelegate()
        // ... other delegates
    }
    
    /**
     * Setup media session delegate untuk handle playback events.
     * 
     * ## Why session-level tracking?
     * 
     * GeckoView's MediaSession.Delegate callbacks dapat dipanggil
     * kapan saja selama media aktif. Jika kita track session di
     * callback level (seperti Phase 1), callback bisa di-recreate
     * dan kehilangan reference ke session.
     * 
     * Dengan tracking di session level:
     * 1. Session persist selama GeckoBrowserSession hidup
     * 2. Callback bisa di-recreate tanpa masalah
     * 3. Single source of truth
     */
    private fun setupMediaSessionDelegate() {
        session.mediaSessionDelegate = object : MediaSession.Delegate {
            
            /**
             * Called when media session is activated.
             * 
             * Ini terjadi saat:
             * - Video/audio mulai play pertama kali
             * - Tab dengan media dipindah ke foreground (setelah sebelumnya di background)
             * 
             * IMPORTANT: Set _activeMediaSession DI SINI sebelum notify callback.
             */
            override fun onActivated(
                session: GeckoSession, 
                mediaSession: MediaSession
            ) {
                _hasActiveMedia = true
                
                // Create dan track session
                val browserMediaSession = GeckoMediaSession(mediaSession)
                _activeMediaSession = browserMediaSession
                
                // Notify callback SETELAH session di-set
                sessionCallback?.onMediaActivated(browserMediaSession)
            }
            
            /**
             * Called when media session is deactivated.
             * 
             * Ini terjadi saat:
             * - Video/audio di-stop (bukan pause)
             * - Tab ditutup
             * - Navigasi away dari page dengan media
             */
            override fun onDeactivated(
                session: GeckoSession, 
                mediaSession: MediaSession
            ) {
                _hasActiveMedia = false
                _activeMediaSession = null  // Clear session
                
                sessionCallback?.onMediaDeactivated()
            }
            
            /**
             * Called when media starts playing.
             * 
             * Note: Tidak perlu pass mediaSession ke callback karena
             * callback bisa akses via session.activeMediaSession.
             */
            override fun onPlay(session: GeckoSession, mediaSession: MediaSession) {
                // Verify session is still tracked (defensive)
                if (_activeMediaSession == null) {
                    android.util.Log.w(TAG, "onPlay called but no active session")
                    return
                }
                
                sessionCallback?.onMediaStateChanged(BrowserMediaState.PLAY)
            }
            
            override fun onPause(session: GeckoSession, mediaSession: MediaSession) {
                if (_activeMediaSession == null) {
                    android.util.Log.w(TAG, "onPause called but no active session")
                    return
                }
                
                sessionCallback?.onMediaStateChanged(BrowserMediaState.PAUSE)
            }
            
            override fun onStop(session: GeckoSession, mediaSession: MediaSession) {
                // onStop biasanya followed by onDeactivated
                // tapi kita tetap forward ke callback untuk consistency
                sessionCallback?.onMediaStateChanged(BrowserMediaState.STOP)
            }
            
            override fun onMetadata(
                session: GeckoSession, 
                mediaSession: MediaSession,
                meta: MediaSession.Metadata
            ) {
                val metadata = BrowserMediaMetadata(
                    title = meta.title,
                    artist = meta.artist,
                    album = meta.album,
                    artwork = meta.artwork
                )
                
                sessionCallback?.onMediaMetadataChanged(metadata)
            }
        }
    }
    
    companion object {
        private const val TAG = "GeckoBrowserSession"
    }
}
```

**Key Implementation Details:**

1. **Order of Operations di `onActivated`:**
   ```kotlin
   _activeMediaSession = browserMediaSession  // SET FIRST
   sessionCallback?.onMediaActivated(...)      // THEN notify
   ```
   Ini penting agar callback bisa langsung akses `activeMediaSession`.

2. **Defensive Checks:**
   - Check `_activeMediaSession` di `onPlay`/`onPause` untuk log warning jika unexpected

3. **Thread Safety:**
   - Semua GeckoView callbacks run di main thread
   - Tidak perlu synchronization khusus

---

### 2.3 Layer 3: TabSessionManager Simplification

**File:** `feature/tabmanager/src/main/java/com/dwlhm/tabmanager/api/TabSessionManager.kt`

**Before (Phase 1):**
```kotlin
tabHandle.session.sessionCallback = object : BrowserSessionCallback {
    // Workaround fields
    private var _mediaSession: BrowserMediaSession? = null
    
    override fun onMediaActivated(mediaSession: BrowserMediaSession) {
        _mediaSession = mediaSession
        lastKnownMediaSession = mediaSession  // Another workaround
        eventDispatcher.dispatch(MediaActivatedEvent(tabHandle.id, mediaSession))
    }
    
    override fun onMediaStateChanged(state: BrowserMediaState) {
        // Complex fallback logic
        val session = _mediaSession 
            ?: lastKnownMediaSession 
            ?: return
        
        lastKnownMediaState = state
        eventDispatcher.dispatch(MediaStateChangedEvent(tabHandle.id, state, session))
    }
}
```

**After (Phase 2):**
```kotlin
tabHandle.session.sessionCallback = object : BrowserSessionCallback {
    // TIDAK ADA _mediaSession atau workaround fields!
    
    override fun onMediaActivated(mediaSession: BrowserMediaSession) {
        // Session sudah di-track di GeckoBrowserSession
        // Parameter mediaSession untuk backward compatibility
        eventDispatcher.dispatch(MediaActivatedEvent(tabHandle.id, mediaSession))
    }
    
    override fun onMediaDeactivated() {
        eventDispatcher.dispatch(MediaDeactivatedEvent(tabHandle.id))
    }
    
    override fun onMediaStateChanged(state: BrowserMediaState) {
        // SIMPLE: Langsung akses dari session
        val session = tabHandle.session.activeMediaSession ?: return
        
        // Keep untuk sync functionality
        lastKnownMediaState = state
        
        eventDispatcher.dispatch(MediaStateChangedEvent(tabHandle.id, state, session))
    }
    
    override fun onMediaMetadataChanged(metadata: BrowserMediaMetadata) {
        val session = tabHandle.session.activeMediaSession ?: return
        eventDispatcher.dispatch(MediaMetadataChangedEvent(tabHandle.id, metadata, session))
    }
}

// syncMediaStateFromNotification juga simplified
fun syncMediaStateFromNotification(tabId: String) {
    val currentTab = selectedTab.value ?: return
    if (currentTab.id != tabId) return
    
    // Langsung dari session - no workaround needed
    val session = currentTab.session.activeMediaSession ?: return
    val state = lastKnownMediaState ?: return
    
    eventDispatcher.dispatch(MediaStateChangedEvent(tabId, state, session))
}
```

**Perubahan:**
1. Hapus `lastKnownMediaSession` (tidak perlu lagi)
2. Hapus `_mediaSession` di callback
3. Keep `lastKnownMediaState` untuk sync functionality
4. Gunakan `tabHandle.session.activeMediaSession` sebagai source of truth

---

## 3. Data Flow Comparison

### 3.1 Phase 1 (Current) - onMediaStateChanged Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ GeckoView fires onPlay                                          │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│ TabSessionManager callback                                       │
│                                                                  │
│   1. Check _mediaSession (callback field)                        │
│      └── PROBLEM: Might be null if callback recreated!          │
│   2. Fallback to lastKnownMediaSession                          │
│      └── Workaround                                              │
│   3. Update lastKnownMediaState                                  │
│   4. Dispatch MediaStateChangedEvent                             │
│                                                                  │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│ EventDispatcher → MediaPlaybackManager → Service                 │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Phase 2 (Proposed) - onMediaStateChanged Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ GeckoView fires onPlay                                          │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│ TabSessionManager callback                                       │
│                                                                  │
│   1. Access tabHandle.session.activeMediaSession                 │
│      └── ALWAYS valid if media active (tracked at session)      │
│   2. Update lastKnownMediaState (for sync only)                  │
│   3. Dispatch MediaStateChangedEvent                             │
│                                                                  │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│ EventDispatcher → MediaPlaybackManager → Service                 │
└─────────────────────────────────────────────────────────────────┘
```

**Key Difference:**
- Phase 1: 2 lookups dengan fallback logic
- Phase 2: 1 lookup, guaranteed valid

---

## 4. Migration Checklist

### 4.1 Pre-Implementation

- [ ] Review current test coverage
- [ ] Identify all usages of `lastKnownMediaSession`
- [ ] Document current behavior for comparison

### 4.2 Implementation

**Step 1: Interface Update**
- [ ] Add `activeMediaSession` to `BrowserSession` interface
- [ ] Add KDoc documentation

**Step 2: GeckoBrowserSession**
- [ ] Add `_activeMediaSession` backing field
- [ ] Implement `activeMediaSession` property
- [ ] Update `onActivated` to set `_activeMediaSession`
- [ ] Update `onDeactivated` to clear `_activeMediaSession`
- [ ] Add defensive checks in `onPlay`/`onPause`

**Step 3: TabSessionManager**
- [ ] Remove `_mediaSession` from callback
- [ ] Remove `lastKnownMediaSession` field
- [ ] Update `onMediaStateChanged` to use `session.activeMediaSession`
- [ ] Update `onMediaMetadataChanged` similarly
- [ ] Update `syncMediaStateFromNotification`

### 4.3 Testing

- [ ] Unit tests for GeckoBrowserSession
- [ ] Integration tests for TabSessionManager
- [ ] Manual testing: Play/Pause from notification
- [ ] Manual testing: Return from notification
- [ ] Manual testing: Tab switching during playback

### 4.4 Cleanup

- [ ] Remove Phase 1 workaround code (setelah verified working)
- [ ] Update documentation

---

## 5. Rollback Plan

Jika Phase 2 menyebabkan issues:

1. **Quick Rollback:** Re-add workaround fields tanpa hapus session-level tracking
   ```kotlin
   val session = tabHandle.session.activeMediaSession
       ?: lastKnownMediaSession  // Temporary fallback
       ?: return
   ```

2. **Full Rollback:** Revert semua changes, gunakan Phase 1 workarounds

---

## 6. Success Metrics

Phase 2 dianggap sukses jika:

1. **Zero regressions:** Semua existing functionality tetap bekerja
2. **Bug resolved:** Notification sync bug tidak terjadi lagi
3. **Code simplified:** LOC reduced di TabSessionManager
4. **No workarounds:** `lastKnownMediaSession` dihapus sepenuhnya

---

## 7. FAQ

### Q: Mengapa tidak track `activeMediaState` juga di session level?

**A:** Kita bisa, tapi tidak perlu karena:
- State dibutuhkan hanya untuk sync (`syncMediaStateFromNotification`)
- Tracking state di callback cukup untuk use case ini
- Menambah state di session = tambah complexity

### Q: Bagaimana jika GeckoView callback pattern berubah?

**A:** Arsitektur ini isolated di `GeckoBrowserSession`. Jika GeckoView berubah, hanya perlu update satu file.

### Q: Apakah ini thread-safe?

**A:** Ya, karena:
- Semua GeckoView callbacks run di main thread
- `activeMediaSession` hanya diakses dari main thread
- Tidak ada concurrent access

---

## Referensi

- [ADR-004: Phase 2 - Session-Level Media State](../adr/004-phase2-session-level-media-state.md)
- [ADR-003: Media State Management Refactoring](../adr/003-media-state-management-refactoring.md)
- [GeckoView MediaSession API Documentation](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/)
