# Testing: Core Datastore

## Overview

| Info | Value |
|------|-------|
| Path | `core/datastore/src/main/java/com/dwlhm/datastore/` |
| Priority | 🟠 High |
| Phase | 2 (Core Business) |
| Est. Time | 4-5 jam |
| Total Tests | 14 |

---

## Files to Test

| File | Test Class | Tests | Priority | Status |
|------|------------|-------|----------|--------|
| `room/tabmanager/internal/TabRepositoryImpl.kt` | `TabRepositoryTest` | 6 | 🟠 | ⬜ |
| `room/tabmanager/internal/TabMapper.kt` | `TabMapperTest` | 4 | 🟡 | ⬜ |
| `preferences/OnboardingPrefs.kt` | `OnboardingPrefsTest` | 4 | 🟡 | ⬜ |

---

## Test Cases

### TabRepositoryTest

**Difficulty:** ⭐⭐ Medium (Room in-memory database)
**Est. Time:** 2 jam

```kotlin
// File: core/datastore/src/test/java/com/dwlhm/datastore/room/tabmanager/TabRepositoryTest.kt

class TabRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var tabDao: TabDao
    private lateinit var repository: TabRepositoryImpl
    
    @BeforeEach
    fun setup() {
        // In-memory database untuk testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        tabDao = database.tabDao()
        repository = TabRepositoryImpl(tabDao)
    }
    
    @AfterEach
    fun teardown() {
        database.close()
    }
```

- [ ] `getAllTabs should return all stored tabs`
  ```kotlin
  @Test
  fun `getAllTabs should return all stored tabs`() = runTest {
      // Arrange
      repository.saveTab(StoredTab("1", "Tab 1", "https://example1.com"))
      repository.saveTab(StoredTab("2", "Tab 2", "https://example2.com"))
      
      // Act
      val tabs = repository.getAllTabs().first()
      
      // Assert
      assertEquals(2, tabs.size)
  }
  ```

- [ ] `saveTab should persist tab to database`
- [ ] `deleteTab should remove tab from database`
- [ ] `getTabById should return correct tab`
- [ ] `getTabById for non-existent should return null`
- [ ] `updateTab should modify existing tab`

---

### TabMapperTest

**Difficulty:** ⭐ Easy
**Est. Time:** 1 jam

```kotlin
// File: core/datastore/src/test/java/com/dwlhm/datastore/room/tabmanager/TabMapperTest.kt

class TabMapperTest {
```

- [ ] `toEntity should convert StoredTab to TabEntity`
  ```kotlin
  @Test
  fun `toEntity should convert StoredTab to TabEntity`() {
      val storedTab = StoredTab(
          id = "tab-1",
          title = "My Tab",
          url = "https://example.com"
      )
      
      val entity = storedTab.toEntity()
      
      assertEquals("tab-1", entity.id)
      assertEquals("My Tab", entity.title)
      assertEquals("https://example.com", entity.url)
  }
  ```

- [ ] `toDomain should convert TabEntity to StoredTab`
- [ ] `round-trip mapping should preserve all fields`
- [ ] `null fields should be handled correctly`

---

### OnboardingPrefsTest

**Difficulty:** ⭐⭐ Medium (DataStore testing)
**Est. Time:** 1 jam

```kotlin
// File: core/datastore/src/test/java/com/dwlhm/datastore/preferences/OnboardingPrefsTest.kt

class OnboardingPrefsTest {
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var prefs: OnboardingPrefs
    
    @BeforeEach
    fun setup() {
        // In-memory DataStore
        dataStore = PreferenceDataStoreFactory.create(
            scope = TestScope()
        ) {
            File.createTempFile("test_prefs", ".preferences_pb")
        }
        prefs = OnboardingPrefs(dataStore)
    }
```

- [ ] `initial value should be false (not onboarded)`
  ```kotlin
  @Test
  fun `initial value should be false`() = runTest {
      val hasOnboarded = prefs.hasOnboarded.first()
      assertFalse(hasOnboarded)
  }
  ```

- [ ] `setOnboarded true should persist`
- [ ] `hasOnboarded Flow should emit updates`
- [ ] `value should persist across reads`

---

## Notes

### Room In-Memory Database

Untuk testing Room, gunakan in-memory database:

```kotlin
// Di androidTest (instrumented test)
Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
    .allowMainThreadQueries()  // Untuk test simplicity
    .build()
```

### DataStore Testing

```kotlin
// Temporary file-based DataStore
PreferenceDataStoreFactory.create(
    scope = TestScope()
) {
    File.createTempFile("test", ".preferences_pb")
}
```

### Important

Room tests biasanya perlu `androidTest` (instrumented) karena butuh Android Context. Untuk unit test murni, mock `TabDao`.

---

## Progress

| Test Class | Done | Total | % |
|------------|------|-------|---|
| TabRepositoryTest | 0 | 6 | 0% |
| TabMapperTest | 0 | 4 | 0% |
| OnboardingPrefsTest | 0 | 4 | 0% |
| **Total** | **0** | **14** | **0%** |
