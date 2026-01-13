# ADR-002: Media Notification Architecture

| Status | Accepted |
|--------|----------|
| Tanggal | 2026-01-13 |
| Deciders | dwlhm |
| Kategori | Feature Architecture |
| Supersedes | - |

## Konteks

Start Browser mendukung background media playback (musik, video) dari web. Untuk memberikan kontrol kepada user saat app di background, dibutuhkan **media notification** dengan kontrol playback (play/pause/stop/next/previous).

### Arsitektur Sebelumnya

```
┌──────────────────────────────────────────────────────────────────┐
│                    MediaPlaybackService                           │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  companion object {                                        │  │
│  │      private var mediaSessionCallback: BrowserMediaSession?│  │ ← Static mutable state
│  │      private var sessionId: String?                        │  │
│  │      private var currentArtwork: Bitmap?                   │  │
│  │      fun setMediaSession(...)                              │  │
│  │      fun updateState(...)                                  │  │
│  │      fun updateMetadata(...)                               │  │
│  │  }                                                         │  │
│  │                                                            │  │
│  │  // Instance methods                                       │  │
│  │  private fun createNotificationChannel()                   │  │
│  │  private fun initMediaSession()                            │  │ ← Semua tanggung jawab
│  │  private fun updateMediaSessionMetadata()                  │  │   tercampur dalam
│  │  private fun updatePlaybackState()                         │  │   satu class
│  │  private fun buildNotification()                           │  │
│  │  private fun createPendingIntent()                         │  │
│  │  private fun updateNotification()                          │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

### Masalah yang Dihadapi

1. **Static Mutable State**
   - `mediaSessionCallback`, `sessionId`, `currentArtwork` di companion object
   - Menyebabkan memory leak jika service di-destroy tapi static reference masih ada
   - Race condition saat multiple media activation events
   - State bisa stale/outdated

2. **God Class Anti-Pattern**
   - `MediaPlaybackService` melakukan terlalu banyak hal:
     - Manage notification channel
     - Build notification UI
     - Handle media session callbacks
     - Track playback state
     - Handle intent actions
   - Sulit di-test, di-maintain, dan di-extend

3. **Duplikasi State**
   - `currentMediaSession` ada di Manager DAN Service
   - `currentState` (play/pause) ada di Manager DAN Service
   - Bisa out-of-sync dan menyebabkan bug subtle

4. **Intent-Based Communication yang Kompleks**
   - Banyak action constants yang harus diingat
   - Easy to miss atau typo
   - Tidak type-safe

5. **Unused Code**
   - `MediaEventListener` di module `feature/media` hanya logging
   - Tidak memberikan value apapun

## Keputusan

Melakukan **refactoring arsitektur** dengan prinsip:

1. **Single Responsibility Principle** - Setiap class satu tanggung jawab
2. **Immutable State** - State tidak bisa diubah in-place
3. **Unidirectional Data Flow** - Data mengalir satu arah
4. **Explicit Over Implicit** - Lebih baik verbose tapi jelas

### Arsitektur Baru

```
┌────────────────────────────────────────────────────────────────────────────┐
│                                                                            │
│   Browser Events (MediaActivated, StateChanged, MetadataChanged, etc.)     │
│                                                                            │
└──────────────────────────────────┬─────────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                         MediaPlaybackManager                               │
│  ─────────────────────────────────────────────────────────────────────────│
│  Tanggung Jawab:                                                           │
│  • Single Source of Truth untuk media playback                             │
│  • Listen events dari browser                                              │
│  • Manage service lifecycle (start/stop)                                   │
│  • Forward state changes ke service via Intent                             │
└──────────────────────────────────┬─────────────────────────────────────────┘
                                   │
                                   │ Intent (ACTION_INITIALIZE, UPDATE_STATE, etc.)
                                   ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                         MediaPlaybackService                               │
