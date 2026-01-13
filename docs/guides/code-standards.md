# Code Standards Guide

> Panduan coding standard berdasarkan best practices industri.
> 
> **References:**
> - [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) (JetBrains Official)
> - [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide) (Google Official)
> - [Clean Code](https://www.oreilly.com/library/view/clean-code-a/9780136083238/) (Robert C. Martin)

---

## 📏 Naming Conventions

### Packages

```kotlin
// ✅ Good: lowercase, no underscores
package com.example.startbrowser.feature.bookmark

// ❌ Bad
package com.example.startBrowser.Feature_Bookmark
```

### Classes & Interfaces

```kotlin
// ✅ PascalCase untuk class, interface, object
class BookmarkRepository
interface BookmarkDao
object NetworkModule

// ✅ Suffix yang meaningful
class BookmarkViewModel      // ViewModel
class BookmarkUseCase        // UseCase  
class BookmarkRepository     // Repository
class BookmarkScreen         // Composable Screen
class BookmarkActivity       // Activity
class BookmarkFragment       // Fragment

// ❌ Bad: generic atau tidak deskriptif
class Manager
class Helper
class Utils   // hindari, lebih baik extension function
```

### Functions

```kotlin
// ✅ camelCase, verb-based, descriptive
fun getBookmarks(): List<Bookmark>
fun saveBookmark(bookmark: Bookmark)
fun deleteBookmarkById(id: String)
fun isBookmarkExists(url: String): Boolean

// ✅ Suspend functions - bisa tambah suffix jika perlu clarity
suspend fun fetchBookmarksFromNetwork(): List<Bookmark>

// ❌ Bad
fun data()          // tidak deskriptif
fun process()       // terlalu generic
fun doStuff()       // tidak meaningful
```

### Variables & Properties

```kotlin
// ✅ camelCase
val bookmarkList: List<Bookmark>
var isLoading: Boolean
private val _uiState = MutableStateFlow<UiState>()

// ✅ Constants: SCREAMING_SNAKE_CASE
companion object {
    private const val MAX_BOOKMARKS = 100
    private const val DEFAULT_TIMEOUT_MS = 5000L
}

// ✅ Backing properties dengan underscore prefix
private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

// ❌ Bad
val x: Int              // tidak deskriptif
val temp: String        // hindari nama temporary
val data: Any           // terlalu generic
```

### Composable Functions

```kotlin
// ✅ PascalCase untuk Composable (seperti class)
@Composable
fun BookmarkScreen(
    viewModel: BookmarkViewModel = hiltViewModel()
) { }

@Composable
fun BookmarkItem(
    bookmark: Bookmark,
    onClick: () -> Unit
) { }

// ✅ Stateless composable dengan parameter yang jelas
@Composable
fun BookmarkCard(
    title: String,
    url: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier  // modifier selalu parameter terakhir dengan default
) { }
```

---

## 📐 Code Structure & Organization

### File Organization

```kotlin
// Urutan dalam file (sesuai Kotlin conventions):
// 1. Package statement
// 2. Imports (no wildcard imports)
// 3. Top-level declarations

package com.example.feature.bookmark

import androidx.compose.runtime.Composable
import com.example.core.domain.Bookmark
// ... imports dikelompokkan: Android, third-party, project

/**
 * Screen untuk menampilkan daftar bookmark.
 */
@Composable
fun BookmarkScreen() { }

// Private helpers di bawah
@Composable
private fun BookmarkList() { }
```

### Class Organization

```kotlin
class BookmarkViewModel @Inject constructor(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase
) : ViewModel() {

    // 1. Companion object / constants (jika ada)
    companion object {
        private const val TAG = "BookmarkViewModel"
    }

    // 2. Properties - public dulu, lalu private
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()
    
    private val _uiState = MutableStateFlow(BookmarkUiState())

    // 3. Init block (jika ada)
    init {
        loadBookmarks()
    }

    // 4. Public functions
    fun deleteBookmark(id: String) {
        viewModelScope.launch {
            deleteBookmarkUseCase(id)
        }
    }

    // 5. Private functions
    private fun loadBookmarks() {
        viewModelScope.launch {
            // ...
        }
    }
}
```

### Import Guidelines

```kotlin
// ✅ Good: explicit imports
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow

// ❌ Bad: wildcard imports
import kotlinx.coroutines.flow.*

// ✅ Exception: banyak Compose imports boleh wildcard
import androidx.compose.ui.*
import androidx.compose.material3.*
```

---

## 🎯 Clean Code Principles

### Single Responsibility

```kotlin
// ❌ Bad: ViewModel doing too much
class BookmarkViewModel {
    fun loadBookmarks() { /* load dari DB */ }
    fun syncWithServer() { /* network call */ }
    fun formatDate(date: Date) { /* formatting */ }
    fun validateUrl(url: String) { /* validation */ }
}

// ✅ Good: separated concerns
class BookmarkViewModel(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val syncBookmarksUseCase: SyncBookmarksUseCase
)

class UrlValidator {
    fun isValid(url: String): Boolean
}

// Date formatting via extension function
fun Date.toDisplayFormat(): String
```

### Function Size

```kotlin
// ✅ Good: function melakukan satu hal, mudah dibaca
fun processBookmark(bookmark: Bookmark): Result<Unit> {
    validateBookmark(bookmark).getOrElse { return Result.failure(it) }
    saveToDatabase(bookmark)
    updateUiState()
    return Result.success(Unit)
}

// ❌ Bad: function terlalu panjang (>20-30 lines biasanya red flag)
fun processBookmark(bookmark: Bookmark) {
    // 100+ lines of code...
}
```

### Parameter Count

```kotlin
// ❌ Bad: terlalu banyak parameter
fun createBookmark(
    title: String,
    url: String,
    favicon: String,
    createdAt: Long,
    updatedAt: Long,
    folderId: String,
    tags: List<String>,
    isPrivate: Boolean
)

// ✅ Good: gunakan data class
data class CreateBookmarkRequest(
    val title: String,
    val url: String,
    val favicon: String? = null,
    val folderId: String? = null,
    val tags: List<String> = emptyList(),
    val isPrivate: Boolean = false
)

fun createBookmark(request: CreateBookmarkRequest)
```

### Early Return

```kotlin
// ✅ Good: early return untuk reduce nesting
fun processUrl(url: String?): Result<Bookmark> {
    if (url.isNullOrBlank()) {
        return Result.failure(InvalidUrlException("URL is empty"))
    }
    
    if (!isValidUrl(url)) {
        return Result.failure(InvalidUrlException("Invalid URL format"))
    }
    
    val bookmark = createBookmark(url)
    return Result.success(bookmark)
}

// ❌ Bad: deep nesting
fun processUrl(url: String?): Result<Bookmark> {
    if (url != null) {
        if (url.isNotBlank()) {
            if (isValidUrl(url)) {
                val bookmark = createBookmark(url)
                return Result.success(bookmark)
            } else {
                return Result.failure(InvalidUrlException("Invalid"))
            }
        } else {
            return Result.failure(InvalidUrlException("Empty"))
        }
    } else {
        return Result.failure(InvalidUrlException("Null"))
    }
}
```

---

## 🔄 Kotlin Idioms

### Null Safety

```kotlin
// ✅ Good: gunakan safe calls dan elvis operator
val title = bookmark?.title ?: "Untitled"

// ✅ Good: let untuk null check dengan action
bookmark?.let { saveToDatabase(it) }

// ✅ Good: takeIf / takeUnless
val validUrl = url.takeIf { it.startsWith("http") }

// ❌ Bad: unnecessary null checks
if (bookmark != null) {
    if (bookmark.title != null) {
        // ...
    }
}
```

### Collections

```kotlin
// ✅ Good: gunakan collection functions
val activeBookmarks = bookmarks
    .filter { it.isActive }
    .sortedByDescending { it.createdAt }
    .take(10)

// ✅ Good: firstOrNull instead of find
val bookmark = bookmarks.firstOrNull { it.id == targetId }

// ❌ Bad: manual loop untuk hal yang bisa pakai stdlib
val result = mutableListOf<Bookmark>()
for (b in bookmarks) {
    if (b.isActive) {
        result.add(b)
    }
}
```

### Scope Functions

```kotlin
// apply - configure object, return object
val bookmark = Bookmark().apply {
    title = "Example"
    url = "https://example.com"
}

// let - transform nullable, return result
val length = url?.let { processUrl(it) }

// run - execute block, return result
val result = bookmark.run {
    "$title - $url"
}

// also - side effects, return object
val bookmark = createBookmark().also {
    logger.log("Created bookmark: ${it.id}")
}

// with - non-null receiver, return result
val description = with(bookmark) {
    "$title\n$url\nCreated: $createdAt"
}
```

### Data Classes

```kotlin
// ✅ Good: data class untuk model
data class Bookmark(
    val id: String,
    val title: String,
    val url: String,
    val createdAt: Long = System.currentTimeMillis()
)

// ✅ Good: copy untuk immutable updates
val updated = bookmark.copy(title = "New Title")

// ✅ Good: destructuring
val (id, title, url) = bookmark
```

---

## 🏗️ Architecture Patterns

### Repository Pattern

```kotlin
// Interface di domain layer
interface BookmarkRepository {
    suspend fun getAll(): List<Bookmark>
    suspend fun getById(id: String): Bookmark?
    suspend fun save(bookmark: Bookmark)
    suspend fun delete(id: String)
}

// Implementation di data layer
class BookmarkRepositoryImpl @Inject constructor(
    private val localDataSource: BookmarkLocalDataSource,
    private val remoteDataSource: BookmarkRemoteDataSource
) : BookmarkRepository {
    
    override suspend fun getAll(): List<Bookmark> {
        return localDataSource.getAll()
    }
    
    // ... other implementations
}
```

### UseCase Pattern

```kotlin
// Single responsibility use case
class GetBookmarksUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(): List<Bookmark> {
        return repository.getAll()
            .sortedByDescending { it.createdAt }
    }
}

// Usage di ViewModel
class BookmarkViewModel @Inject constructor(
    private val getBookmarksUseCase: GetBookmarksUseCase
) : ViewModel() {
    
    fun loadBookmarks() {
        viewModelScope.launch {
            val bookmarks = getBookmarksUseCase()  // invoke via operator
            _uiState.update { it.copy(bookmarks = bookmarks) }
        }
    }
}
```

### UI State Pattern

```kotlin
// ✅ Good: sealed class/interface untuk UI state
sealed interface BookmarkUiState {
    data object Loading : BookmarkUiState
    data class Success(val bookmarks: List<Bookmark>) : BookmarkUiState
    data class Error(val message: String) : BookmarkUiState
}

// Atau dengan data class + loading/error flags
data class BookmarkUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

---

## 📝 Documentation

### KDoc Comments

```kotlin
/**
 * Repository untuk mengelola bookmark.
 *
 * Repository ini menghandle:
 * - Local storage via Room
 * - Remote sync via API (jika online)
 *
 * @property localDataSource Data source untuk operasi lokal
 * @property remoteDataSource Data source untuk operasi remote
 */
class BookmarkRepository(
    private val localDataSource: BookmarkLocalDataSource,
    private val remoteDataSource: BookmarkRemoteDataSource
)

/**
 * Menyimpan bookmark ke database.
 *
 * @param bookmark Bookmark yang akan disimpan
 * @return [Result] berisi bookmark yang tersimpan atau error
 * @throws IllegalArgumentException jika URL tidak valid
 */
suspend fun save(bookmark: Bookmark): Result<Bookmark>
```

### When to Comment

```kotlin
// ✅ Good: explain WHY, not WHAT
// Delay needed because GeckoView needs time to initialize media session
delay(100)

// ✅ Good: document non-obvious behavior
// Room requires @Transaction for multiple operations to ensure atomicity
@Transaction
suspend fun replaceAll(bookmarks: List<Bookmark>)

// ❌ Bad: obvious comments
// Get the bookmark by ID
fun getBookmarkById(id: String): Bookmark

// ❌ Bad: commented-out code (hapus saja)
// fun oldMethod() { ... }
```

---

## ⚠️ Error Handling

### Result Pattern

```kotlin
// ✅ Good: gunakan Result untuk operasi yang bisa fail
suspend fun saveBookmark(bookmark: Bookmark): Result<Bookmark> {
    return runCatching {
        validateBookmark(bookmark)
        repository.save(bookmark)
        bookmark
    }
}

// Usage
saveBookmark(bookmark)
    .onSuccess { saved -> showSuccess("Saved: ${saved.title}") }
    .onFailure { error -> showError(error.message) }
```

### Sealed Class untuk Domain Errors

```kotlin
sealed class BookmarkError : Exception() {
    data object InvalidUrl : BookmarkError()
    data object DuplicateBookmark : BookmarkError()
    data class NetworkError(override val message: String) : BookmarkError()
    data class Unknown(override val cause: Throwable) : BookmarkError()
}
```

---

## 🧪 Testability

### Dependency Injection

```kotlin
// ✅ Good: dependencies injected, easy to test
class BookmarkViewModel @Inject constructor(
    private val getBookmarksUseCase: GetBookmarksUseCase
) : ViewModel()

// ❌ Bad: hard-coded dependencies
class BookmarkViewModel : ViewModel() {
    private val repository = BookmarkRepositoryImpl()  // tidak bisa di-mock
}
```

### Interface-based Design

```kotlin
// ✅ Good: interface untuk mockability
interface BookmarkRepository {
    suspend fun getAll(): List<Bookmark>
}

// Dalam test bisa mock dengan ease
val mockRepository = mockk<BookmarkRepository>()
coEvery { mockRepository.getAll() } returns listOf(testBookmark)
```

---

## 📋 Checklist Sebelum PR

- [ ] Naming sesuai conventions
- [ ] Tidak ada magic numbers (gunakan constants)
- [ ] Functions tidak terlalu panjang (<30 lines)
- [ ] Tidak ada deep nesting (max 2-3 levels)
- [ ] Null safety dihandle dengan proper
- [ ] Error handling ada dan meaningful
- [ ] KDoc untuk public APIs
- [ ] Tidak ada commented-out code
- [ ] Tidak ada wildcard imports (kecuali Compose)
- [ ] Dependencies di-inject (testable)

---

## 🔗 References

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- [Effective Kotlin](https://kt.academy/book/effectivekotlin)
- [Clean Code](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)

---

*Last updated: 2026-01-14*
