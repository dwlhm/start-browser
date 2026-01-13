# Testing: App Media Services

## Overview

| Info | Value |
|------|-------|
| Path | `app/src/main/java/com/dwlhm/startbrowser/services/` |
| Priority | 🔴 Critical |
| Phase | 1-2 (Foundation + Core Business) |
| Est. Time | 8-10 jam total |
| Total Tests | 35 |

---

## Files to Test

| File | Test Class | Tests | Priority | Status |
|------|------------|-------|----------|--------|
| `media/MediaPlaybackState.kt` | `MediaPlaybackStateTest` | 8 | 🔴 | ⬜ |
| `media/MediaNotificationBuilder.kt` | `MediaNotificationBuilderTest` | 8 | 🔴 | ⬜ |
| `media/MediaSessionController.kt` | `MediaSessionControllerTest` | 7 | 🔴 | ⬜ |
| `MediaPlaybackManager.kt` | `MediaPlaybackManagerTest` | 12 | 🔴 | ⬜ |

---

## Test Cases

### MediaPlaybackStateTest

**Difficulty:** ⭐ Easy (Pure data class, no mocking)
**Est. Time:** 1-2 jam
**Start Here!** ← Recommended first test

```kotlin
// File: app/src/test/java/com/dwlhm/startbrowser/services/media/MediaPlaybackStateTest.kt

class MediaPlaybackStateTest {
    // Fixtures
    private val testTabId = "test-tab-123"
    private val mockMediaSession = mockk<BrowserMediaSession>()
```

#### Constructor Tests

- [ ] `when created with minimal params then defaults should be set`
  ```kotlin
  @Test
  fun `when created with minimal params then defaults should be set`() {
      val state = MediaPlaybackState(
          tabId = testTabId,
          mediaSession = mockMediaSession
      )
      
      assertEquals(testTabId, state.tabId)
      assertEquals(BrowserMediaState.PAUSE, state.playbackState)
      assertNull(state.title)
  }
  ```

- [ ] `when created with all params then all values should be set`

#### withPlaybackState Tests

- [ ] `withPlaybackState should return new instance with updated state`
- [ ] `withPlaybackState should not modify original (immutability)`
- [ ] `withPlaybackState should preserve other properties`

#### withMetadata Tests

- [ ] `withMetadata should update all metadata fields`
- [ ] `withMetadata should preserve playback state`
- [ ] `withMetadata from BrowserMediaMetadata should work correctly`

---

### MediaNotificationBuilderTest

**Difficulty:** ⭐⭐ Medium (Requires mocking Context)
**Est. Time:** 2-3 jam

```kotlin
// File: app/src/test/java/com/dwlhm/startbrowser/services/media/MediaNotificationBuilderTest.kt

class MediaNotificationBuilderTest {
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockNotificationManager = mockk<NotificationManager>(relaxed = true)
    
    private lateinit var builder: MediaNotificationBuilder
    
    @BeforeEach
    fun setup() {
        every { mockContext.getSystemService(Context.NOTIFICATION_SERVICE) } returns mockNotificationManager
        builder = MediaNotificationBuilder(mockContext)
    }
```

#### Notification Building

- [ ] `buildNotification with PLAY state should have pause action`
  ```kotlin
  @Test
  fun `buildNotification with PLAY state should have pause action`() {
      val state = createTestState(playbackState = BrowserMediaState.PLAY)
      
      val notification = builder.buildNotification(state)
      
      // Verify pause action exists
      val actions = notification.actions
      assertTrue(actions.any { it.title.toString().contains("Pause", ignoreCase = true) })
  }
  ```

- [ ] `buildNotification with PAUSE state should have play action`
- [ ] `notification should have correct channel ID`
- [ ] `notification should include title when available`
- [ ] `notification should include artist when available`
- [ ] `notification should work without metadata (fallback text)`
- [ ] `notification should have correct content intent`
- [ ] `notification should have stop action`

---

### MediaSessionControllerTest

**Difficulty:** ⭐⭐ Medium (Requires mocking MediaSessionCompat)
**Est. Time:** 2-3 jam

```kotlin
// File: app/src/test/java/com/dwlhm/startbrowser/services/media/MediaSessionControllerTest.kt

class MediaSessionControllerTest {
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockMediaSession = mockk<MediaSessionCompat>(relaxed = true)
    
    private lateinit var controller: MediaSessionController
```

