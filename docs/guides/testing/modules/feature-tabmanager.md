# Testing: Feature Tab Manager

## Overview

| Info | Value |
|------|-------|
| Path | `feature/tabmanager/src/main/java/com/dwlhm/tabmanager/` |
| Priority | 🔴 Critical |
| Phase | 3 (Feature Layer) |
| Est. Time | 6-7 jam |
| Total Tests | 26 |

---

## Files to Test

| File | Test Class | Tests | Priority | Status |
|------|------------|-------|----------|--------|
| `api/TabInfo.kt` | `TabInfoTest` | 3 | 🟡 | ⬜ |
| `api/TabHandle.kt` | `TabHandleTest` | 3 | 🟡 | ⬜ |
| `api/TabMode.kt` | `TabModeTest` | 2 | 🟢 | ⬜ |
| `api/TabSessionManager.kt` | `TabSessionManagerTest` | 10 | 🔴 | ⬜ |
| `api/DefaultTabManager.kt` | `DefaultTabManagerTest` | 8 | 🟠 | ⬜ |

---

## Test Cases

### TabInfoTest

**Difficulty:** ⭐ Easy
**Est. Time:** 30 menit

```kotlin
// File: feature/tabmanager/src/test/java/com/dwlhm/tabmanager/api/TabInfoTest.kt

class TabInfoTest {
```

- [ ] `should hold id title url correctly`
  ```kotlin
  @Test
  fun `should hold id title url correctly`() {
      val tabInfo = TabInfo(
          id = "tab-1",
          title = "Example",
          url = "https://example.com"
      )
      
      assertEquals("tab-1", tabInfo.id)
      assertEquals("Example", tabInfo.title)
      assertEquals("https://example.com", tabInfo.url)
  }
  ```

- [ ] `default values should be set correctly`
- [ ] `copy should work correctly`

---

### TabHandleTest

**Difficulty:** ⭐ Easy
**Est. Time:** 30 menit

```kotlin
// File: feature/tabmanager/src/test/java/com/dwlhm/tabmanager/api/TabHandleTest.kt

class TabHandleTest {
```

- [ ] `should hold id and session`
- [ ] `equality should be based on id`
- [ ] `session should be accessible`

---

### TabModeTest

**Difficulty:** ⭐ Easy
**Est. Time:** 15 menit

```kotlin
class TabModeTest {
```

- [ ] `should have SINGLE and MULTI values`
- [ ] `values should be distinguishable`

---

### TabSessionManagerTest

**Difficulty:** ⭐⭐⭐ Hard (Complex state management)
**Est. Time:** 3-4 jam

```kotlin
// File: feature/tabmanager/src/test/java/com/dwlhm/tabmanager/api/TabSessionManagerTest.kt

@OptIn(ExperimentalCoroutinesApi::class)
class TabSessionManagerTest {
    private val testScope = TestScope()
    private val mockEventDispatcher = mockk<EventDispatcher>(relaxed = true)
    
    private lateinit var manager: TabSessionManager
    
    @BeforeEach
    fun setup() {
        manager = TabSessionManager(mockEventDispatcher, testScope)
    }
```

#### Tab Observation

- [ ] `selectedTab should emit current tab`
  ```kotlin
  @Test
  fun `selectedTab should emit current tab`() = testScope.runTest {
      val tab = createMockTabHandle("tab-1")
      
      manager.setSelectedTab(tab)
      
      val selected = manager.selectedTab.first()
      assertEquals("tab-1", selected?.id)
  }
  ```

- [ ] `changing tab should update selectedTab`
- [ ] `null tab should clear selection`

#### Media Events (Critical for sync bug)

- [ ] `onMediaActivated should dispatch MediaActivatedEvent`
  ```kotlin
  @Test
  fun `onMediaActivated should dispatch MediaActivatedEvent`() = testScope.runTest {
      val tab = createMockTabHandle("tab-1")
      val session = mockk<BrowserMediaSession>()
      
      manager.setSelectedTab(tab)
      
      // Simulate callback
      tab.session.sessionCallback?.onMediaActivated(session)
      
      verify {
          mockEventDispatcher.dispatch(match { event ->
              event is MediaActivatedEvent && event.tabId == "tab-1"
          })
      }
  }
  ```