│  ─────────────────────────────────────────────────────────────────────────│
│  Tanggung Jawab:                                                           │
│  • Thin orchestrator - koordinasi komponen                                 │
│  • Handle Android service lifecycle                                        │
│  • Route intent actions ke handler yang sesuai                             │
│                                                                            │
│  State:                                                                    │
│  • currentState: MediaPlaybackState? (single source of truth di service)  │
│                                                                            │
│  Components:                                                               │
│  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐           │
│  │MediaNotification │ │MediaSession      │ │MediaPlaybackState│           │
│  │Builder           │ │Controller        │ │(immutable)       │           │
│  │                  │ │                  │ │                  │           │
│  │• Build notif     │ │• Manage          │ │• Hold state      │           │
│  │• Create actions  │ │  MediaSession    │ │• Provide helpers │           │
│  │• Create channel  │ │  Compat          │ │• Copy semantics  │           │
│  │                  │ │• Handle callbacks│ │                  │           │
│  └──────────────────┘ └──────────────────┘ └──────────────────┘           │
└────────────────────────────────────────────────────────────────────────────┘
```

### Komponen yang Dibuat

#### 1. MediaPlaybackState (Immutable State Holder)

```kotlin
data class MediaPlaybackState(
    val tabId: String,
    val mediaSession: BrowserMediaSession,
    val playbackState: BrowserMediaState = BrowserMediaState.PAUSE,
    val title: String = "Media sedang diputar",
    val artist: String = "Start Browser",
    val album: String? = null,
    val artwork: Bitmap? = null
)
```

**Mengapa data class?**
- Immutable by default (semua `val`)
- Auto-generate `copy()` untuk membuat instance baru
- Auto-generate `equals()`, `hashCode()`, `toString()`

**Mengapa immutable?**
- Tidak ada side effects
- Thread-safe tanpa synchronization
- Mudah di-reason tentang state
- Debugging lebih mudah (state tidak berubah diam-diam)

#### 2. MediaNotificationBuilder (Notification Builder)

```kotlin
class MediaNotificationBuilder(private val context: Context) {
    fun createNotificationChannel()
    fun buildNotification(state: MediaPlaybackState, ...): Notification
    fun updateNotification(notification: Notification)
}
```

**Mengapa class terpisah?**
- Single responsibility: hanya build notification
- Stateless: tidak menyimpan state, semua via parameter
- Reusable: bisa dipakai di context lain
- Testable: mudah di-mock dan di-test

#### 3. MediaSessionController (Media Session Handler)

```kotlin
class MediaSessionController(private val context: Context) {
    val sessionToken: MediaSessionCompat.Token?
    fun initialize(callback: MediaSessionCallback)
    fun updateMetadata(state: MediaPlaybackState)
    fun updatePlaybackState(browserState: BrowserMediaState)
    fun release()
}
```

**Mengapa interface callback?**
- Decoupling: Controller tidak tahu tentang Service
- Testable: bisa inject mock callback
- Flexible: callback bisa diubah tanpa ubah controller

#### 4. MediaPlaybackServiceBridge (Data Bridge)

```kotlin
object MediaPlaybackServiceBridge {
    fun setMediaSession(session: BrowserMediaSession?)
    fun getMediaSession(): BrowserMediaSession?
    fun setArtwork(artwork: Bitmap?)
    fun consumeArtwork(): Bitmap?
    fun clear()
}
```

**Mengapa masih ada static object?**
- `BrowserMediaSession` dan `Bitmap` tidak bisa di-serialize ke Intent
- Alternative (Binder) terlalu kompleks untuk use case ini
- Data langsung di-consume (null-kan setelah diambil) untuk minimize risk

**Mengapa "consume" pattern?**
- Data hanya bisa diambil sekali
- Menghindari stale reference
- Explicit lifecycle: set → get → clear

## Alternatif yang Dipertimbangkan

### Alternatif 1: Binder-Based Communication

```kotlin
class MediaPlaybackService : Service() {
    inner class MediaBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }
    
    override fun onBind(intent: Intent): IBinder = MediaBinder()
}

// Di Manager
val connection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val binder = service as MediaPlaybackService.MediaBinder
        mediaService = binder.getService()
    }
}
```

**Ditolak karena:**
- Kompleksitas tinggi (lifecycle ServiceConnection)
- Overkill untuk unidirectional communication
- Manager perlu handle bind/unbind lifecycle
- Race condition saat binding

### Alternatif 2: ViewModel Shared State

```kotlin
class MediaPlaybackViewModel : ViewModel() {
    val state = MutableStateFlow<MediaPlaybackState?>(null)
}

// Shared via Application atau Hilt
```

**Ditolak karena:**
- Service tidak bisa akses ViewModel secara natural
- Mixing UI pattern (ViewModel) dengan Service
- Lifecycle mismatch (ViewModel tied to UI, Service independent)

### Alternatif 3: Room Database untuk State

```kotlin
@Entity
data class MediaPlaybackEntity(
    @PrimaryKey val id: Int = 1,
    val tabId: String,
    val state: String,
    // ...
)
```

**Ditolak karena:**
- Overkill untuk transient state
- Database I/O overhead
- State ini tidak perlu persist across app restart

### Alternatif 4: Keep Monolith but Clean Up

```kotlin
class MediaPlaybackService : Service() {
    // Pindahkan static ke instance dengan lifecycle
    private var mediaSession: BrowserMediaSession? = null
    
