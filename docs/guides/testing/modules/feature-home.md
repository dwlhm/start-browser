# Testing: Feature Home

## Overview

| Info | Value |
|------|-------|
| Path | `feature/home/src/main/java/com/dwlhm/home/` |
| Priority | 🟡 Medium |
| Phase | 3 (Feature Layer) |
| Est. Time | 3-4 jam |
| Total Tests | 11 |

---

## Files to Test

| File | Test Class | Tests | Priority | Status |
|------|------------|-------|----------|--------|
| `ui/HomeViewModel.kt` | `HomeViewModelTest` | 6 | 🟡 | ⬜ |
| `ui/LastVisitedViewModel.kt` | `LastVisitedViewModelTest` | 5 | 🟡 | ⬜ |

**Note:** Composable UI (`HomeScreen.kt`, `LastVisitedComposable.kt`) di-test dengan UI tests (optional).

---

## Test Cases

### HomeViewModelTest

**Difficulty:** ⭐⭐ Medium (ViewModel testing pattern)
**Est. Time:** 2 jam

```kotlin
// File: feature/home/src/test/java/com/dwlhm/home/ui/HomeViewModelTest.kt

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    private val mockRepository = mockk<HomeRepository>()
    private val mockSavedStateHandle = SavedStateHandle()
    
    private lateinit var viewModel: HomeViewModel
    
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(mockRepository, mockSavedStateHandle)
    }
    
    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }
```

- [ ] `initial state should be loading`
  ```kotlin
  @Test
  fun `initial state should be loading`() = runTest {
      val state = viewModel.uiState.first()
      assertTrue(state is HomeUiState.Loading)
  }
  ```

- [ ] `loadData success should update state to success`
- [ ] `loadData error should update state to error`
- [ ] `search should filter results`
- [ ] `refresh should reload data`
- [ ] `navigation event should be emitted correctly`

---

### LastVisitedViewModelTest

**Difficulty:** ⭐⭐ Medium
**Est. Time:** 1.5 jam

```kotlin
// File: feature/home/src/test/java/com/dwlhm/home/ui/LastVisitedViewModelTest.kt

class LastVisitedViewModelTest {
    private val mockRepository = mockk<LastVisitedRepository>()
    private lateinit var viewModel: LastVisitedViewModel
    
    @BeforeEach
    fun setup() {
        every { mockRepository.getLastVisited() } returns flowOf(emptyList())
        viewModel = LastVisitedViewModel(mockRepository)
    }
```

- [ ] `lastVisited should emit from repository`
  ```kotlin
  @Test
  fun `lastVisited should emit from repository`() = runTest {
      val items = listOf(
          LastVisitedData("1", "Example", "https://example.com", System.currentTimeMillis())
      )
      every { mockRepository.getLastVisited() } returns flowOf(items)
      
      viewModel = LastVisitedViewModel(mockRepository)
      
      val result = viewModel.lastVisited.first()
      assertEquals(1, result.size)
      assertEquals("Example", result[0].title)
  }
  ```

- [ ] `addVisited should call repository`
- [ ] `clearHistory should call repository`
- [ ] `items should be sorted by date (newest first)`
- [ ] `limit should be respected`

---

## Notes

### ViewModel Testing Pattern

```kotlin
// Standard setup untuk ViewModel testing
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)  // Override Main dispatcher
    }
    
    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()  // Reset
    }
    
    @Test
    fun `test`() = runTest {
        // Test dengan coroutines
    }
}
```

### Testing StateFlow

```kotlin
// Dengan Turbine (recommended)
@Test
fun `state should update`() = runTest {
    viewModel.uiState.test {
        assertEquals(Loading, awaitItem())  // Initial
        
        viewModel.loadData()
        
        assertEquals(Success(data), awaitItem())  // After load
    }
}
```

---

## Progress

| Test Class | Done | Total | % |
|------------|------|-------|---|
| HomeViewModelTest | 0 | 6 | 0% |
| LastVisitedViewModelTest | 0 | 5 | 0% |
| **Total** | **0** | **11** | **0%** |
