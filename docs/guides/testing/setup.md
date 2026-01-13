# Testing Setup

> **Panduan setup environment testing untuk project Start Browser.**

## Dependencies

Tambahkan ke setiap module yang perlu di-test:

### Unit Testing (JVM)

```kotlin
// build.gradle.kts (module level)
dependencies {
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    
    // Kotlin test
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.0")
    
    // MockK - Mocking untuk Kotlin
    testImplementation("io.mockk:mockk:1.13.8")
    
    // Coroutines testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Truth - Assertion library
    testImplementation("com.google.truth:truth:1.1.5")
    
    // Turbine - Flow testing
    testImplementation("app.cash.turbine:turbine:1.0.0")
}

// Enable JUnit 5
tasks.withType<Test> {
    useJUnitPlatform()
}
```

### Android Instrumented Testing

```kotlin
dependencies {
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```

---

## Folder Structure

Buat folder test untuk setiap module:

```
module/
├── src/
│   ├── main/
│   │   └── java/com/dwlhm/[module]/
│   │       └── ... (production code)
│   │
│   ├── test/                              ← Unit tests (JVM)
│   │   └── java/com/dwlhm/[module]/
│   │       ├── [ClassName]Test.kt
│   │       └── ...
│   │
│   └── androidTest/                       ← Instrumented tests
│       └── java/com/dwlhm/[module]/
│           └── [ClassName]InstrumentedTest.kt
```

### Contoh untuk app module

```
app/src/
├── main/java/com/dwlhm/startbrowser/
│   └── services/media/
│       ├── MediaPlaybackState.kt
│       ├── MediaNotificationBuilder.kt
│       └── MediaSessionController.kt
│
├── test/java/com/dwlhm/startbrowser/
│   └── services/media/
│       ├── MediaPlaybackStateTest.kt
│       ├── MediaNotificationBuilderTest.kt
│       └── MediaSessionControllerTest.kt
│
└── androidTest/java/com/dwlhm/startbrowser/
    └── MediaNotificationIntegrationTest.kt
```

---

## Menjalankan Tests

### Command Line

```bash
# Semua unit tests
./gradlew test

# Unit tests untuk module tertentu
./gradlew :app:test
./gradlew :core:browser:test
./gradlew :feature:tabmanager:test

# Test class tertentu
./gradlew test --tests "com.dwlhm.startbrowser.services.media.MediaPlaybackStateTest"

# Dengan output detail
./gradlew test --info

# Instrumented tests (butuh device/emulator)
./gradlew connectedAndroidTest
```

### Android Studio

1. **Run single test:** Klik icon ▶️ di sebelah kiri nama test
2. **Run test class:** Klik kanan pada file → Run 'ClassName'
3. **Run all tests in package:** Klik kanan pada folder → Run Tests

---

## Test Report

Setelah menjalankan tests, report ada di:

```
build/reports/tests/testDebugUnitTest/index.html
```

Buka dengan browser untuk melihat hasil detail.

---

## Troubleshooting

### Error: "No tests found"

```kotlin
// Pastikan test class dan method punya annotation yang benar
import org.junit.jupiter.api.Test  // JUnit 5, bukan JUnit 4!

class MyTest {
    @Test  // HARUS ada
    fun `my test`() { ... }
}
```

### Error: "MockK not initialized"

```kotlin
// Untuk unit test, gunakan mockk biasa
testImplementation("io.mockk:mockk:1.13.8")

// Untuk instrumented test, gunakan mockk-android
androidTestImplementation("io.mockk:mockk-android:1.13.8")
```

### Error: Coroutine test timeout

```kotlin
// Gunakan runTest untuk coroutine tests
@Test
fun `my async test`() = runTest {
    // test code
}
```

---

## Checklist Setup

- [ ] Tambahkan dependencies ke `build.gradle.kts`
- [ ] Buat folder `src/test/java/...`
- [ ] Buat test class pertama
- [ ] Jalankan `./gradlew test` untuk verify
- [ ] Setup berhasil jika test pass

---

## Next Steps

Setelah setup selesai, mulai dari:
1. [app-media.md](modules/app-media.md) - `MediaPlaybackStateTest` (paling mudah)
2. [core-browser.md](modules/core-browser.md) - Pure data classes
