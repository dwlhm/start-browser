# Testing: Engine Gecko

## Overview

| Info | Value |
|------|-------|
| Path | `engine/gecko/src/main/java/com/dwlhm/gecko/` |
| Priority | 🟠 High |
| Phase | 4 (Engine Layer) |
| Est. Time | 4-5 jam |
| Total Tests | 14 |

---

## Files to Test

| File | Test Class | Tests | Priority | Status |
|------|------------|-------|----------|--------|
| `api/GeckoBrowserSession.kt` | `GeckoBrowserSessionTest` | 10 | 🔴 | ⬜ |
| `api/GeckoMediaSession.kt` | `GeckoMediaSessionTest` | 4 | 🟡 | ⬜ |

**Note:** `GeckoBrowserView.kt` adalah UI component, di-test dengan UI tests.

---

## Test Cases

### GeckoBrowserSessionTest

**Difficulty:** ⭐⭐⭐ Hard (GeckoView mocking kompleks)
**Est. Time:** 3-4 jam

```kotlin
// File: engine/gecko/src/test/java/com/dwlhm/gecko/api/GeckoBrowserSessionTest.kt

class GeckoBrowserSessionTest {
    private val mockGeckoSession = mockk<GeckoSession>(relaxed = true)
    private val mockGeckoRuntime = mockk<GeckoRuntime>(relaxed = true)
    
    private lateinit var browserSession: GeckoBrowserSession
    
    // Capture delegate untuk simulate callbacks
    private val delegateSlot = slot<MediaSession.Delegate>()
    
    @BeforeEach
    fun setup() {
        every { mockGeckoSession.mediaSessionDelegate = capture(delegateSlot) } just Runs
        
        browserSession = GeckoBrowserSession(mockGeckoSession, mockGeckoRuntime)
    }
```

#### Basic Functionality

- [ ] `setUrl should call session loadUri`
  ```kotlin
  @Test
  fun `setUrl should call session loadUri`() {
      browserSession.setUrl("https://example.com")
      
      verify { mockGeckoSession.loadUri("https://example.com") }
  }
  ```

- [ ] `goBack should call session goBack`
- [ ] `goForward should call session goForward`
- [ ] `reload should call session reload`
- [ ] `close should cleanup resources`

#### Media Session (Phase 2 Critical)

- [ ] ⚠️ `onActivated should set activeMediaSession` **CRITICAL**
  ```kotlin
  @Test
  fun `onActivated should set activeMediaSession`() {
      val mockMediaSession = mockk<MediaSession>()
      
      // Simulate GeckoView callback
      delegateSlot.captured.onActivated(mockGeckoSession, mockMediaSession)
      
      // Verify session is tracked
      assertNotNull(browserSession.activeMediaSession)
  }
  ```

- [ ] ⚠️ `onDeactivated should clear activeMediaSession` **CRITICAL**
  ```kotlin
  @Test
  fun `onDeactivated should clear activeMediaSession`() {
      val mockMediaSession = mockk<MediaSession>()
      
      // First activate
      delegateSlot.captured.onActivated(mockGeckoSession, mockMediaSession)
      assertNotNull(browserSession.activeMediaSession)
      
      // Then deactivate
      delegateSlot.captured.onDeactivated(mockGeckoSession, mockMediaSession)
      assertNull(browserSession.activeMediaSession)
  }
  ```

- [ ] ⚠️ `activeMediaSession should persist across callback recreations` **CRITICAL - Phase 2 Core**
  ```kotlin
  @Test
  fun `activeMediaSession should persist when callback is accessed`() {
      val mockMediaSession = mockk<MediaSession>()
      
      // Activate media
      delegateSlot.captured.onActivated(mockGeckoSession, mockMediaSession)
      
      // Session should be accessible even if we get new callback reference
      // This is what Phase 2 is designed to fix
      val session1 = browserSession.activeMediaSession
      val session2 = browserSession.activeMediaSession
      
      assertEquals(session1, session2)
      assertNotNull(session1)
  }
  ```

- [ ] `onPlay should call sessionCallback with PLAY state`
- [ ] `onPause should call sessionCallback with PAUSE state`

---

### GeckoMediaSessionTest

**Difficulty:** ⭐⭐ Medium
**Est. Time:** 1 jam

```kotlin
// File: engine/gecko/src/test/java/com/dwlhm/gecko/api/GeckoMediaSessionTest.kt

class GeckoMediaSessionTest {
    private val mockMediaSession = mockk<MediaSession>(relaxed = true)
    private lateinit var geckoMediaSession: GeckoMediaSession
    
    @BeforeEach
    fun setup() {
        geckoMediaSession = GeckoMediaSession(mockMediaSession)
    }
```

- [ ] `play should call underlying mediaSession`
  ```kotlin
  @Test
  fun `play should call underlying mediaSession`() {
      geckoMediaSession.play()
      
      verify { mockMediaSession.play() }
  }
  ```

- [ ] `pause should call underlying mediaSession`
- [ ] `stop should call underlying mediaSession`
- [ ] `isActive should return correct state`

---

## Notes

### Why Engine Tests are Important

Engine layer adalah bridge ke GeckoView. Bugs di sini affect everything:
- Navigation
- Media playback
- Tab management

### GeckoView Mocking Challenges

GeckoView classes sulit di-mock karena:
- Banyak final classes
- Complex internal state
- Native dependencies

**Solutions:**
1. Use `mockk(relaxed = true)` untuk simplify
2. Capture delegates untuk simulate callbacks
3. Focus pada behavior, bukan implementation

### Phase 2 Tests

Tests dengan ⚠️ adalah tests untuk Phase 2 implementation:
- `activeMediaSession` tracking di session level
- Persistence across callback lifecycle

Lihat [ADR-004](../../adr/004-phase2-session-level-media-state.md) untuk context.

### Alternative: Instrumented Tests

Jika mocking terlalu sulit, pertimbangkan instrumented tests:

```kotlin
// androidTest
@Test
fun integrationTest() {
    // Use real GeckoSession dengan in-process GeckoRuntime
    val runtime = GeckoRuntime.getDefault(context)
    val session = GeckoSession()
    session.open(runtime)
    
    // Test real behavior
}
```

---

## Progress

| Test Class | Done | Total | % |
|------------|------|-------|---|
| GeckoBrowserSessionTest | 0 | 10 | 0% |
| GeckoMediaSessionTest | 0 | 4 | 0% |
| **Total** | **0** | **14** | **0%** |
