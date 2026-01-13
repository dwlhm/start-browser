# Start Browser Documentation

## 🗓️ Current Sprint

- [January 2026 Sprint](roadmap/january-2026-sprint.md) - 🆕 Roadmap development 1 bulan (14 Jan - 14 Feb 2026)

---

## Architecture Decision Records (ADR)

Dokumentasi keputusan arsitektur yang penting:

- [ADR-001: Session Lifecycle Management](adr/001-session-lifecycle-management.md) - Manajemen lifecycle session untuk background media playback
- [ADR-002: Media Notification Architecture](adr/002-media-notification-architecture.md) - Arsitektur sistem notifikasi media
- [ADR-003: Media State Management Refactoring](adr/003-media-state-management-refactoring.md) - Refactoring state management untuk fix sync bug
- [ADR-004: Phase 2 - Session-Level Media State](adr/004-phase2-session-level-media-state.md) - Refactoring lanjutan untuk session-level tracking

## Technical Design Documents

Penjelasan detail teknis implementasi:

- [Media Notification System](design/media-notification-system.md) - Detail implementasi sistem notifikasi media player
- [Media State Synchronization](design/media-state-synchronization.md) - Pattern untuk sinkronisasi state media saat navigasi
- [Phase 2: Session-Level Media Implementation](design/phase2-session-level-media-implementation.md) - Panduan teknis implementasi Phase 2

## Guides

Panduan praktis untuk developer:

### 🎯 Core Guides (Wajib Baca)
- [Developer Workflow](guides/developer-workflow.md) - **Panduan workflow dari grooming sampai selesai**
- [Code Standards](guides/code-standards.md) - 🆕 **Kotlin/Android coding standards & best practices**
- [Git Workflow](guides/git-workflow.md) - 🆕 **Git branching, commit conventions, PR guidelines**

### 🧪 Testing
- [Testing Guide](guides/testing-guide.md) - Panduan lengkap testing untuk pemula (unit test, mocking, best practices)
- [Testing TODO](guides/testing/) - Daftar modular test yang perlu dibuat (per module, mudah ditambah)

## Templates

Template dokumen untuk digunakan:

- [Spike Template](templates/spike-template.md) - Template untuk mencatat hasil eksplorasi/spike
- [Design Template](templates/design-template.md) - Template untuk technical design document
- [ADR Template](templates/adr-template.md) - Template untuk Architecture Decision Record
- [Task Checklist](templates/task-checklist.md) - Checklist lengkap untuk setiap task baru

## Components

### UI Layer

- [SystemBars](ui/SystemBars.md) - Status bar management and edge-to-edge layouts

---

## Documentation Structure

```
docs/
├── roadmap/          # Sprint Planning & Roadmaps
│   └── january-2026-sprint.md
│
├── adr/              # Architecture Decision Records
│                     # Format: [nomor]-[judul].md
│
├── design/           # Technical Design Documents  
│                     # Penjelasan detail teknis
│
├── notes/            # 🆕 Spike notes, exploration logs
│                     # Format: spike-[nama-fitur].md
│
├── templates/        # 🆕 Document templates
│   ├── spike-template.md
│   ├── design-template.md
│   ├── adr-template.md
│   └── task-checklist.md
│
├── guides/           # Practical Guides
│   ├── developer-workflow.md  # Workflow guide
│   ├── code-standards.md      # 🆕 Coding standards
│   ├── git-workflow.md        # 🆕 Git conventions
│   ├── testing-guide.md
│   └── testing/      # Testing TODO (modular)
│       ├── README.md        # Index + tracking
│       ├── setup.md         # Setup environment
│       ├── modules/         # Per-module test specs
│       └── integration/     # Integration tests
│
└── ui/               # UI Component Documentation
```

### Kapan Menulis Spike Notes?

- Saat belum yakin approach teknis yang tepat
- Saat belum familiar dengan bagian codebase yang akan diubah
- Saat melibatkan library/API yang belum pernah dipakai
- Saat task kompleks dengan banyak kemungkinan solusi

### Kapan Menulis Design Doc?

- Saat task memakan waktu > 2-3 hari
- Saat implementasi melibatkan perubahan di multiple modules
- Saat perlu menjelaskan "mengapa" di balik keputusan
- Saat banyak class/function yang saling terkait
- Saat memperbaiki bug yang melibatkan pattern/principle tertentu

### Kapan Menulis ADR?

- Saat membuat keputusan arsitektur yang signifikan
- Saat memilih antara beberapa alternatif
- Saat keputusan akan sulit diubah di masa depan
- Saat keputusan perlu dijelaskan "kenapa" ke tim atau diri sendiri di masa depan