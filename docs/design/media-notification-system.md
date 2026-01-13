# Technical Design Document: Media Notification System

| Document Info | |
|---------------|---|
| Author | dwlhm |
| Created | 2026-01-13 |
| Status | Implemented |
| Related ADR | [ADR-002](../adr/002-media-notification-architecture.md) |

## 1. Overview

Dokumen ini menjelaskan detail teknis implementasi Media Notification System di Start Browser. Berbeda dengan ADR yang fokus pada *keputusan*, dokumen ini fokus pada *"mengapa"* di balik setiap pilihan implementasi.

### 1.1 Scope

Dokumen ini mencakup:
- Penjelasan setiap class dan fungsinya
- Alasan penamaan (naming conventions)
- Alasan pemilihan pattern
- Alasan struktur parameter dan return type
- Trade-offs yang dipilih

---

## 2. Class-by-Class Explanation

### 2.1 MediaPlaybackState

**Lokasi:** `app/.../services/media/MediaPlaybackState.kt`

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

#### 2.1.1 Mengapa `data class`?

Kotlin `data class` memberikan:

| Feature | Manual Implementation | data class |
|---------|----------------------|------------|
| `equals()` | ~10 lines | Auto |
| `hashCode()` | ~5 lines | Auto |
| `toString()` | ~5 lines | Auto |
| `copy()` | ~15 lines | Auto |
| Destructuring | Manual | Auto |

**Trade-off:** `data class` tidak bisa di-extend (final). Tapi untuk state holder, inheritance jarang diperlukan.

#### 2.1.2 Mengapa Nama `MediaPlaybackState`?

| Alternatif | Alasan Ditolak |
|------------|----------------|
| `MediaState` | Terlalu generic, bisa berarti state media di mana saja |
| `NotificationState` | Terlalu spesifik ke notification, padahal state ini juga untuk MediaSession |
| `MediaPlaybackData` | "Data" tidak menunjukkan bahwa ini representasi state saat ini |
| `CurrentMedia` | Tidak jelas ini adalah state object |

**`MediaPlaybackState`** dipilih karena:
- `Media` → domain (media playback)
- `Playback` → aktivitas (pemutaran)
- `State` → jenis object (representasi kondisi)

#### 2.1.3 Mengapa Default Values?

```kotlin
playbackState: BrowserMediaState = BrowserMediaState.PAUSE,
title: String = "Media sedang diputar",
artist: String = "Start Browser",
```

**Alasan:**
1. **Reduce boilerplate** - Caller tidak perlu specify semua field
2. **Safe defaults** - State awal yang aman (paused, generic text)
3. **Progressive enhancement** - State bisa di-update incrementally

**Mengapa `PAUSE` sebagai default?**
- Lebih aman daripada `PLAY` (tidak langsung consume resource)
- Sesuai dengan real-world behavior (media biasanya start paused)

#### 2.1.4 Helper Properties

```kotlin
val isPlaying: Boolean
    get() = playbackState == BrowserMediaState.PLAY

val isStopped: Boolean
    get() = playbackState == BrowserMediaState.STOP
```

**Mengapa computed property, bukan function?**

| Approach | Syntax | Semantic |
|----------|--------|----------|
| Property | `state.isPlaying` | "State ini adalah playing" |
| Function | `state.isPlaying()` | "Cek apakah state playing" |

Property lebih cocok untuk:
- Query tentang state (bukan aksi)
- Tidak ada side effect
- Hasil deterministic (selalu sama untuk input sama)

#### 2.1.5 Modifier Methods

```kotlin
fun withPlaybackState(newState: BrowserMediaState): MediaPlaybackState {
    return copy(playbackState = newState)
}

fun withMetadata(
    title: String?,
    artist: String?,
    album: String?,
    artwork: Bitmap?
): MediaPlaybackState {
    return copy(
        title = title ?: this.title,
        artist = artist ?: this.artist,
        album = album ?: this.album,
        artwork = artwork ?: this.artwork
    )
}
```

