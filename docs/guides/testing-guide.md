# Panduan Testing untuk Pemula

> **Dokumen ini ditujukan untuk developer yang baru pertama kali menulis test.**
> Kita akan belajar dari konsep dasar hingga implementasi praktis.

## Daftar Isi

1. [Mengapa Testing Penting?](#1-mengapa-testing-penting)
2. [Jenis-Jenis Testing](#2-jenis-jenis-testing)
3. [Anatomi Unit Test](#3-anatomi-unit-test)
4. [Setup Testing di Android/Kotlin](#4-setup-testing-di-androidkotlin)
5. [Menulis Test Pertamamu](#5-menulis-test-pertamamu)
6. [Test Cases untuk Media Notification](#6-test-cases-untuk-media-notification)
7. [Tips & Best Practices](#7-tips--best-practices)
8. [Common Mistakes](#8-common-mistakes)
9. [Resources untuk Belajar Lebih Lanjut](#9-resources-untuk-belajar-lebih-lanjut)

---

## 1. Mengapa Testing Penting?

### Analogi Sederhana

Bayangkan kamu membangun rumah:
- **Tanpa testing:** Bangun semua dulu, baru cek. Kalau ada yang salah, bongkar semua.
- **Dengan testing:** Cek setiap bata, setiap dinding, setiap lantai. Masalah ketahuan lebih awal.

### Manfaat Testing

| Manfaat | Penjelasan |
|---------|------------|
| **Confidence** | Yakin code bekerja sesuai ekspektasi |
| **Dokumentasi** | Test menjelaskan bagaimana code seharusnya bekerja |
| **Refactoring Safety** | Bisa ubah code tanpa takut merusak fitur |
| **Bug Prevention** | Catch bugs sebelum production |
| **Faster Development** | Paradoks: menulis test = develop lebih cepat dalam jangka panjang |

### Contoh Nyata di Project Ini

Kita punya bug: notification tidak sinkron saat kembali dari notification click.

**Tanpa test:**
1. Fix bug
2. Manual testing (buka app, play video, klik notification, cek...)
3. Deploy
4. Bug muncul lagi karena code lain berubah
5. Ulangi dari awal

**Dengan test:**
1. Tulis test yang gagal (reproduce bug)
2. Fix bug
3. Test pass
4. Deploy
5. Kalau code lain merusak fix ini, test akan gagal → ketahuan sebelum deploy

---

## 2. Jenis-Jenis Testing

### Testing Pyramid

```
                    ┌─────────┐
                   /   E2E    \        ← Paling lambat, paling mahal
                  /  (Manual)  \          Tapi paling mirip user experience
                 /───────────────\
                /   Integration   \    ← Menengah
               /    (beberapa      \      Test interaksi antar komponen
              /      komponen)      \
             /───────────────────────\
            /       Unit Tests        \  ← Paling cepat, paling murah
           /    (satu fungsi/class)    \    Mayoritas test ada di sini
          /─────────────────────────────\
```

### Penjelasan Setiap Jenis

#### Unit Test
**Apa:** Test satu fungsi atau satu class secara terisolasi.

```kotlin
// Contoh: Test fungsi sederhana
fun add(a: Int, b: Int): Int = a + b

@Test
fun `add should return sum of two numbers`() {
    val result = add(2, 3)
    assertEquals(5, result)
}
```

**Karakteristik:**
- Cepat (milidetik)
- Tidak butuh device/emulator
- Mudah ditulis
- Banyak (ratusan-ribuan di project besar)

#### Integration Test
**Apa:** Test interaksi beberapa komponen bersama.

```kotlin
// Contoh: Test MediaPlaybackManager + EventDispatcher
@Test
fun `when media activated event dispatched then service should start`() {
    // Ini melibatkan:
    // - EventDispatcher
    // - MediaPlaybackManager
    // - Context (untuk start service)
}
```

**Karakteristik:**
- Lebih lambat (detik)
- Mungkin butuh Android context
- Lebih kompleks
- Lebih sedikit jumlahnya

#### End-to-End (E2E) Test
**Apa:** Test keseluruhan aplikasi seperti user sungguhan.

```
1. Buka app
2. Navigate ke YouTube
3. Play video
4. Lihat notification muncul
5. Klik notification
6. Verify kembali ke app dengan state benar
```

**Karakteristik:**
- Paling lambat (menit)
- Butuh device/emulator
- Paling brittle (mudah gagal karena hal kecil)
- Paling sedikit jumlahnya

### Untuk Pemula: Fokus ke Unit Test Dulu!

Kenapa?
1. Paling mudah dipelajari
2. Feedback loop cepat
3. Membangun mental model testing yang benar
4. Skill transferable ke jenis test lain

---

## 3. Anatomi Unit Test

### Struktur AAA (Arrange-Act-Assert)

Setiap test punya 3 bagian:

```kotlin
@Test
fun `descriptive test name describing what should happen`() {
    // ═══════════════════════════════════════════════════
    // ARRANGE (Setup)
    // Siapkan semua yang dibutuhkan untuk test
    // ═══════════════════════════════════════════════════
    val calculator = Calculator()
    val a = 5
    val b = 3
    
    // ═══════════════════════════════════════════════════
    // ACT (Execute)
    // Jalankan aksi yang mau di-test
    // ═══════════════════════════════════════════════════
    val result = calculator.add(a, b)
    
    // ═══════════════════════════════════════════════════
    // ASSERT (Verify)
    // Cek apakah hasilnya sesuai ekspektasi
    // ═══════════════════════════════════════════════════
    assertEquals(8, result)
}
```

### Naming Convention

Format yang bagus:
```kotlin
fun `when [kondisi] then [ekspektasi]`()
fun `should [aksi] when [kondisi]`()
fun `given [state] when [aksi] then [hasil]`()
```

Contoh:
```kotlin
// ✅ Bagus - Jelas dan deskriptif
fun `when media activated then activeMediaSession should not be null`()
fun `should start service when first media state change received`()
fun `given paused media when play clicked then state should be PLAY`()

// ❌ Kurang bagus - Tidak jelas
fun `test1`()
fun `testMediaActivated`()
fun `mediaTest`()
```

### Contoh Lengkap

```kotlin
class MediaPlaybackStateTest {
    
    @Test
    fun `when created with default values then playbackState should be PAUSE`() {
        // Arrange
        val tabId = "tab-123"
        val mediaSession = mockk<BrowserMediaSession>()
        
        // Act
        val state = MediaPlaybackState(
            tabId = tabId,
            mediaSession = mediaSession
        )
        
        // Assert
        assertEquals(BrowserMediaState.PAUSE, state.playbackState)
    }
    
    @Test
    fun `withPlaybackState should return new instance with updated state`() {
        // Arrange
        val original = MediaPlaybackState(
            tabId = "tab-123",
            mediaSession = mockk(),
            playbackState = BrowserMediaState.PAUSE
        )
        
        // Act
        val updated = original.withPlaybackState(BrowserMediaState.PLAY)
        
        // Assert
        // Original tidak berubah (immutable)
        assertEquals(BrowserMediaState.PAUSE, original.playbackState)
        // Updated punya state baru
        assertEquals(BrowserMediaState.PLAY, updated.playbackState)
        // Tapi data lain tetap sama
        assertEquals(original.tabId, updated.tabId)
    }
}
```

---

## 4. Setup Testing di Android/Kotlin

### Dependencies yang Dibutuhkan

Di `build.gradle.kts` (module level):

```kotlin
dependencies {
    // ═══════════════════════════════════════════════════
    // Unit Testing
    // ═══════════════════════════════════════════════════
    
    // JUnit 5 - Framework test utama
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    
    // Kotlin test utilities
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.0")
    
    // MockK - Untuk mocking di Kotlin
    testImplementation("io.mockk:mockk:1.13.8")
    
    // Coroutines testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Truth - Assertion library yang readable (opsional, tapi recommended)
    testImplementation("com.google.truth:truth:1.1.5")
    
    // ═══════════════════════════════════════════════════
    // Android Instrumented Testing (Integration/UI)
    // ═══════════════════════════════════════════════════
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

### Struktur Folder

```
app/
├── src/
│   ├── main/           ← Production code
│   │   └── java/
│   │       └── com/dwlhm/startbrowser/
│   │           └── services/
│   │               ├── MediaPlaybackService.kt
│   │               └── MediaPlaybackManager.kt
│   │
│   ├── test/           ← Unit tests (JVM, no Android)
│   │   └── java/
│   │       └── com/dwlhm/startbrowser/
│   │           └── services/
│   │               ├── MediaPlaybackServiceTest.kt
│   │               └── MediaPlaybackManagerTest.kt
│   │
│   └── androidTest/    ← Instrumented tests (butuh device/emulator)
│       └── java/
│           └── com/dwlhm/startbrowser/
│               └── MediaNotificationIntegrationTest.kt
```

### Menjalankan Test

```bash
# Jalankan semua unit tests
./gradlew test

# Jalankan test untuk module tertentu
./gradlew :app:test

# Jalankan test class tertentu
./gradlew test --tests "com.dwlhm.startbrowser.services.MediaPlaybackStateTest"

# Jalankan dengan output detail
./gradlew test --info
```

Di Android Studio:
1. Klik kanan pada file test → "Run 'TestClassName'"
2. Atau klik icon play di sebelah kiri nama test

---

## 5. Menulis Test Pertamamu

### Step 1: Pilih Class yang Paling Sederhana

Mulai dari class tanpa dependencies eksternal. Di project ini, `MediaPlaybackState` adalah kandidat terbaik karena:
- Pure data class
- Tidak ada Android dependencies
- Logic sederhana

### Step 2: Buat File Test

Lokasi: `app/src/test/java/com/dwlhm/startbrowser/services/media/MediaPlaybackStateTest.kt`

```kotlin
package com.dwlhm.startbrowser.services.media

import com.dwlhm.browser.BrowserMediaState
import com.dwlhm.browser.BrowserMediaSession
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested

/**
 * Unit tests untuk MediaPlaybackState.
 * 
 * MediaPlaybackState adalah immutable data class yang menyimpan
 * state media playback. Test ini memverifikasi:
 * 1. Default values benar
 * 2. Immutability terjaga
 * 3. Helper functions bekerja dengan benar
 */
class MediaPlaybackStateTest {
    
    // ═══════════════════════════════════════════════════════════════
    // Test Fixtures (data yang dipakai berulang)
    // ═══════════════════════════════════════════════════════════════
    
    private val testTabId = "test-tab-123"
    private val mockMediaSession = mockk<BrowserMediaSession>()
    
    // ═══════════════════════════════════════════════════════════════
    // Tests untuk Constructor / Default Values
    // ═══════════════════════════════════════════════════════════════
    
    @Nested
    @DisplayName("Constructor Tests")
    inner class ConstructorTests {
        
        @Test
        fun `when created with minimal params then default values should be set`() {
            // Arrange & Act
            val state = MediaPlaybackState(
                tabId = testTabId,
                mediaSession = mockMediaSession
            )
            
            // Assert
            assertEquals(testTabId, state.tabId)
            assertEquals(mockMediaSession, state.mediaSession)
            assertEquals(BrowserMediaState.PAUSE, state.playbackState) // default
            assertNull(state.title)
            assertNull(state.artist)
            assertNull(state.album)
            assertNull(state.artwork)
        }
        
        @Test
        fun `when created with all params then all values should be set`() {
            // Arrange
            val title = "Test Song"
            val artist = "Test Artist"
            
            // Act
            val state = MediaPlaybackState(
                tabId = testTabId,
                mediaSession = mockMediaSession,
                playbackState = BrowserMediaState.PLAY,
                title = title,
                artist = artist
            )
            
            // Assert
            assertEquals(BrowserMediaState.PLAY, state.playbackState)
            assertEquals(title, state.title)
            assertEquals(artist, state.artist)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Tests untuk withPlaybackState()
    // ═══════════════════════════════════════════════════════════════
    
    @Nested
    @DisplayName("withPlaybackState Tests")
    inner class WithPlaybackStateTests {
        
        @Test
        fun `should return new instance with updated playback state`() {
            // Arrange
            val original = MediaPlaybackState(
                tabId = testTabId,
                mediaSession = mockMediaSession,
                playbackState = BrowserMediaState.PAUSE
            )
            
            // Act
            val updated = original.withPlaybackState(BrowserMediaState.PLAY)
            
            // Assert - New instance has new state
            assertEquals(BrowserMediaState.PLAY, updated.playbackState)
        }
        
        @Test
        fun `should not modify original instance (immutability)`() {
            // Arrange
            val original = MediaPlaybackState(
                tabId = testTabId,
                mediaSession = mockMediaSession,
                playbackState = BrowserMediaState.PAUSE
            )
            
            // Act
            original.withPlaybackState(BrowserMediaState.PLAY)
            
            // Assert - Original unchanged
            assertEquals(BrowserMediaState.PAUSE, original.playbackState)
        }
        
        @Test
        fun `should preserve other properties when changing state`() {
            // Arrange
            val original = MediaPlaybackState(
                tabId = testTabId,
                mediaSession = mockMediaSession,
                playbackState = BrowserMediaState.PAUSE,
                title = "Original Title",
                artist = "Original Artist"
            )
            
            // Act
            val updated = original.withPlaybackState(BrowserMediaState.PLAY)
            
            // Assert - Other properties preserved
            assertEquals(original.tabId, updated.tabId)
            assertEquals(original.mediaSession, updated.mediaSession)
            assertEquals(original.title, updated.title)
            assertEquals(original.artist, updated.artist)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Tests untuk withMetadata()
    // ═══════════════════════════════════════════════════════════════
    
    @Nested
    @DisplayName("withMetadata Tests")
    inner class WithMetadataTests {
        
        @Test
        fun `should return new instance with updated metadata`() {
            // Arrange
            val original = MediaPlaybackState(
                tabId = testTabId,
                mediaSession = mockMediaSession
            )
            val newTitle = "New Title"
            val newArtist = "New Artist"
            
            // Act
            val updated = original.withMetadata(
                newTitle = newTitle,
                newArtist = newArtist,
                newAlbum = null,
                newArtwork = null
            )
            
            // Assert
            assertEquals(newTitle, updated.title)
            assertEquals(newArtist, updated.artist)
        }
        
        @Test
        fun `should preserve playback state when updating metadata`() {
            // Arrange
            val original = MediaPlaybackState(
                tabId = testTabId,
                mediaSession = mockMediaSession,
                playbackState = BrowserMediaState.PLAY
            )
            
            // Act
            val updated = original.withMetadata(
                newTitle = "Title",
                newArtist = null,
                newAlbum = null,
                newArtwork = null
            )
            
            // Assert
            assertEquals(BrowserMediaState.PLAY, updated.playbackState)
        }
    }
}
```

### Step 3: Jalankan Test

```bash
./gradlew test --tests "MediaPlaybackStateTest"
```

### Step 4: Lihat Hasilnya

- ✅ Hijau = Test pass
- ❌ Merah = Test gagal (ada bug atau test salah)

---

## 6. Test Cases untuk Media Notification

Berikut daftar test cases yang perlu diimplementasikan untuk fitur media notification.

### 6.1 MediaPlaybackState Tests

| Test Case | Deskripsi | Priority |
|-----------|-----------|----------|
| Default playback state is PAUSE | Saat dibuat tanpa specify state, default PAUSE | High |
| withPlaybackState returns new instance | Immutability check | High |
| withPlaybackState preserves other fields | Tidak merusak data lain | High |
| withMetadata updates correctly | Metadata update works | Medium |
| withMetadata from BrowserMediaMetadata | Overload function works | Medium |

### 6.2 MediaNotificationBuilder Tests

| Test Case | Deskripsi | Priority |
|-----------|-----------|----------|
| Build notification with PLAY state shows pause button | Action button correct | High |
| Build notification with PAUSE state shows play button | Action button correct | High |
| Notification has correct channel ID | Channel setup | High |
| Notification includes metadata when available | Title, artist shown | Medium |
| Notification works without metadata | Graceful fallback | Medium |
| PendingIntents are correctly configured | Click actions work | High |

### 6.3 MediaSessionController Tests

| Test Case | Deskripsi | Priority |
|-----------|-----------|----------|
| Initialize creates MediaSession | Setup works | High |
| Update state to PLAY sets correct PlaybackState | State mapping | High |
| Update state to PAUSE sets correct PlaybackState | State mapping | High |
| Metadata is updated correctly | Metadata propagation | Medium |
| Callback actions trigger correct events | Play/pause buttons | High |
| Release cleans up MediaSession | No memory leak | Medium |

### 6.4 MediaPlaybackManager Tests

| Test Case | Deskripsi | Priority |
|-----------|-----------|----------|
| MediaActivatedEvent starts tracking | Service lifecycle | High |
| MediaDeactivatedEvent stops service after debounce | Debounce works | High |
| Rapid deactivate-activate cancels debounce | Race condition fix | Critical |
| MediaStateChanged updates service | State forwarding | High |
| First state change starts service with correct state | Initial state bug fix | Critical |
| Different tab events are ignored | Tab isolation | Medium |

### 6.5 Phase 2: GeckoBrowserSession Tests

| Test Case | Deskripsi | Priority |
|-----------|-----------|----------|
| onActivated sets activeMediaSession | Session tracking | Critical |
| onDeactivated clears activeMediaSession | Cleanup | Critical |
| activeMediaSession persists across callback recreations | Main Phase 2 goal | Critical |
| onPlay with null session logs warning | Defensive | Medium |

---

## 7. Tips & Best Practices

### ✅ DO (Lakukan)

1. **Test satu hal per test**
   ```kotlin
   // ✅ Bagus - fokus
   @Test
   fun `should update playback state`() { ... }
   
   @Test
   fun `should preserve metadata when updating state`() { ... }
   
   // ❌ Buruk - test banyak hal
   @Test
   fun `should update state and preserve metadata and not be null`() { ... }
   ```

2. **Nama test yang deskriptif**
   ```kotlin
   // ✅ Jelas apa yang di-test
   fun `when media deactivated during debounce period then service should not stop`()
   
   // ❌ Tidak jelas
   fun `testDebounce`()
   ```

3. **Arrange-Act-Assert dengan jelas**
   ```kotlin
   @Test
   fun `example`() {
       // Arrange
       val input = createTestInput()
       
       // Act
       val result = systemUnderTest.process(input)
       
       // Assert
       assertEquals(expected, result)
   }
   ```

4. **Test edge cases**
   - Null values
   - Empty strings
   - Boundary conditions
   - Error scenarios

5. **Independent tests**
   - Setiap test harus bisa jalan sendiri
   - Tidak bergantung pada test lain
   - Tidak bergantung pada urutan

### ❌ DON'T (Hindari)

1. **Test implementation details**
   ```kotlin
   // ❌ Buruk - test private method behavior
   @Test
   fun `internal helper should format correctly`() { ... }
   
   // ✅ Bagus - test public behavior
   @Test
   fun `notification title should be formatted correctly`() { ... }
   ```

2. **Test yang terlalu brittle**
   ```kotlin
   // ❌ Buruk - break kalau message sedikit berubah
   assertEquals("Error: Invalid input at position 5", error.message)
   
   // ✅ Bagus - fokus ke behavior
   assertTrue(error.message.contains("Invalid input"))
   ```

3. **Mock everything**
   ```kotlin
   // ❌ Over-mocking
   val mockString = mockk<String>()
   every { mockString.length } returns 5
   
   // ✅ Use real objects when simple
   val realString = "hello"
   ```

4. **Ignore failing tests**
   ```kotlin
   // ❌ JANGAN
   @Ignore("fix later")
   @Test
   fun `important test`() { ... }
   ```

---

## 8. Common Mistakes

### Mistake 1: Tidak Mengisolasi Test

```kotlin
// ❌ Buruk - shared mutable state
class BadTest {
    val items = mutableListOf<String>()  // Shared!
    
    @Test
    fun `test1`() {
        items.add("a")
        assertEquals(1, items.size)
    }
    
    @Test
    fun `test2`() {
        items.add("b")
        assertEquals(1, items.size)  // FAIL! items masih ada "a"
    }
}

// ✅ Bagus - fresh state per test
class GoodTest {
    private lateinit var items: MutableList<String>
    
    @BeforeEach
    fun setup() {
        items = mutableListOf()  // Fresh setiap test
    }
    
    @Test
    fun `test1`() { ... }
    
    @Test
    fun `test2`() { ... }
}
```

### Mistake 2: Testing Kotlin Data Class Equals

```kotlin
// ❌ Tidak perlu - Kotlin generate ini otomatis
@Test
fun `equals should work`() {
    val a = State(id = 1)
    val b = State(id = 1)
    assertEquals(a, b)  // Ini selalu pass untuk data class
}

// ✅ Lebih berguna - test business logic
@Test
fun `copy should create new instance with updated field`() {
    val original = State(id = 1, name = "test")
    val copied = original.copy(name = "updated")
    
    assertEquals("updated", copied.name)
    assertEquals(1, copied.id)  // preserved
}
```

### Mistake 3: Assertion yang Terlalu Lemah

```kotlin
// ❌ Buruk - hanya cek tidak null
@Test
fun `should return result`() {
    val result = calculate(5)
    assertNotNull(result)  // Terlalu lemah!
}

// ✅ Bagus - cek nilai sebenarnya
@Test
fun `should return doubled value`() {
    val result = calculate(5)
    assertEquals(10, result)
}
```

### Mistake 4: Async Testing Salah

```kotlin
// ❌ Buruk - tidak handle async
@Test
fun `async test`() {
    var result: String? = null
    
    fetchData { data ->
        result = data
    }
    
    assertEquals("expected", result)  // FAIL! Callback belum dipanggil
}

// ✅ Bagus - pakai coroutine test
@Test
fun `async test with coroutines`() = runTest {
    val result = fetchDataSuspend()
    assertEquals("expected", result)
}
```

---

## 9. Resources untuk Belajar Lebih Lanjut

### Dokumentasi Resmi

1. **JUnit 5 User Guide**
   - https://junit.org/junit5/docs/current/user-guide/
   - Panduan lengkap JUnit 5

2. **MockK Documentation**
   - https://mockk.io/
   - Mocking library untuk Kotlin

3. **Kotlin Coroutines Testing**
   - https://kotlinlang.org/docs/coroutines-guide.html
   - Testing coroutines

4. **Android Testing**
   - https://developer.android.com/training/testing
   - Testing di Android

### Video Tutorials (Recommended untuk Pemula)

1. **Philipp Lackner - Android Testing**
   - YouTube channel dengan banyak tutorial testing Android
   - Bahasa sederhana, step by step

2. **Coding with Mitch - Unit Testing**
   - Tutorial unit testing Android dengan MockK

### Books

1. **"Test-Driven Development by Example" - Kent Beck**
   - Classic book tentang TDD
   - Mengajarkan mindset testing

2. **"Unit Testing Principles, Practices, and Patterns" - Vladimir Khorikov**
   - Best practices unit testing
   - Kapan mock, kapan tidak

### Practice

1. **Mulai dari yang simpel**
   - Test pure functions dulu
   - Tidak ada dependencies
   
2. **Tambah complexity bertahap**
   - Tambah mocking
   - Test async code
   - Test Android components

3. **Code Kata**
   - https://kata-log.rocks/
   - Latihan coding dengan focus TDD

---

## Appendix: Quick Reference

### Assertions Cheat Sheet

```kotlin
// Equality
assertEquals(expected, actual)
assertNotEquals(unexpected, actual)

// Boolean
assertTrue(condition)
assertFalse(condition)

// Null
assertNull(value)
assertNotNull(value)

// Exception
assertThrows<ExceptionType> { 
    codeYangThrowException() 
}

// Collections
assertIterableEquals(expectedList, actualList)
assertTrue(list.isEmpty())
assertEquals(3, list.size)

// With message (untuk debugging)
assertEquals(expected, actual, "Custom message jika gagal")
```

### MockK Cheat Sheet

```kotlin
// Create mock
val mock = mockk<MyClass>()

// Define behavior
every { mock.method() } returns "value"
every { mock.method(any()) } returns "value"  // any param
every { mock.suspendMethod() } coAnswers { "value" }  // suspend

// Verify calls
verify { mock.method() }
verify(exactly = 2) { mock.method() }  // called exactly 2 times
verify { mock.method(match { it > 5 }) }  // with matcher

// Capture arguments
val slot = slot<String>()
every { mock.method(capture(slot)) } returns Unit
// Later: slot.captured contains the value

// Relaxed mock (auto-stub)
val relaxed = mockk<MyClass>(relaxed = true)
```

### Test Lifecycle

```kotlin
class MyTest {
    @BeforeAll   // Sekali sebelum semua test di class
    fun setupClass() { }
    
    @BeforeEach  // Sebelum setiap test
    fun setup() { }
    
    @Test
    fun test1() { }
    
    @Test
    fun test2() { }
    
    @AfterEach   // Setelah setiap test
    fun teardown() { }
    
    @AfterAll    // Sekali setelah semua test di class
    fun teardownClass() { }
}
```

---

> **Catatan Akhir:**
> 
> Testing adalah skill yang berkembang seiring waktu. Jangan khawatir kalau awalnya terasa sulit atau lambat. Dengan latihan, menulis test akan menjadi natural dan bahkan menyenangkan!
> 
> Mulai dari yang kecil, dan iterasi. Setiap test yang kamu tulis adalah investasi untuk masa depan codebase.
