# Testing: Feature Onboarding

## Overview

| Info | Value |
|------|-------|
| Path | `feature/onboarding/src/main/java/com/dwlhm/onboarding/` |
| Priority | 🟢 Low |
| Phase | 3 (Feature Layer) |
| Est. Time | 1 jam |
| Total Tests | 4 |

---

## Files to Test

| File | Test Class | Tests | Priority | Status |
|------|------------|-------|----------|--------|
| `ui/OnboardingViewModel.kt` | `OnboardingViewModelTest` | 4 | 🟢 | ⬜ |

**Note:** Animation components (`AnimatedRotatingText.kt`, `SunlightParticle.kt`) tidak perlu di-test. UI screens di-test dengan UI tests (optional).

---

## Test Cases

### OnboardingViewModelTest

**Difficulty:** ⭐⭐ Medium
**Est. Time:** 1 jam

```kotlin
// File: feature/onboarding/src/test/java/com/dwlhm/onboarding/ui/OnboardingViewModelTest.kt

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    private val mockOnboardingPrefs = mockk<OnboardingPrefs>(relaxed = true)
    
    private lateinit var viewModel: OnboardingViewModel
    
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel(mockOnboardingPrefs)
    }
    
    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }
```

- [ ] `initial state should be on first page`
  ```kotlin
  @Test
  fun `initial state should be on first page`() = runTest {
      val state = viewModel.uiState.first()
      assertEquals(0, state.currentPage)
  }
  ```

- [ ] `nextPage should advance page`
  ```kotlin
  @Test
  fun `nextPage should advance page`() = runTest {
      viewModel.nextPage()
      advanceUntilIdle()
      
      val state = viewModel.uiState.first()
      assertEquals(1, state.currentPage)
  }
  ```

- [ ] `complete should set hasOnboarded true`
  ```kotlin
  @Test
  fun `complete should set hasOnboarded true`() = runTest {
      viewModel.complete()
      advanceUntilIdle()
      
      coVerify { mockOnboardingPrefs.setOnboarded(true) }
  }
  ```

- [ ] `complete should emit navigation event`

---

## Notes

### Low Priority

Onboarding adalah fitur sekali pakai (user hanya lihat sekali). Bug di sini tidak terlalu critical dibanding fitur lain.

### Skip Animation Testing

Files ini tidak perlu di-test:
- `AnimatedRotatingText.kt` - Pure UI animation
- `AnimatedRotatingTextViewModel.kt` - Animation state only
- `SunlightParticle.kt` - Visual effect

Testing animation susah dan tidak memberikan value yang tinggi.

---

## Progress

| Test Class | Done | Total | % |
|------------|------|-------|---|
| OnboardingViewModelTest | 0 | 4 | 0% |
| **Total** | **0** | **4** | **0%** |