#### Initialization

- [ ] `initialize should create MediaSession`
- [ ] `initialize should set callback`

#### State Updates

- [ ] `updateState PLAY should set STATE_PLAYING`
  ```kotlin
  @Test
  fun `updateState PLAY should set STATE_PLAYING`() {
      controller.updateState(BrowserMediaState.PLAY)
      
      verify {
          mockMediaSession.setPlaybackState(match { 
              it.state == PlaybackStateCompat.STATE_PLAYING 
          })
      }
  }
  ```

- [ ] `updateState PAUSE should set STATE_PAUSED`
- [ ] `updateMetadata should update MediaSession metadata`

#### Callback Actions

- [ ] `callback onPlay should trigger play on browser session`
- [ ] `callback onPause should trigger pause on browser session`

#### Cleanup

- [ ] `release should release MediaSession`

---

### MediaPlaybackManagerTest

**Difficulty:** ⭐⭐⭐ Hard (Requires coroutine testing, complex mocking)
**Est. Time:** 3-4 jam

```kotlin
// File: app/src/test/java/com/dwlhm/startbrowser/services/MediaPlaybackManagerTest.kt

@OptIn(ExperimentalCoroutinesApi::class)
class MediaPlaybackManagerTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockEventDispatcher = mockk<EventDispatcher>()
    
    private lateinit var manager: MediaPlaybackManager
    
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Setup event flow
        every { mockEventDispatcher.events } returns MutableSharedFlow()
        
        manager = MediaPlaybackManager(mockContext, mockEventDispatcher, testScope)
    }
    
    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }
```

#### Activation/Deactivation

- [ ] `MediaActivatedEvent should start tracking tabId`
- [ ] `MediaDeactivatedEvent should trigger debounce timer`
- [ ] ⚠️ `rapid deactivate-activate should cancel debounce` **CRITICAL**
  ```kotlin
  @Test
  fun `rapid deactivate-activate should cancel debounce`() = testScope.runTest {
      // Arrange
      val tabId = "tab-1"
      val session = mockk<BrowserMediaSession>()
      
      // Act - Activate
      manager.handleEvent(MediaActivatedEvent(tabId, session))
      
      // Act - Deactivate
      manager.handleEvent(MediaDeactivatedEvent(tabId))
      
      // Act - Quickly reactivate (before debounce completes)
      advanceTimeBy(100) // Less than DEACTIVATION_DELAY_MS (300)
      manager.handleEvent(MediaActivatedEvent(tabId, session))
      
      // Assert - Service should still be running
      assertTrue(manager.isServiceRunning)
  }
  ```

- [ ] `MediaDeactivatedEvent for different tab should be ignored`

#### State Changes

- [ ] `MediaStateChanged PLAY should cancel pending deactivation`
- [ ] `MediaStateChanged should update service`
- [ ] ⚠️ `first state change should start service with correct state` **CRITICAL**
- [ ] `state change for inactive tab should be ignored`

#### Service Lifecycle

- [ ] `should start foreground service on first media`
- [ ] `should stop service after debounce timeout completes`
- [ ] `destroy should cancel all jobs`
- [ ] `destroy should stop service if running`

---

## Notes

### Why This Module is Critical

1. **User-facing:** Notification adalah fitur yang langsung dilihat user
2. **Bug history:** Ada beberapa bug yang sudah di-fix (lihat ADR-003)
3. **Complex state:** Race conditions dan timing issues

### Testing Tips

1. **Start with MediaPlaybackState** - Paling mudah, pure data class
2. **Use relaxed mocks** untuk Context - `mockk<Context>(relaxed = true)`
3. **Use TestScope** untuk coroutine testing
4. **Mark critical tests** - Tests dengan ⚠️ adalah regression tests

### Related Documents

- [ADR-003: Media State Management](../../adr/003-media-state-management-refactoring.md)
- [Design: Media State Sync](../../design/media-state-synchronization.md)

---

## Progress

| Test Class | Done | Total | % |
|------------|------|-------|---|
| MediaPlaybackStateTest | 0 | 8 | 0% |
| MediaNotificationBuilderTest | 0 | 8 | 0% |
| MediaSessionControllerTest | 0 | 7 | 0% |
| MediaPlaybackManagerTest | 0 | 12 | 0% |
| **Total** | **0** | **35** | **0%** |
