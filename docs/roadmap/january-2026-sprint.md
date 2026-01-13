# Development Roadmap: January 2026 Sprint

> **Periode:** 14 Januari - 14 Februari 2026 (1 bulan)
> **Prepared by:** Product Manager
> **Status:** Draft

---

## Executive Summary

Sprint ini fokus pada **3 pilar utama:**

1. **Quality Foundation** - Testing coverage untuk stability
2. **Core UX Improvements** - Memperbaiki flow browsing yang masih rough
3. **Essential Features** - Fitur-fitur basic yang expected dari sebuah browser

### Sprint Goals

| Goal | Target | Priority |
|------|--------|----------|
| Test Coverage | 50+ tests | 🔴 Critical |
| Browser Toolbar Completion | 100% | 🔴 Critical |
| Tab Management UI | Working | 🟠 High |
| Settings Page | MVP | 🟡 Medium |

---

## Week 1: Foundation & Critical Fixes

### 📅 14-20 Januari 2026

> **Theme:** "Stabilize the Core"

---

### Epic 1.1: Testing Infrastructure

**Priority:** 🔴 Critical
**Owner:** Developer

#### Story 1.1.1: Setup Testing Environment

**Acceptance Criteria:**
- [ ] JUnit 5, MockK, Coroutines Test, Turbine dependencies added
- [ ] Test folders created for all modules
- [ ] `./gradlew test` runs successfully (even with 0 tests)
- [ ] CI configuration updated to run tests

**Definition of Done:**
- First dummy test passes
- Documentation updated with test commands

---

#### Story 1.1.2: Core Data Class Tests

**Acceptance Criteria:**

| Class | Tests | Status |
|-------|-------|--------|
| `MediaPlaybackState` | 8 | ⬜ |
| `BrowserMediaMetadata` | 3 | ⬜ |
| `TabInfo` | 3 | ⬜ |

**Test Examples:**
```kotlin
// MediaPlaybackState
✅ when created with minimal params then defaults should be set
✅ withPlaybackState should return new instance (immutability)
✅ withMetadata should preserve playback state
```

**Definition of Done:**
- All 14 tests passing
- No mocking required (pure unit tests)

---

### Epic 1.2: Browser Toolbar Completion

**Priority:** 🔴 Critical
**Owner:** Developer

#### Current State Analysis

```
Current Toolbar:
┌────────────────────────────────────────────────────────┐
│  [→]  │  [________________URL________________]  │  [□]  │
│Forward│              URL Input                  │ Home  │
└────────────────────────────────────────────────────────┘

❌ Missing: Back button
❌ Missing: Refresh button
❌ Missing: Loading indicator
❌ Missing: Tabs counter/switcher
❌ Missing: Menu (share, bookmarks, settings)
```

#### Story 1.2.1: Add Back Button

**Acceptance Criteria:**
- [ ] Back button visible di toolbar (icon: ←)
- [ ] Back button disabled saat `canGoBack = false`
- [ ] Back button tint berubah saat disabled (gray)
- [ ] Tap back button navigates ke halaman sebelumnya

**UI Specification:**
```
┌────────────────────────────────────────────────────────┐
│ [←] [→] │  [___________URL___________]  │  [□]  │
│Back Fwd │         URL Input             │ Home  │
└────────────────────────────────────────────────────────┘
```

---

#### Story 1.2.2: Add Refresh/Stop Button

**Acceptance Criteria:**
- [ ] Saat loading: tampilkan icon ✕ (stop)
- [ ] Saat idle: tampilkan icon ↻ (refresh)
- [ ] Tap saat loading → stop loading
- [ ] Tap saat idle → reload page

**UI Specification:**
```
┌────────────────────────────────────────────────────────┐
│ [←] [→] [↻] │  [________URL________]  │  [□]  │
│             │  (atau ✕ saat loading)  │       │
└────────────────────────────────────────────────────────┘
```

---

#### Story 1.2.3: Add Loading Progress Indicator

**Acceptance Criteria:**
- [ ] Linear progress bar di bawah URL input
- [ ] Progress bar visible hanya saat loading
- [ ] Progress bar menunjukkan actual progress (0-100%)
- [ ] Warna progress bar sesuai theme (blue atau accent color)