**Mengapa `withX()` pattern, bukan setter?**

```kotlin
// ❌ Mutable approach (BAD)
state.playbackState = newState  // Mengubah object asli

// ✅ Immutable approach (GOOD)
val newState = state.withPlaybackState(newState)  // Membuat object baru
```

**Naming convention `withX()`:**
- Common pattern di immutable objects (Java: `LocalDate.withMonth()`)
- Jelas bahwa ini return object baru
- Tidak menyiratkan mutation

**Mengapa `title ?: this.title` di `withMetadata`?**
- Nullable parameter = optional update
- Jika `null`, keep existing value
- Memungkinkan partial update

---

### 2.2 MediaNotificationBuilder

**Lokasi:** `app/.../services/media/MediaNotificationBuilder.kt`

```kotlin
class MediaNotificationBuilder(
    private val context: Context
)
```

#### 2.2.1 Mengapa Nama `MediaNotificationBuilder`?

| Alternatif | Alasan Ditolak |
|------------|----------------|
| `NotificationHelper` | "Helper" terlalu generic |
| `NotificationFactory` | Factory biasanya untuk object creation, bukan building complex objects |
| `NotificationManager` | Konflik dengan Android's `NotificationManager` |
| `MediaNotificationFactory` | Factory pattern kurang tepat karena banyak customization |

**`MediaNotificationBuilder`** dipilih karena:
- `Builder` pattern - membangun object kompleks step by step
- Jelas bahwa output adalah `Notification`
- Konsisten dengan Android convention (`NotificationCompat.Builder`)

#### 2.2.2 Mengapa Companion Object untuk Constants?

```kotlin
companion object {
    const val CHANNEL_ID = "media_playback_channel"
    const val NOTIFICATION_ID = 1001
    const val ACTION_PLAY = "com.dwlhm.startbrowser.ACTION_PLAY"
    // ...
}
```

**Alasan:**
1. **Single source of truth** - Constants defined once
2. **Accessible tanpa instance** - `MediaNotificationBuilder.CHANNEL_ID`
3. **Compile-time constant** - `const val` di-inline oleh compiler

**Mengapa prefix `com.dwlhm.startbrowser.`?**
- Android best practice untuk Intent actions
- Hindari collision dengan app lain
- Explicit ownership

#### 2.2.3 Method: `createNotificationChannel()`

```kotlin
fun createNotificationChannel() {
    val channel = NotificationChannel(
        CHANNEL_ID,
        "Media Playback",
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = "Kontrol pemutaran media browser"
        setShowBadge(false)
    }
    notificationManager.createNotificationChannel(channel)
}
```

**Mengapa `IMPORTANCE_LOW`?**

| Importance | Behavior |
|------------|----------|
| HIGH | Sound, heads-up notification |
| DEFAULT | Sound, status bar icon |
| LOW | No sound, status bar icon only |
| MIN | No sound, tidak muncul di status bar |

`LOW` dipilih karena:
- Media notification tidak perlu suara (media sudah bunyi)
- Tetap perlu visible di status bar untuk kontrol
- Tidak intrusive

**Mengapa `setShowBadge(false)`?**
- Badge di app icon tidak relevan untuk media playback
- Menghindari confusion ("Ada 1 apa?")

#### 2.2.4 Method: `buildNotification()`

```kotlin
fun buildNotification(
    state: MediaPlaybackState,
    mediaSessionToken: MediaSessionCompat.Token?,
    serviceClass: Class<*>
): Notification
```

**Mengapa parameter `serviceClass: Class<*>`?**

```kotlin
// Tanpa parameter - hardcoded dependency
private fun createPendingIntent(action: String): PendingIntent {
    val intent = Intent(context, MediaPlaybackService::class.java)  // ❌ Hardcoded
    // ...
}

// Dengan parameter - flexible
private fun createServicePendingIntent(action: String, serviceClass: Class<*>): PendingIntent {
    val intent = Intent(context, serviceClass)  // ✅ Configurable
    // ...
}
```

