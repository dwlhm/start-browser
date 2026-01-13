# ADR-001: Session Lifecycle Management

| Status | Accepted |
|--------|----------|
| Tanggal | 2026-01-13 |
| Deciders | dwlhm |
| Kategori | Core Architecture |

## Konteks

Browser Start Browser menggunakan GeckoView sebagai engine rendering. Setiap tab browser direpresentasikan oleh `GeckoSession` yang memiliki dua state penting:

- **Active**: Apakah session boleh melakukan pekerjaan (render halaman, execute JavaScript, play media)
- **Focused**: Apakah session memiliki input focus (menerima keyboard/touch input)

### Masalah yang Dihadapi

1. **Resource Management**: Session yang tidak terlihat user seharusnya tidak mengonsumsi resource berlebihan
2. **Background Media Playback**: User ingin bisa memutar musik/video di background sambil menggunakan app lain
3. **Battery & Performance**: Session yang fully active di background menguras baterai dan memperlambat device

### State Matrix GeckoSession

| State | Active | Focused | Perilaku |
|-------|--------|---------|----------|
| Foreground | ✅ | ✅ | Full capability, menerima input |
| Background (Media) | ✅ | ❌ | Media bisa jalan, tidak menerima input |
| Suspended | ❌ | ❌ | Tidak ada aktivitas, hemat resource |

## Keputusan

Mengimplementasikan **Session Lifecycle Management** dengan pendekatan:

### 1. Tracking Media State di Session Level

Setiap `BrowserSession` melacak status media playback-nya sendiri secara internal.

```kotlin
// Di BrowserSession interface
val hasActiveMedia: Boolean

// Di GeckoBrowserSession implementation
private var _hasActiveMedia: Boolean = false

// Di-update oleh mediaSessionDelegate
override fun onActivated(...) { _hasActiveMedia = true }
override fun onDeactivated(...) { _hasActiveMedia = false }
```

**Alasan**: 
- Encapsulation - state media adalah tanggung jawab session sendiri
- Tidak perlu tracking eksternal yang bisa out-of-sync
- Query langsung ke source of truth

### 2. Suspend Method dengan Parameter

```kotlin
fun suspendSession(keepActive: Boolean = false)
```

**Alasan**:
- Single method untuk semua kasus suspend
- Parameter `keepActive` memberikan kontrol eksplisit
- Caller tidak perlu tahu detail implementasi GeckoView

### 3. Centralized Suspend Logic di TabSessionManager

```kotlin
fun suspendCurrentTab() {
    val currentTab = selectedTab.value ?: return
    val isPlayingMedia = currentTab.session.hasActiveMedia
    currentTab.session.suspendSession(keepActive = isPlayingMedia)
}
```

**Alasan**:
- Single point of control untuk suspend logic
- Business logic (kapan keep active) ada di satu tempat
- Mudah di-test dan di-modify

## Alternatif yang Dipertimbangkan

### Alternatif 1: Observer Pattern untuk Media State

```kotlin
class MediaStateObserver {
    private val tabsWithMedia = mutableSetOf<String>()
    
    fun onMediaActivated(tabId: String) { tabsWithMedia.add(tabId) }
    fun onMediaDeactivated(tabId: String) { tabsWithMedia.remove(tabId) }
    fun hasActiveMedia(tabId: String) = tabsWithMedia.contains(tabId)
}
```

**Ditolak karena**:
- State tracking terpisah dari session, bisa out-of-sync
- Callback bisa missed jika session callback di-null-kan
- Kompleksitas tambahan tanpa benefit signifikan

### Alternatif 2: Automatic Suspend via Lifecycle Observer

```kotlin
class SessionLifecycleObserver : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        // Auto suspend all sessions
    }
}
```

**Ditolak karena**:
- Terlalu implicit, sulit debug
- Tidak fleksibel untuk kasus khusus
- Coupling dengan Android Lifecycle

### Alternatif 3: Separate Background Tab Manager

```kotlin
class BackgroundTabManager {
    fun moveToBackground(session: BrowserSession)
    fun moveToForeground(session: BrowserSession)
}
```

**Ditolak karena**:
- Overhead class tambahan
- Duplikasi dengan TabSessionManager yang sudah ada
- Over-engineering untuk use case sederhana

## Konsekuensi

### Positif

1. **Hemat Resource**: Session yang tidak memutar media sepenuhnya suspended
2. **Background Playback**: Media tetap bisa jalan di background
3. **Modular**: Logic suspend terenkapsulasi dengan baik
4. **Testable**: Mudah di-unit test karena dependency injection friendly
5. **Readable**: Code verbose dan self-documenting

### Negatif

1. **Manual Call**: Developer harus ingat memanggil `suspendCurrentTab()` saat navigasi
2. **Potential Miss**: Jika ada navigation path baru, bisa lupa suspend
3. **GeckoView Coupling**: Logic `setActive`/`setFocused` spesifik ke GeckoView

### Risiko & Mitigasi

| Risiko | Mitigasi |
|--------|----------|
| Lupa panggil suspend saat navigasi baru | Code review checklist, unit test |
| Media state tidak ter-update | Internal tracking di session, tidak depend ke callback |
| Session bocor (tidak ter-suspend) | Logging di suspendSession untuk debugging |

## Implementasi

### File yang Dimodifikasi

```
core/browser/
└── BrowserSession.kt          # Interface + hasActiveMedia, suspendSession

engine/gecko/
└── GeckoBrowserSession.kt     # Implementation + _hasActiveMedia tracking

feature/tabmanager/
└── TabSessionManager.kt       # suspendCurrentTab() method

shell/browser/
└── BrowserShellRegistrar.kt   # Panggil suspend saat navigasi
```

### Diagram Alur

```
┌─────────────────────────────────────────────────────────────────┐
│                     User di Browser View                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ Klik "Home" / Back
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              BrowserShellRegistrar.onGoToHome()                  │
│                              │                                   │
│                              ▼                                   │
│              tabSessionManager.suspendCurrentTab()               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    TabSessionManager                             │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ val isPlayingMedia = currentTab.session.hasActiveMedia     │ │
│  │ currentTab.session.suspendSession(keepActive=isPlayingMedia)│ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   GeckoBrowserSession                            │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ session.setFocused(false)  // Selalu                       │ │
│  │ if (!keepActive) {                                         │ │
│  │     session.setActive(false)  // Hanya jika tidak ada media│ │
│  │ }                                                          │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
┌─────────────────────────┐     ┌─────────────────────────┐
│   Ada Media Playing     │     │   Tidak Ada Media       │
│   ─────────────────     │     │   ─────────────────     │
│   Active: true          │     │   Active: false         │
│   Focused: false        │     │   Focused: false        │
│   ─────────────────     │     │   ─────────────────     │
│   Media jalan di BG ✓   │     │   Fully suspended ✓     │
└─────────────────────────┘     └─────────────────────────┘
```

## Referensi

- [GeckoView Session Lifecycle](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/automation.html)
- [Mozilla: Media Session API](https://developer.mozilla.org/en-US/docs/Web/API/Media_Session_API)
- [Android Background Playback](https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice)

## Changelog

| Tanggal | Perubahan |
|---------|-----------|
| 2026-01-13 | Initial implementation |