**UI Specification:**
```
┌────────────────────────────────────────────────────────┐
│ [←] [→] [↻] │  [________URL________]  │  [□]  │
│═══════════════════════▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░░░░│ ← Progress
└────────────────────────────────────────────────────────┘
```

---

### Epic 1.3: Critical Bug Fixes

**Priority:** 🔴 Critical

#### Story 1.3.1: Phase 2 Implementation (Session-Level Media State)

**Context:** ADR-004 - Fix callback reset issue

**Acceptance Criteria:**
- [ ] `BrowserSession` interface has `activeMediaSession` property
- [ ] `GeckoBrowserSession` tracks media session at session level
- [ ] `TabSessionManager` uses `session.activeMediaSession` instead of callback-level tracking
- [ ] `lastKnownMediaSession` workaround removed
- [ ] All existing media notification tests still pass

**Regression Test:**
```
1. Play video di YouTube
2. Switch ke tab lain, kembali ke YouTube
3. ✅ Notification masih sinkron dengan video state
4. ✅ Play/pause dari notification bekerja
```

---

## Week 2: Tab Management & Event System

### 📅 21-27 Januari 2026

> **Theme:** "Multi-Tab Experience"

---

### Epic 2.1: Tab Management UI

**Priority:** 🟠 High
**Owner:** Developer

#### Current State Analysis

```
Problem:
- TabList component exists tapi commented out di HomeScreen
- User tidak bisa switch antar tabs dari UI
- User tidak bisa lihat tabs yang terbuka
```

#### Story 2.1.1: Tab Counter Button in Toolbar

**Acceptance Criteria:**
- [ ] Button menampilkan jumlah tab yang terbuka (angka dalam kotak)
- [ ] Saat 0 tabs, tampilkan "0" atau "+"
- [ ] Saat 10+ tabs, tampilkan "9+" atau ":D"
- [ ] Tap button → navigate ke Tab Switcher screen

**UI Specification:**
```
┌────────────────────────────────────────────────────────┐
│ [←] [→] [↻] │  [___URL___]  │  [3]  │  [⋮]  │
│             │               │ tabs  │ menu  │
└────────────────────────────────────────────────────────┘
```

---

#### Story 2.1.2: Tab Switcher Screen

**Acceptance Criteria:**
- [ ] Grid/List view menampilkan semua tabs
- [ ] Setiap tab card menampilkan: favicon, title, URL preview
- [ ] Tap tab → switch ke tab tersebut & navigate ke browser
- [ ] Swipe/Close button untuk close tab
- [ ] "New Tab" button untuk buat tab baru
- [ ] Empty state saat tidak ada tabs

**UI Specification:**
```
┌─────────────────────────────────────────┐
│  Tabs (3)                    [+ New]    │
├─────────────────────────────────────────┤
│ ┌─────────────┐ ┌─────────────┐        │
│ │ 🔵 YouTube  │ │ 🟢 GitHub   │        │
│ │ Video title │ │ Repository  │        │
│ │         [×] │ │         [×] │        │
│ └─────────────┘ └─────────────┘        │
│                                         │
│ ┌─────────────┐                        │
│ │ 🔴 Gmail    │                        │
│ │ Inbox       │                        │
│ │         [×] │                        │
│ └─────────────┘                        │
└─────────────────────────────────────────┘
```

---

#### Story 2.1.3: Tab Persistence

**Acceptance Criteria:**
- [ ] Tabs survive app restart
- [ ] Tab state (URL, title, scroll position) persisted
- [ ] Restore tabs on app launch
- [ ] Option to "restore" or "start fresh" jika crash sebelumnya

---

### Epic 2.2: Event System Tests

**Priority:** 🔴 Critical

#### Story 2.2.1: EventDispatcher Tests

**Acceptance Criteria:**
- [ ] 6 tests untuk EventDispatcher
- [ ] Cover: dispatch, multiple collectors, filterIsInstance, ordering
- [ ] Use Turbine for Flow testing