**Benefit:**
- Builder tidak coupled ke Service tertentu
- Testable dengan mock Service class
- Reusable jika ada service lain yang butuh notification

**Mengapa return `Notification`, bukan langsung `notify()`?**

```kotlin
// ❌ Side effect di builder
fun buildAndShowNotification(...) {
    val notification = build()
    notificationManager.notify(id, notification)
}

// ✅ Pure function - caller decides
fun buildNotification(...): Notification {
    return notification
}
```

**Alasan:**
- Builder hanya *build*, tidak *show*
- Caller bisa decide kapan show (e.g., `startForeground` vs `notify`)
- Lebih mudah di-test (verify output, bukan side effect)

#### 2.2.5 Inner Class: `NotificationActions`

```kotlin
data class NotificationActions(
    val previous: NotificationCompat.Action,
    val playPause: NotificationCompat.Action,
    val next: NotificationCompat.Action,
    val stop: NotificationCompat.Action
)
```

**Mengapa buat class terpisah?**

```kotlin
// ❌ Return multiple values tanpa container
private fun createActions(): List<NotificationCompat.Action>  // Order matters, error prone

// ❌ Return as Pair/Triple
private fun createActions(): Pair<Action, Pair<Action, Pair<Action, Action>>>  // Ugly

// ✅ Named container
private fun createActions(): NotificationActions  // Clear, type-safe
```

**Mengapa `data class`?**
- Auto equals/hashCode (useful for testing)
- Destructuring: `val (prev, pp, next, stop) = createActions()`

---

### 2.3 MediaSessionController

**Lokasi:** `app/.../services/media/MediaSessionController.kt`

```kotlin
class MediaSessionController(
    private val context: Context
)
```

#### 2.3.1 Mengapa Nama `MediaSessionController`?

| Alternatif | Alasan Ditolak |
|------------|----------------|
| `MediaSessionManager` | Konflik dengan Android's system service |
| `MediaSessionHandler` | "Handler" di Android punya meaning khusus (Looper/Handler) |
| `MediaSessionWrapper` | "Wrapper" menyiratkan thin layer, ini lebih dari itu |

**`MediaSessionController`** dipilih karena:
- `Controller` = manages lifecycle dan operations
- Jelas bahwa ini mengontrol `MediaSession`
- Pattern umum (MVC, MVVM controllers)

#### 2.3.2 Interface: `MediaSessionCallback`

```kotlin
interface MediaSessionCallback {
    fun onPlay()
    fun onPause()
    fun onStop()
    fun onSkipToNext()
    fun onSkipToPrevious()
}
```

**Mengapa interface, bukan direct callback ke Service?**

```kotlin
// ❌ Direct coupling
class MediaSessionController(
    private val service: MediaPlaybackService  // Tight coupling
) {
    private fun handlePlay() {
        service.handlePlayAction()  // Controller tahu tentang Service
    }
}

// ✅ Interface-based
class MediaSessionController(private val context: Context) {
    fun initialize(callback: MediaSessionCallback) {  // Loose coupling
        // ...
    }
}
```

**Benefit:**
- Controller tidak tahu siapa yang implement callback
- Testable: pass mock callback
- Flexible: bisa dipakai di context lain

**Mengapa nama method `onX()` bukan `handleX()`?**

| Naming | Semantic |
|--------|----------|
| `onPlay()` | Event notification: "Play event occurred" |
| `handlePlay()` | Command: "Handle the play action" |

`onX()` lebih tepat karena ini adalah callback interface (event-based).

#### 2.3.3 Method: `initialize()`

```kotlin
fun initialize(callback: MediaSessionCallback) {
    if (mediaSession != null) {
        release()
    }
    
    mediaSession = MediaSessionCompat(context, "StartBrowserMediaSession").apply {
        setCallback(createMediaSessionCallback(callback))
        isActive = true
    }
}
```

**Mengapa cek dan release existing session?**

```kotlin
if (mediaSession != null) {
    release()
}
```

