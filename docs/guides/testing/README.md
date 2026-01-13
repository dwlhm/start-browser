# Testing TODO - Start Browser

> **Index dan tracking untuk semua testing di project ini.**

## Quick Links

| Kategori | Link |
|----------|------|
| 📚 Panduan Belajar | [Testing Guide](../testing-guide.md) |
| 🔧 Setup | [Setup Environment](setup.md) |

---

## Module Index

### App Layer

| Module | File | Tests | Status |
|--------|------|-------|--------|
| Media Services | [app-media.md](modules/app-media.md) | 35 | ⬜ Not Started |

### Core Layer

| Module | File | Tests | Status |
|--------|------|-------|--------|
| Browser Interfaces | [core-browser.md](modules/core-browser.md) | 8 | ⬜ Not Started |
| Event System | [core-event.md](modules/core-event.md) | 13 | ⬜ Not Started |
| Datastore | [core-datastore.md](modules/core-datastore.md) | 14 | ⬜ Not Started |

### Feature Layer

| Module | File | Tests | Status |
|--------|------|-------|--------|
| Tab Manager | [feature-tabmanager.md](modules/feature-tabmanager.md) | 26 | ⬜ Not Started |
| Home | [feature-home.md](modules/feature-home.md) | 11 | ⬜ Not Started |
| Onboarding | [feature-onboarding.md](modules/feature-onboarding.md) | 4 | ⬜ Not Started |

### Engine Layer

| Module | File | Tests | Status |
|--------|------|-------|--------|
| Gecko | [engine-gecko.md](modules/engine-gecko.md) | 14 | ⬜ Not Started |

### Integration Tests

| Suite | File | Tests | Status |
|-------|------|-------|--------|
| Media Notification | [media-notification.md](integration/media-notification.md) | 5 | ⬜ Not Started |

---

## Timeline

```
Week 1-2:  Foundation
           └── Setup + core-browser + core-event + Pure classes dari app-media

Week 3-4:  Core Business Logic  
           └── app-media (full) + core-datastore

Week 5-6:  Feature Layer
           └── feature-tabmanager + feature-home + feature-onboarding

Week 7-8:  Engine & Integration
           └── engine-gecko + integration tests
```

### Weekly Target

| Week | Focus | Target Tests |
|------|-------|--------------|
| 1 | Setup + Pure Classes | 13 |
| 2 | Event System | 18 |
| 3 | Media Services (Part 1) | 20 |
| 4 | Media Services (Part 2) + Datastore | 21 |
| 5 | Tab Manager | 18 |
| 6 | Home + Onboarding | 15 |
| 7 | Gecko Engine | 14 |
| 8 | Integration + Polish | 9 |

**Total: ~128 tests**

---

## Progress Tracking

### Status Legend

| Icon | Meaning |
|------|---------|
| ⬜ | Not Started |
| 🟡 | In Progress |
| ✅ | Complete |
| ⏸️ | Blocked |

### Overall Progress

```
Foundation:     ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ 0%
Core Business:  ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ 0%
Feature Layer:  ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ 0%
Integration:    ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ 0%
─────────────────────────────────
Total:          0 / 128 tests (0%)
```

### Milestones

- [ ] **Milestone 1:** First test passing
- [ ] **Milestone 2:** 25 tests (Week 2)
- [ ] **Milestone 3:** 50 tests (Week 4)
- [ ] **Milestone 4:** 75 tests (Week 5)
- [ ] **Milestone 5:** 100 tests (Week 7)
- [ ] **Milestone 6:** All tests complete

---

## Cara Menambah Module Baru

1. Buat file baru di `modules/[nama-module].md`
2. Gunakan template dari file module yang sudah ada
3. Update tabel di README.md ini
4. Update total test count

### Template Module Baru

```markdown
# Testing: [Nama Module]

## Overview

| Info | Value |
|------|-------|
| Path | `path/to/module` |
| Priority | 🔴 Critical / 🟠 High / 🟡 Medium / 🟢 Low |
| Phase | 1-4 |
| Est. Time | X jam |

## Files to Test

| File | Test Class | Tests | Status |
|------|------------|-------|--------|
| `File.kt` | `FileTest` | X | ⬜ |

## Test Cases

### FileTest

- [ ] test case 1
- [ ] test case 2

## Notes

(tambahan notes jika ada)
```

---

## Changelog

| Tanggal | Update |
|---------|--------|
| 2026-01-14 | Restructure ke modular format |