**Test List:**
```kotlin
✅ dispatch should emit event to flow
✅ multiple collectors should receive same event
✅ filterIsInstance should only pass matching types
✅ dispatch should not block caller
✅ events should be received in order
✅ collector cancellation should stop receiving
```

---

## Week 3: Settings & UX Polish

### 📅 28 Januari - 3 Februari 2026

> **Theme:** "User Customization"

---

### Epic 3.1: Settings Page MVP

**Priority:** 🟡 Medium
**Owner:** Developer

#### Story 3.1.1: Settings Navigation

**Acceptance Criteria:**
- [ ] Menu button (⋮) di toolbar
- [ ] Menu item "Settings" dalam dropdown
- [ ] Navigate ke Settings screen

---

#### Story 3.1.2: Settings Screen - General

**Acceptance Criteria:**
- [ ] Section: General
  - [ ] Search Engine (dropdown: Google, DuckDuckGo, Bing)
  - [ ] Homepage URL (text input)
- [ ] Section: Privacy
  - [ ] Clear Browsing Data (button → confirmation dialog)
  - [ ] Block Trackers (toggle) - placeholder untuk future
- [ ] Section: About
  - [ ] App Version
  - [ ] Build Number
  - [ ] Open Source Licenses

**UI Specification:**
```
┌─────────────────────────────────────────┐
│  ← Settings                             │
├─────────────────────────────────────────┤
│                                         │
│  GENERAL                                │
│  ┌─────────────────────────────────┐   │
│  │ Search Engine          Google ▼ │   │
│  ├─────────────────────────────────┤   │
│  │ Homepage     https://google.com │   │
│  └─────────────────────────────────┘   │
│                                         │
│  PRIVACY                                │
│  ┌─────────────────────────────────┐   │
│  │ Clear Browsing Data          → │   │
│  ├─────────────────────────────────┤   │
│  │ Block Trackers              🔘 │   │
│  └─────────────────────────────────┘   │
│                                         │
│  ABOUT                                  │
│  ┌─────────────────────────────────┐   │
│  │ Version                  1.0.0  │   │
│  │ Licenses                     → │   │
│  └─────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
```

---

### Epic 3.2: Error Handling UX

**Priority:** 🟠 High

#### Story 3.2.1: Network Error Page

**Acceptance Criteria:**
- [ ] Custom error page saat offline/timeout
- [ ] Tampilkan: icon, message, "Try Again" button
- [ ] Retry button attempts reload
- [ ] Tidak tampilkan raw GeckoView error

**UI Specification:**
```
┌─────────────────────────────────────────┐
│                                         │
│            📡                           │
│                                         │
│     No Internet Connection              │
│                                         │
│   Check your connection and try again   │
│                                         │
│        [ Try Again ]                    │
│                                         │
└─────────────────────────────────────────┘
```

---

#### Story 3.2.2: Invalid URL Feedback

**Acceptance Criteria:**
- [ ] Saat user input invalid URL → search instead
- [ ] Toast/snackbar menginformasikan "Searching for: {query}"
- [ ] Tidak crash atau silent fail

---

### Epic 3.3: Business Logic Tests

**Priority:** 🔴 Critical

#### Story 3.3.1: MediaPlaybackManager Tests

**Acceptance Criteria:**
- [ ] 12 tests covering all scenarios
- [ ] ⚠️ Critical: debounce cancellation test
- [ ] ⚠️ Critical: initial state test

**Test List:**
```kotlin
// Activation
✅ MediaActivatedEvent should start tracking
✅ MediaDeactivatedEvent should trigger debounce
⚠️ rapid deactivate-activate should cancel debounce
✅ different tab events should be ignored

// State changes
✅ PLAY should cancel pending deactivation
⚠️ first state change should start service with correct state
✅ state change for inactive tab should be ignored

// Lifecycle
✅ should start foreground service on first media
✅ should stop service after debounce timeout
✅ destroy should cancel all jobs
```

---

## Week 4: Polish & Release Prep

### 📅 4-14 Februari 2026

> **Theme:** "Ship It!"

---

### Epic 4.1: UI/UX Polish

**Priority:** 🟡 Medium

#### Story 4.1.1: Consistent Theming