**Alasan:**
- Defensive programming
- Jika `initialize()` dipanggil dua kali, session lama di-release dulu
- Mencegah memory leak dari orphaned session

**Mengapa `isActive = true`?**
- MediaSession harus active untuk menerima callbacks
- Android requirement untuk MediaStyle notification

#### 2.3.4 Method: `updatePlaybackState()`

```kotlin
fun updatePlaybackState(browserState: BrowserMediaState) {
    val session = mediaSession ?: return
    
    val state = mapToPlaybackState(browserState)
    
    val playbackState = PlaybackStateCompat.Builder()
        .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
        .setActions(SUPPORTED_ACTIONS)
        .build()
    
    session.setPlaybackState(playbackState)
}
```

**Mengapa `?: return` (early return)?**

```kotlin
// ❌ Nested if
fun updatePlaybackState(browserState: BrowserMediaState) {
    if (mediaSession != null) {
        val state = mapToPlaybackState(browserState)
        // ... nested code
    }
}

// ✅ Early return
fun updatePlaybackState(browserState: BrowserMediaState) {
    val session = mediaSession ?: return
    val state = mapToPlaybackState(browserState)
    // ... flat code
}
```

**Benefit:**
- Reduce nesting
- Clear precondition ("function needs session")
- Easier to read

**Mengapa `PLAYBACK_POSITION_UNKNOWN`?**
- Web media tidak selalu expose position
- Avoid incorrect position display
- MediaSession masih berfungsi tanpa position

#### 2.3.5 Private Method: `mapToPlaybackState()`

```kotlin
private fun mapToPlaybackState(state: BrowserMediaState): Int {
    return when (state) {
        BrowserMediaState.PLAY -> PlaybackStateCompat.STATE_PLAYING
        BrowserMediaState.PAUSE -> PlaybackStateCompat.STATE_PAUSED
        BrowserMediaState.STOP -> PlaybackStateCompat.STATE_STOPPED
    }
}
```

**Mengapa function terpisah?**

```kotlin
// ❌ Inline di updatePlaybackState
val state = when (browserState) {
    BrowserMediaState.PLAY -> PlaybackStateCompat.STATE_PLAYING
    // ...
}

// ✅ Separate function
val state = mapToPlaybackState(browserState)
```

**Alasan:**
- Separation of concerns
- Reusable jika ada method lain yang butuh mapping
- Easier to read (hide mapping details)
- Easier to test mapping logic

---

### 2.4 MediaPlaybackService

**Lokasi:** `app/.../services/MediaPlaybackService.kt`

```kotlin
class MediaPlaybackService : Service()
```

#### 2.4.1 Mengapa Extend `Service`, Bukan `MediaBrowserServiceCompat`?

| Class | Use Case |
|-------|----------|
| `Service` | General foreground service |
| `MediaBrowserServiceCompat` | Media player app dengan browsable content |

**`Service` dipilih karena:**
- Browser bukan dedicated media player
- Tidak perlu media browsing (tidak ada playlist dari app)
- Simpler API, less boilerplate

#### 2.4.2 State: Hanya `currentState`

```kotlin
private var currentState: MediaPlaybackState? = null
```

**Mengapa hanya satu state variable?**

```kotlin
// ❌ Multiple state variables (sebelumnya)
private var currentState: BrowserMediaState = BrowserMediaState.PAUSE
private var currentTitle: String? = null
private var currentArtist: String? = null
private var currentAlbum: String? = null
private var mediaSessionCallback: BrowserMediaSession? = null

// ✅ Single state object (sekarang)
private var currentState: MediaPlaybackState? = null
```

**Benefit:**
- Single source of truth
- State consistent (tidak bisa title berubah tapi artist tidak)
- Atomic updates
- Easier reasoning

#### 2.4.3 Method: `handleIntent()`