    // Tetap satu class tapi organize methods
}
```

**Ditolak karena:**
- Masih violate Single Responsibility
- Class tetap besar dan hard to test
- Tidak solve fundamental design issues

## Konsekuensi

### Positif

1. **Maintainability**
   - Setiap class punya satu tanggung jawab jelas
   - Mudah dimodifikasi tanpa side effects
   - Code review lebih fokus

2. **Testability**
   - Components bisa di-test secara isolated
   - Mock dependencies mudah
   - State predictable (immutable)

3. **Debuggability**
   - Data flow jelas dan satu arah
   - State tidak berubah diam-diam
   - Logging lebih meaningful

4. **Scalability**
   - Mudah extend tanpa ubah existing code
   - Components bisa di-reuse
   - Pattern bisa diapply ke fitur lain

5. **Reduced Risk**
   - Tidak ada static mutable state yang long-lived
   - Memory leak risk diminimalisir
   - Race condition risk berkurang

### Negatif

1. **More Files**
   - 5 files baru vs 2 files sebelumnya
   - Perlu navigate lebih banyak file

2. **Learning Curve**
   - Developer baru perlu pahami arsitektur
   - More concepts to understand

3. **Still Has Bridge Object**
   - `MediaPlaybackServiceBridge` masih static
   - Trade-off antara simplicity dan purity

4. **Indirection**
   - Action dari user harus traverse: Notification → Service → State → MediaSession
   - More hops = potential more failure points

### Mitigasi

| Risk | Mitigation |
|------|------------|
| File navigation complex | IDE "Go to Definition", consistent naming |
| Learning curve | Documentation, design doc |
| Bridge object misuse | Consume pattern, explicit clear() |
| Indirection issues | Logging di setiap layer, comprehensive error handling |

## Implementasi

### Struktur File

```
app/src/main/java/com/dwlhm/startbrowser/services/
├── MediaPlaybackManager.kt          # Refactored - event handling & service control
├── MediaPlaybackService.kt          # Refactored - thin orchestrator
└── media/
    ├── MediaPlaybackState.kt        # NEW - immutable state
    ├── MediaNotificationBuilder.kt  # NEW - notification building
    └── MediaSessionController.kt    # NEW - media session management
```

### File yang Dihapus

```
feature/media/src/main/java/com/dwlhm/media/api/
└── MediaEventListener.kt            # DELETED - tidak berguna
```

### Diagram Alur Data

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            EVENT FLOW                                        │
└─────────────────────────────────────────────────────────────────────────────┘

    GeckoSession                    EventDispatcher                Manager
        │                               │                            │
        │ Media activated               │                            │
        ├──────────────────────────────>│                            │
        │                               │ MediaActivatedEvent        │
        │                               ├───────────────────────────>│
        │                               │                            │
        │                               │         Set bridge data    │
        │                               │         Start service      │
        │                               │                            │
        │                               │                            ▼
        │                               │                    ┌───────────────┐
        │                               │                    │   Service     │
        │                               │                    │   (started)   │
        │                               │                    └───────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           ACTION FLOW                                        │
└─────────────────────────────────────────────────────────────────────────────┘

    Notification              Service                Components           Browser
        │                        │                       │                   │
        │ User taps "Pause"      │                       │                   │
        ├───────────────────────>│                       │                   │
        │                        │                       │                   │
        │                        │ handlePauseAction()   │                   │
        │                        ├─────────────────────┐ │                   │
        │                        │                     │ │                   │
        │                        │ currentState        │ │                   │
        │                        │   ?.mediaSession    │ │                   │
        │                        │   ?.pause()         │ │                   │
        │                        │                     │ │                   │
        │                        │<────────────────────┘ │                   │
        │                        │                       │                   │
        │                        │                       │    pause()        │
        │                        │───────────────────────┼──────────────────>│
        │                        │                       │                   │
        │                        │                       │   Media paused    │
        │                        │                       │<──────────────────│
```

## Referensi

- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Single Responsibility Principle](https://en.wikipedia.org/wiki/Single-responsibility_principle)
- [Immutable Object Pattern](https://en.wikipedia.org/wiki/Immutable_object)
- [Android Foreground Services](https://developer.android.com/guide/components/foreground-services)
- [MediaSession Documentation](https://developer.android.com/guide/topics/media-apps/working-with-a-media-session)

## Changelog

| Tanggal | Perubahan |
|---------|-----------|
| 2026-01-13 | Initial implementation - complete refactoring |