- [ ] `onMediaDeactivated should dispatch MediaDeactivatedEvent`
- [ ] `onMediaStateChanged should dispatch with correct session`
- [ ] ⚠️ `onMediaStateChanged with null session should use fallback` **Phase 1 Fix**
  ```kotlin
  @Test
  fun `onMediaStateChanged with null session should use fallback`() = testScope.runTest {
      // This tests the Phase 1 fix where lastKnownMediaSession is used
      // when _mediaSession in callback is null
      
      val tab = createMockTabHandle("tab-1")
      val session = mockk<BrowserMediaSession>()
      
      manager.setSelectedTab(tab)
      
      // First, activate to set lastKnownMediaSession
      tab.session.sessionCallback?.onMediaActivated(session)
      
      // Simulate callback reset (new callback instance)
      // In real scenario, this happens when tab re-attaches
      
      // State change should still work using fallback
      tab.session.sessionCallback?.onMediaStateChanged(BrowserMediaState.PLAY)
      
      verify {
          mockEventDispatcher.dispatch(match { event ->
              event is MediaStateChangedEvent && 
              event.state == BrowserMediaState.PLAY
          })
      }
  }
  ```

#### Sync Functionality

- [ ] `syncMediaStateFromNotification should dispatch event`
- [ ] `syncMediaStateFromNotification for wrong tab should do nothing`
- [ ] `syncMediaStateFromNotification with no media should do nothing`

---

### DefaultTabManagerTest

**Difficulty:** ⭐⭐ Medium
**Est. Time:** 2-3 jam

```kotlin
// File: feature/tabmanager/src/test/java/com/dwlhm/tabmanager/api/DefaultTabManagerTest.kt

class DefaultTabManagerTest {
    private lateinit var tabManager: DefaultTabManager
    
    @BeforeEach
    fun setup() {
        tabManager = DefaultTabManager(/* dependencies */)
    }
```

- [ ] `createTab should return new TabHandle`
  ```kotlin
  @Test
  fun `createTab should return new TabHandle`() {
      val tab = tabManager.createTab("https://example.com")
      
      assertNotNull(tab)
      assertNotNull(tab.id)
  }
  ```

- [ ] `closeTab should remove tab`
- [ ] `getTab should return correct tab`
- [ ] `allTabs should return all tabs`
- [ ] `selectTab should update current tab`
- [ ] `closeAllTabs should clear all`
- [ ] `tab count should be accurate`
- [ ] `creating tab when at limit should handle gracefully`

---

## Notes

### Why TabSessionManager is Critical

`TabSessionManager` adalah jembatan antara:
- GeckoView callbacks
- Event system
- MediaPlaybackManager

Bug sync notification terjadi di sini. Tests dengan ⚠️ adalah regression tests untuk bug tersebut.

### Testing Callbacks

Untuk test callback behavior:

```kotlin
// Create mock tab dengan accessible callback
fun createMockTabHandle(id: String): TabHandle {
    val mockSession = mockk<BrowserSession>(relaxed = true)
    var callback: BrowserSessionCallback? = null
    
    every { mockSession.sessionCallback = any() } answers {
        callback = firstArg()
    }
    every { mockSession.sessionCallback } answers { callback }
    
    return TabHandle(id, mockSession)
}
```

### Related Documents

- [ADR-003](../../adr/003-media-state-management-refactoring.md) - Sync bug context
- [ADR-004](../../adr/004-phase2-session-level-media-state.md) - Phase 2 changes

---

## Progress

| Test Class | Done | Total | % |
|------------|------|-------|---|
| TabInfoTest | 0 | 3 | 0% |
| TabHandleTest | 0 | 3 | 0% |
| TabModeTest | 0 | 2 | 0% |
| TabSessionManagerTest | 0 | 10 | 0% |
| DefaultTabManagerTest | 0 | 8 | 0% |
| **Total** | **0** | **26** | **0%** |