```kotlin
private fun handleIntent(intent: Intent) {
    when (intent.action) {
        ACTION_PLAY -> handlePlayAction()
        ACTION_PAUSE -> handlePauseAction()
        ACTION_STOP -> handleStopAction()
        ACTION_PREVIOUS -> handlePreviousAction()
        ACTION_NEXT -> handleNextAction()
        ACTION_INITIALIZE -> handleInitialize(intent)
        ACTION_UPDATE_STATE -> handleUpdateState(intent)
        ACTION_UPDATE_METADATA -> handleUpdateMetadata(intent)
    }
}
```

**Mengapa dispatcher pattern?**

```kotlin
// ❌ Long when in onStartCommand
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
        ACTION_PLAY -> {
            currentState?.mediaSession?.play()
        }
        ACTION_PAUSE -> {
            currentState?.mediaSession?.pause()
        }
        // ... banyak code
    }
}

// ✅ Dispatcher + handlers
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    intent?.let { handleIntent(it) }
    return START_NOT_STICKY
}

private fun handleIntent(intent: Intent) {
    when (intent.action) {
        ACTION_PLAY -> handlePlayAction()
        // ...
    }
}

private fun handlePlayAction() {
    currentState?.mediaSession?.play()
}
```

**Benefit:**
- `onStartCommand` tetap bersih
- Setiap handler bisa grow independently
- Easier to test individual handlers
- Clear action → handler mapping

#### 2.4.4 Mengapa `START_NOT_STICKY`?

```kotlin
return START_NOT_STICKY
```

| Flag | Behavior |
|------|----------|
| `START_STICKY` | Restart service jika killed, dengan null intent |
| `START_NOT_STICKY` | Jangan restart service jika killed |
| `START_REDELIVER_INTENT` | Restart service dengan last intent |

**`START_NOT_STICKY` dipilih karena:**
- Media playback state di browser bisa stale jika service di-restart
- Lebih baik user manually restart daripada inconsistent state
- Resource friendly (tidak restart jika tidak perlu)

---

### 2.5 MediaPlaybackServiceBridge

**Lokasi:** `app/.../services/MediaPlaybackService.kt` (bottom of file)

```kotlin
object MediaPlaybackServiceBridge {
    private var pendingMediaSession: BrowserMediaSession? = null
    private var pendingArtwork: Bitmap? = null
    // ...
}
```

#### 2.5.1 Mengapa `object` (Singleton)?

**Alasan:**
- Butuh shared state antara Manager dan Service
- `Intent` tidak bisa carry object references
- Thread-safe di Kotlin (single instance guaranteed)

**Trade-off accepted:**
- Global mutable state (biasanya dihindari)
- Tapi di-mitigate dengan consume pattern

#### 2.5.2 Naming: `pending` prefix

```kotlin
private var pendingMediaSession: BrowserMediaSession? = null
private var pendingArtwork: Bitmap? = null
```

**Mengapa `pending`?**
- Menyiratkan data sedang "menunggu" untuk di-consume
- Jelas lifecycle: set → pending → consumed

Alternatif yang ditolak:
- `current` → Bisa mislead (ini bukan current state)
- `temp` → Terlalu generic
- `queued` → Menyiratkan queue/list

#### 2.5.3 Method: `getMediaSession()` vs `consumeArtwork()`

```kotlin
fun getMediaSession(): BrowserMediaSession? {
    val session = pendingMediaSession
    pendingMediaSession = null
    return session
}

fun consumeArtwork(): Bitmap? {
    val artwork = pendingArtwork
    pendingArtwork = null
    return artwork
}
```

**Mengapa naming berbeda?**

| Method | Naming | Reason |
|--------|--------|--------|
| MediaSession | `get` | Sekali set, sekali get (1:1) |
| Artwork | `consume` | Explicitly menyatakan "ambil dan habiskan" |

**Note:** Bisa di-standardize ke `consume` semua untuk konsistensi.

#### 2.5.4 Method: `clear()`

```kotlin
fun clear() {
    pendingMediaSession = null
    pendingArtwork = null
}
```

**Mengapa explicit clear method?**
- Defensive cleanup
- Dipanggil saat deactivation untuk ensure no leaked references
- Explicit lebih baik daripada rely on GC

---