**Acceptance Criteria:**
- [ ] Define color palette (primary, secondary, background, surface)
- [ ] Apply consistent colors across all screens
- [ ] Dark mode support (basic)
- [ ] Typography consistency

**Color Palette:**
```kotlin
// Light Theme
Primary = #2196F3     // Blue
OnPrimary = #FFFFFF
Background = #FAFAFA
Surface = #FFFFFF
OnSurface = #212121
Error = #F44336

// Dark Theme
Primary = #90CAF9
Background = #121212
Surface = #1E1E1E
OnSurface = #FFFFFF
```

---

#### Story 4.1.2: Loading States

**Acceptance Criteria:**
- [ ] Skeleton loading untuk LastVisited
- [ ] Skeleton loading untuk Tab Switcher
- [ ] Pull-to-refresh di halaman yang support

---

#### Story 4.1.3: Empty States

**Acceptance Criteria:**
- [ ] Empty state untuk LastVisited (first time user)
- [ ] Empty state untuk Tab Switcher (no tabs)
- [ ] Empty state untuk Sessions list

**UI Specification:**
```
┌─────────────────────────────────────────┐
│                                         │
│           📑                            │
│                                         │
│      No tabs open                       │
│                                         │
│   Tap + to start browsing               │
│                                         │
│        [ + New Tab ]                    │
│                                         │
└─────────────────────────────────────────┘
```

---

### Epic 4.2: Feature Tests

**Priority:** 🟠 High

#### Story 4.2.1: TabSessionManager Tests

**Acceptance Criteria:**
- [ ] 10 tests untuk TabSessionManager
- [ ] Cover tab switching, media events, sync functionality

---

#### Story 4.2.2: Integration Tests

**Acceptance Criteria:**
- [ ] 5 integration tests untuk media notification flow
- [ ] Run on emulator/device

---

### Epic 4.3: Documentation & Release

**Priority:** 🟡 Medium

#### Story 4.3.1: Update Documentation

**Acceptance Criteria:**
- [ ] README updated dengan setup instructions
- [ ] ADR index complete
- [ ] Architecture diagram updated
- [ ] Contributing guide (basic)

---

#### Story 4.3.2: Release Checklist

**Acceptance Criteria:**
- [ ] Version bumped
- [ ] Changelog written
- [ ] APK built and tested
- [ ] Known issues documented

---

## Summary: Deliverables

### By End of Week 1
- ✅ Testing infrastructure setup
- ✅ 14 core data class tests
- ✅ Browser toolbar complete (back, refresh, progress)
- ✅ Phase 2 media session implemented

### By End of Week 2
- ✅ Tab counter button
- ✅ Tab switcher screen
- ✅ Tab persistence
- ✅ 6 EventDispatcher tests

### By End of Week 3
- ✅ Settings page MVP
- ✅ Error handling UX
- ✅ 12 MediaPlaybackManager tests

### By End of Week 4
- ✅ UI/UX polish (theming, loading, empty states)
- ✅ 10 TabSessionManager tests
- ✅ 5 integration tests
- ✅ Documentation complete

---

## Metrics & Success Criteria

| Metric | Target | Measurement |
|--------|--------|-------------|
| Test Coverage | 50+ tests | `./gradlew test` |
| Core Features | 100% toolbar | Manual check |
| Tab Management | Working | E2E test |
| Settings | MVP complete | Manual check |
| Crash Rate | < 1% | Crashlytics (if setup) |

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| GeckoView complexity | High | Fallback ke mock tests |
| Time overrun | Medium | Prioritize critical features |
| Testing bottleneck | Medium | Start with pure classes |

---

## Appendix: Backlog (Future Sprints)

Features yang tidak masuk sprint ini:

- **Bookmarks** - Add/view/manage bookmarks
- **History** - Full browsing history with search
- **Downloads** - Download manager
- **Find in Page** - Ctrl+F equivalent
- **Desktop Mode** - Request desktop site
- **Reader Mode** - Simplified reading view
- **Extensions** - Support for web extensions
- **Sync** - Cross-device synchronization

---

## Changelog

| Tanggal | Update |
|---------|--------|
| 2026-01-14 | Initial roadmap created |
