# Testing: Core Browser

## Overview

| Info | Value |
|------|-------|
| Path | `core/browser/src/main/java/com/dwlhm/browser/` |
| Priority | 🔴 Critical |
| Phase | 1 (Foundation) |
| Est. Time | 1-2 jam |
| Total Tests | 8 |

---

## Files to Test

| File | Test Class | Tests | Priority | Status |
|------|------------|-------|----------|--------|
| `BrowserMediaMetadata.kt` | `BrowserMediaMetadataTest` | 3 | 🟡 | ⬜ |
| `BrowserMediaState.kt` | `BrowserMediaStateTest` | 2 | 🟡 | ⬜ |
| `BrowserTab.kt` | `BrowserTabTest` | 3 | 🟡 | ⬜ |

**Note:** Interface files (`BrowserSession.kt`, `BrowserSessionCallback.kt`, dll) tidak perlu di-test langsung. Test di implementation layer (Gecko).

---

## Test Cases

### BrowserMediaMetadataTest

**Difficulty:** ⭐ Easy
**Est. Time:** 30 menit

```kotlin
// File: core/browser/src/test/java/com/dwlhm/browser/BrowserMediaMetadataTest.kt

class BrowserMediaMetadataTest {
```

- [ ] `should hold all metadata fields correctly`
  ```kotlin
  @Test
  fun `should hold all metadata fields correctly`() {
      val metadata = BrowserMediaMetadata(
          title = "Song Title",
          artist = "Artist Name",
          album = "Album Name",
          artwork = null
      )
      
      assertEquals("Song Title", metadata.title)
      assertEquals("Artist Name", metadata.artist)
      assertEquals("Album Name", metadata.album)
      assertNull(metadata.artwork)
  }
  ```

- [ ] `equality should work for same values`
- [ ] `copy should create new instance with updated field`

---

### BrowserMediaStateTest

**Difficulty:** ⭐ Easy
**Est. Time:** 15 menit

```kotlin
// File: core/browser/src/test/java/com/dwlhm/browser/BrowserMediaStateTest.kt

class BrowserMediaStateTest {
```

- [ ] `should have PLAY PAUSE STOP values`
  ```kotlin
  @Test
  fun `should have PLAY PAUSE STOP values`() {
      val values = BrowserMediaState.values()
      
      assertTrue(values.contains(BrowserMediaState.PLAY))
      assertTrue(values.contains(BrowserMediaState.PAUSE))
      assertTrue(values.contains(BrowserMediaState.STOP))
  }
  ```

- [ ] `values should be distinguishable`
  ```kotlin
  @Test
  fun `values should be distinguishable`() {
      assertNotEquals(BrowserMediaState.PLAY, BrowserMediaState.PAUSE)
      assertNotEquals(BrowserMediaState.PAUSE, BrowserMediaState.STOP)
      assertNotEquals(BrowserMediaState.PLAY, BrowserMediaState.STOP)
  }
  ```

---

### BrowserTabTest

**Difficulty:** ⭐ Easy
**Est. Time:** 30 menit

```kotlin
// File: core/browser/src/test/java/com/dwlhm/browser/BrowserTabTest.kt

class BrowserTabTest {
```

- [ ] `should hold id and session correctly`
- [ ] `equality should be based on id`
- [ ] `hashCode should be consistent with equals`

---

## Notes

### Apa yang TIDAK Perlu Di-test

File-file ini adalah interface/abstraksi:
- `BrowserSession.kt` - Interface, test di `GeckoBrowserSession`
- `BrowserSessionCallback.kt` - Interface
- `BrowserMediaSession.kt` - Interface, test di `GeckoMediaSession`
- `BrowserRuntime.kt` - Interface
- `BrowserViewHost.kt` - Interface
- `TabManager.kt` - Interface

### Related Tests

Implementation tests ada di:
- [engine-gecko.md](engine-gecko.md) - `GeckoBrowserSession`, `GeckoMediaSession`

---

## Progress

| Test Class | Done | Total | % |
|------------|------|-------|---|
| BrowserMediaMetadataTest | 0 | 3 | 0% |
| BrowserMediaStateTest | 0 | 2 | 0% |
| BrowserTabTest | 0 | 3 | 0% |
| **Total** | **0** | **8** | **0%** |