### 2.6 MediaPlaybackManager

**Lokasi:** `app/.../services/MediaPlaybackManager.kt`

```kotlin
class MediaPlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope
)
```

#### 2.6.1 Mengapa Constructor Injection?

```kotlin
// ❌ Create dependencies internally
class MediaPlaybackManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)  // Hardcoded
}

// ✅ Inject dependencies
class MediaPlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope  // Injected
)
```

**Benefit:**
- Testable: inject TestCoroutineScope
- Flexible: caller controls scope lifecycle
- Clear dependencies

#### 2.6.2 State Variables

```kotlin
private var isServiceRunning = false
private var activeMediaTabId: String? = null
private var currentMediaSession: BrowserMediaSession? = null
```

**Mengapa separate variables, bukan single state object?**

Di Manager, state ini untuk **tracking internal**, bukan untuk **representation**:
- `isServiceRunning` → flag boolean sederhana
- `activeMediaTabId` → untuk compare dengan event
- `currentMediaSession` → reference untuk bridge

**Berbeda dengan Service** yang butuh single state object untuk notification building.

#### 2.6.3 Event Observation Pattern

```kotlin
private fun observeMediaActivation() {
    val job = scope.launch {
        EventDispatcher.events
            .filterIsInstance<MediaActivatedEvent>()
            .collect { event -> handleMediaActivated(event) }
    }
    eventJobs.add(job)
}
```

**Mengapa pattern ini?**

1. **Separate observe methods** → Clear responsibility
2. **Track jobs in list** → Easy cleanup
3. **Handler methods** → Separate concerns

**Mengapa `filterIsInstance<T>()`?**

```kotlin
// ❌ Manual type check
EventDispatcher.events.collect { event ->
    when (event) {
        is MediaActivatedEvent -> handleMediaActivated(event)
        // ... banyak when branches
    }
}

// ✅ Filter by type
EventDispatcher.events
    .filterIsInstance<MediaActivatedEvent>()
    .collect { event -> handleMediaActivated(event) }
```

**Benefit:**
- Type-safe (event sudah di-cast)
- Clear: observer ini hanya untuk event type ini
- Performance: Flow difilter di upstream

#### 2.6.4 Method: `handleMediaStateChanged()`

```kotlin
private fun handleMediaStateChanged(event: MediaStateChangedEvent) {
    when (event.state) {
        BrowserMediaState.PLAY -> {
            if (!isServiceRunning) {
                // Start service first
            }
            updateServiceState(event.state)
        }
        BrowserMediaState.PAUSE -> {
            if (isServiceRunning) {
                updateServiceState(event.state)
            }
        }
        BrowserMediaState.STOP -> {
            if (event.tabId == activeMediaTabId) {
                stopService()
                clearState()
            }
        }
    }
}
```

**Mengapa conditional logic berbeda per state?**

| State | Logic | Reason |
|-------|-------|--------|
| PLAY | Start if needed, then update | Service harus running untuk play |
| PAUSE | Update only if running | Tidak start service hanya untuk pause |
| STOP | Stop service | Media ended, cleanup |

**Mengapa check `event.tabId == activeMediaTabId` di STOP?**
- Multiple tabs bisa punya media
- Hanya stop jika yang stop adalah active tab
- Prevent stopping service dari stale event

---

## 3. Naming Conventions Summary

| Convention | Example | Rationale |
|------------|---------|-----------|
| `withX()` for immutable updates | `withPlaybackState()` | Common Kotlin/Java pattern for immutable |
| `onX()` for callbacks | `onPlay()` | Event notification semantic |
| `handleX()` for action handlers | `handlePlayAction()` | Command handling semantic |
| `createX()` for factory methods | `createPendingIntent()` | Object creation semantic |
| `update` prefix for mutations | `updateNotification()` | Modify existing state |
| `pending` for bridge data | `pendingMediaSession` | Data waiting to be consumed |
| `current` for active state | `currentState` | Currently active value |
| `-Builder` suffix | `MediaNotificationBuilder` | Builder pattern |
| `-Controller` suffix | `MediaSessionController` | Control/manage lifecycle |
| `-Manager` suffix | `MediaPlaybackManager` | Orchestrate multiple concerns |
| `-State` suffix | `MediaPlaybackState` | State representation |
| `-Bridge` suffix | `MediaPlaybackServiceBridge` | Connect two components |

