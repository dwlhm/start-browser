# Start Browser Documentation

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

## Components

### UI Layer

- [SystemBars](ui/SystemBars.md) - Status bar management and edge-to-edge layouts

---

## Documentation Structure

```
docs/
├── adr/           # Architecture Decision Records
│                  # Dokumentasi keputusan arsitektur
│                  # Format: [nomor]-[judul].md
│
├── design/        # Technical Design Documents  
│                  # Penjelasan detail teknis
│                  # Termasuk naming, patterns, trade-offs
│
└── ui/            # UI Component Documentation
                   # Dokumentasi komponen UI
```

### Kapan Menulis ADR?

- Saat membuat keputusan arsitektur yang signifikan
- Saat memilih antara beberapa alternatif
- Saat keputusan akan sulit diubah di masa depan

### Kapan Menulis Design Doc?

- Saat implementasi cukup kompleks
- Saat perlu menjelaskan "mengapa" di balik keputusan
- Saat banyak class/function yang saling terkait
- **Saat memperbaiki bug yang melibatkan pattern/principle tertentu**
- Saat bug bisa terjadi lagi di tempat lain tanpa guidance