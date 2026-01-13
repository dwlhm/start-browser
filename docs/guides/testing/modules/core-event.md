# Testing: Core Event System

## Overview

| Info | Value |
|------|-------|
| Path | `core/event/src/main/java/com/dwlhm/event/` |
| Priority | 🔴 Critical |
| Phase | 1 (Foundation) |
| Est. Time | 3-4 jam |
| Total Tests | 13 |

---

## Files to Test

| File | Test Class | Tests | Priority | Status |
|------|------------|-------|----------|--------|
| `Event.kt` | `EventTest` | 3 | 🟡 | ⬜ |
| `EventDispatcher.kt` | `EventDispatcherTest` | 6 | 🔴 | ⬜ |
| `EventCollector.kt` | `EventCollectorTest` | 4 | 🔴 | ⬜ |

---

## Test Cases

### EventTest

**Difficulty:** ⭐ Easy
**Est. Time:** 30 menit

Test untuk concrete event classes yang ada di `Event.kt`.

```kotlin
// File: core/event/src/test/java/com/dwlhm/event/EventTest.kt

class MediaActivatedEventTest {
```

- [ ] `should hold tabId and mediaSession`
  ```kotlin
  @Test
  fun `should hold tabId and mediaSession`() {
      val session = mockk<BrowserMediaSession>()
      val event = MediaActivatedEvent("tab-1", session)
      
      assertEquals("tab-1", event.tabId)
      assertEquals(session, event.mediaSession)
  }
  ```

- [ ] `MediaStateChangedEvent should hold state`
- [ ] `MediaDeactivatedEvent should hold tabId`

---

### EventDispatcherTest

**Difficulty:** ⭐⭐ Medium (Coroutine + Flow testing)
**Est. Time:** 2 jam

```kotlin
// File: core/event/src/test/java/com/dwlhm/event/EventDispatcherTest.kt

@OptIn(ExperimentalCoroutinesApi::class)
class EventDispatcherTest {
    private val testScope = TestScope()
    private lateinit var dispatcher: EventDispatcher
    
    @BeforeEach
    fun setup() {
        dispatcher = EventDispatcher()
    }
```

- [ ] `dispatch should emit event to flow`
  ```kotlin
  @Test
  fun `dispatch should emit event to flow`() = testScope.runTest {
      val events = mutableListOf<Event>()
      
      // Collect in background
      val job = launch {
          dispatcher.events.collect { events.add(it) }
      }
      
      // Dispatch
      dispatcher.dispatch(MediaActivatedEvent("tab-1", mockk()))
      
      // Let it process
      advanceUntilIdle()
      
      assertEquals(1, events.size)
      assertTrue(events[0] is MediaActivatedEvent)
      
      job.cancel()
  }
  ```

- [ ] `multiple collectors should receive same event`
- [ ] `filterIsInstance should only pass matching types`
  ```kotlin
  @Test
  fun `filterIsInstance should only pass matching types`() = testScope.runTest {
      val stateEvents = mutableListOf<MediaStateChangedEvent>()
      
      val job = launch {
          dispatcher.events
              .filterIsInstance<MediaStateChangedEvent>()
              .collect { stateEvents.add(it) }
      }
      
      // Dispatch different event types
      dispatcher.dispatch(MediaActivatedEvent("tab-1", mockk()))
      dispatcher.dispatch(MediaStateChangedEvent("tab-1", BrowserMediaState.PLAY, mockk()))
      dispatcher.dispatch(MediaDeactivatedEvent("tab-1"))
      
      advanceUntilIdle()
      
      // Only state changed events
      assertEquals(1, stateEvents.size)
      
      job.cancel()
  }
  ```

- [ ] `dispatch should not block caller`
- [ ] `events should be received in order`
- [ ] `collector cancellation should stop receiving`

---

### EventCollectorTest

**Difficulty:** ⭐⭐ Medium
**Est. Time:** 1 jam

```kotlin
// File: core/event/src/test/java/com/dwlhm/event/EventCollectorTest.kt

class EventCollectorTest {
```

- [ ] `collect should receive dispatched events`
- [ ] `launchIn should work with provided scope`
- [ ] `cancellation should stop collection`
- [ ] `should handle rapid events without dropping`

---

## Notes

### Why Event System is Critical

Event system adalah **backbone** dari arsitektur:
- Media events flow melalui EventDispatcher
- TabSessionManager dispatch events
- MediaPlaybackManager observe events

Jika event system bermasalah, semua fitur yang bergantung padanya akan error.

### Testing Tools

```kotlin
// Turbine untuk Flow testing (lebih mudah)
testImplementation("app.cash.turbine:turbine:1.0.0")

// Contoh dengan Turbine
@Test
fun `dispatch should emit event`() = runTest {
    dispatcher.events.test {
        dispatcher.dispatch(MediaActivatedEvent("tab-1", mockk()))
        
        val event = awaitItem()
        assertTrue(event is MediaActivatedEvent)
        
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## Progress

| Test Class | Done | Total | % |
|------------|------|-------|---|
| EventTest | 0 | 3 | 0% |
| EventDispatcherTest | 0 | 6 | 0% |
| EventCollectorTest | 0 | 4 | 0% |
| **Total** | **0** | **13** | **0%** |