---

## 4. Pattern Decisions

### 4.1 Immutable State Pattern

**Applied in:** `MediaPlaybackState`

```kotlin
// Create new instance instead of mutating
val newState = currentState.withPlaybackState(BrowserMediaState.PLAY)
```

**Why:**
- Predictable state changes
- No race conditions
- Easy debugging (state doesn't change unexpectedly)

### 4.2 Builder Pattern

**Applied in:** `MediaNotificationBuilder`

```kotlin
// Build complex object step by step
val notification = builder.buildNotification(state, token, serviceClass)
```

**Why:**
- Notification has many optional parts
- Cleaner than constructor with 10+ parameters
- Separate construction from representation

### 4.3 Strategy Pattern (via Interface)

**Applied in:** `MediaSessionController.MediaSessionCallback`

```kotlin
interface MediaSessionCallback {
    fun onPlay()
    fun onPause()
    // ...
}
```

**Why:**
- Controller doesn't know implementation
- Easy to swap implementations
- Testable with mocks

### 4.4 Consume Pattern

**Applied in:** `MediaPlaybackServiceBridge`

```kotlin
fun consumeArtwork(): Bitmap? {
    val artwork = pendingArtwork
    pendingArtwork = null  // Consume (clear after get)
    return artwork
}
```

**Why:**
- Ensure data is used only once
- Prevent stale references
- Explicit lifecycle

### 4.5 Dispatcher Pattern

**Applied in:** `MediaPlaybackService.handleIntent()`

```kotlin
when (intent.action) {
    ACTION_PLAY -> handlePlayAction()
    ACTION_PAUSE -> handlePauseAction()
    // ...
}
```

**Why:**
- Clean routing logic
- Each handler is independent
- Easy to add new actions

---

## 5. File Organization

```
services/
├── MediaPlaybackManager.kt     # Orchestrator - coordinates flow
├── MediaPlaybackService.kt     # Android Service + Bridge object
└── media/                      # Supporting components
    ├── MediaPlaybackState.kt   # State model
    ├── MediaNotificationBuilder.kt  # Notification building
    └── MediaSessionController.kt    # MediaSession management
```

**Why this structure?**

| Location | Content | Reason |
|----------|---------|--------|
| `services/` | Manager, Service | Entry points |
| `services/media/` | Supporting classes | Domain-specific utilities |

**Why subfolder `media/`?**
- Group related classes
- Avoid cluttering `services/` folder
- Clear domain boundary

---

## 6. Trade-offs Accepted

| Trade-off | Choice | Alternative | Reason |
|-----------|--------|-------------|--------|
| Static Bridge | `object MediaPlaybackServiceBridge` | Binder-based | Simpler for our use case |
| More files | 5 files | 1 monolith | Better organization |
| Indirection | Manager → Service → Components | Direct | Separation of concerns |
| Nullable state | `currentState: MediaPlaybackState?` | Always non-null | Service may not have state initially |

---

## 7. Appendix: Code Metrics

### Before Refactoring

| Metric | Value |
|--------|-------|
| Files | 2 |
| Total lines | ~450 |
| Largest class | ~330 lines (MediaPlaybackService) |
| Static mutable vars | 3 |
| Responsibilities per class | 5+ |

### After Refactoring

| Metric | Value |
|--------|-------|
| Files | 5 |
| Total lines | ~550 |
| Largest class | ~180 lines (MediaPlaybackService) |
| Static mutable vars | 2 (in Bridge, with consume pattern) |
| Responsibilities per class | 1-2 |

**Analysis:**
- More code (+100 lines) but better organized
- Largest class reduced by 45%
- Clear single responsibilities
