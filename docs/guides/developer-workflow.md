# Developer Workflow Guide

> Panduan personal untuk mengerjakan task dari grooming sampai selesai.
> Tujuan: Mengurangi "kesana-kemari" saat coding dengan berpikir terstruktur.

---

## 🔄 Overview: Task Lifecycle

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│   GROOMING          PLANNING           CODING           REVIEW     │
│                                                                     │
│   ┌─────────┐      ┌─────────┐      ┌─────────┐      ┌─────────┐  │
│   │ Pahami  │ ──▶  │  Spike  │ ──▶  │Implement│ ──▶  │   PR    │  │
│   │  Task   │      │  + Doc  │      │  Clean  │      │  + Doc  │  │
│   └─────────┘      └─────────┘      └─────────┘      └─────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Phase 1: Grooming / Menerima Task

### Checklist Saat Grooming

- [ ] **Pahami goal** — Apa yang mau dicapai? (bukan HOW, tapi WHAT & WHY)
- [ ] **Catat acceptance criteria** — Kapan task dianggap selesai?
- [ ] **Identifikasi scope** — Apa yang IN dan OUT of scope?
- [ ] **Tanya hal yang ambigu** — Jangan asumsi, tanyakan!
- [ ] **Identifikasi dependencies** — Butuh API dari tim lain? Butuh design?
- [ ] **Flag jika perlu spike** — "Aku butuh eksplorasi dulu sebelum estimasi"

### Output Phase Ini

```
📝 Task Notes (informal, di mana aja - notion, txt, notes app)

Task: [Nama Task]
Goal: [Apa yang mau dicapai]
Acceptance Criteria:
  - [ ] ...
  - [ ] ...
Scope: 
  - IN: ...
  - OUT: ...
Questions:
  - ...
Dependencies:
  - ...
Need Spike: Yes/No
```

---

## 📋 Phase 2: Spike & Planning

### Kapan Butuh Spike?

Spike diperlukan jika:
- [ ] Belum yakin approach teknis yang tepat
- [ ] Belum familiar dengan bagian codebase yang akan diubah
- [ ] Melibatkan library/API yang belum pernah dipakai
- [ ] Task kompleks dengan banyak kemungkinan solusi

### Cara Melakukan Spike

1. **Timebox** — Set batas waktu (30 menit - 2 jam max)
2. **Fokus validasi** — Tujuannya: "Apakah approach ini bisa jalan?"
3. **Boleh berantakan** — Kode spike bukan kode final
4. **Catat findings** — Tulis apa yang dipelajari

### Spike Notes Template

Simpan di: `docs/notes/spike-[nama-fitur].md`

```markdown
# Spike: [Nama Fitur/Task]

## Tanggal
[YYYY-MM-DD]

## Goal Spike
Apa yang mau divalidasi?

## Time Spent
[X jam]

## Approach yang Dicoba

### Approach 1: [Nama]
- Deskripsi: ...
- Hasil: ✅ Works / ❌ Tidak work
- Catatan: ...

### Approach 2: [Nama] (jika ada)
- ...

## Code Snippet yang Works
```kotlin
// paste kode yang berhasil
```

## Findings
- [Hal yang dipelajari]
- [Hal yang dipelajari]

## Blockers / Open Questions
- [Jika ada]

## Conclusion
[Approach mana yang dipilih dan kenapa]

## Next Steps
1. ...
2. ...
```

---

## 📋 Phase 3: Technical Design (untuk task kompleks)

### Kapan Butuh Design Doc?

- [ ] Task memakan waktu > 2-3 hari
- [ ] Melibatkan perubahan di multiple modules
- [ ] Ada keputusan arsitektur yang perlu dipertimbangkan
- [ ] Perlu alignment dengan tim

### Design Doc Template

Simpan di: `docs/design/[nama-fitur].md`

```markdown
# Design: [Nama Fitur]

## Status
Draft / In Review / Approved / Implemented

## Overview
[1-2 kalimat: Fitur ini tentang apa]

## Goals
- [Apa yang ingin dicapai]
- [Apa yang ingin dicapai]

## Non-Goals
- [Apa yang BUKAN scope kali ini]
- [Apa yang BUKAN scope kali ini]

## Current State (jika relevan)
[Kondisi sekarang sebelum ada fitur ini]

## Proposed Solution

### High-Level Design
[Jelaskan approach secara umum]

### Data Layer
- Entity: ...
- Dao: ...
- Repository: ...

### Domain Layer
- UseCase: ...

### UI Layer
- ViewModel: ...
- Screen: ...

### Flow Diagram
```
[User Action] → [ViewModel] → [UseCase] → [Repository] → [DB/API]
```

## Implementation Plan
1. [ ] Step 1: ...
2. [ ] Step 2: ...
3. [ ] Step 3: ...

## Alternatives Considered
| Alternative | Pros | Cons | Verdict |
|-------------|------|------|---------|
| [Opsi 1] | ... | ... | Rejected |
| [Opsi 2] | ... | ... | Rejected |

## Open Questions
- [ ] [Pertanyaan yang belum terjawab]

## References
- [Link ke ADR jika ada]
- [Link ke referensi lain]
```

---

## 📋 Phase 4: Implementation

### Sebelum Coding

- [ ] Spike sudah dilakukan (jika perlu)
- [ ] Design doc sudah dibuat (jika task kompleks)
- [ ] Sudah clear dengan approach yang akan dipakai
- [ ] Sudah tau urutan langkah yang akan dikerjakan

### Working Notes Template

Untuk catatan selama ngoding. Simpan di mana aja yang nyaman (bisa txt lokal, bisa notion).

```markdown
# Working Notes: [Nama Task]

## Task Link
[Link ke Jira/Linear/dll]

## Related Docs
- Design: docs/design/xxx.md
- Spike: docs/notes/spike-xxx.md

## Implementation Checklist
- [ ] Step 1: ...
- [ ] Step 2: ...
- [ ] Step 3: ...
- [ ] Tests
- [ ] Manual testing

## Progress Log
### [Tanggal]
- [Apa yang dikerjakan]
- [Blocker/Issue yang ditemui]
- [Solusi]

## Code Locations
- [Module/file yang diubah dan kenapa]

## Decisions Made
- [Keputusan kecil yang diambil selama coding]

## TODO / Follow-up
- [ ] [Hal yang perlu di-follow up nanti]
```

### Tips Saat Coding

1. **Commit kecil-kecil** — Lebih mudah di-review dan di-revert
2. **Test setiap langkah** — Jangan numpuk testing di akhir
3. **Update checklist** — Centang yang sudah selesai
4. **Catat keputusan** — Kenapa pilih A bukan B

---

## 📋 Phase 5: Review & Documentation

### Sebelum Create PR

- [ ] Kode sudah clean (bukan kode spike)
- [ ] Tests sudah ditulis dan pass
- [ ] Manual testing sudah dilakukan
- [ ] Self-review: baca ulang diff-nya

### Kapan Butuh ADR?

Buat ADR jika ada keputusan arsitektur yang:
- [ ] Akan berdampak jangka panjang
- [ ] Perlu dijelaskan "kenapa" ke orang lain (atau diri sendiri di masa depan)
- [ ] Ada trade-off yang perlu didokumentasikan

### ADR Template

Simpan di: `docs/adr/[NNN]-[nama-keputusan].md`

```markdown
# ADR-[NNN]: [Judul Keputusan]

## Status
Proposed / Accepted / Deprecated / Superseded by ADR-XXX

## Context
[Situasi yang menyebabkan keputusan ini perlu diambil]

## Decision
[Keputusan yang diambil]

## Rationale
[Kenapa memilih ini? Apa pertimbangannya?]

## Consequences

### Positive
- [Dampak positif]

### Negative
- [Dampak negatif / trade-off]

### Neutral
- [Dampak lain]

## Alternatives Considered
1. [Alternatif 1] — Rejected karena...
2. [Alternatif 2] — Rejected karena...

## References
- [Link relevan]
```

---

## 🎯 Quick Reference: Kapan Bikin Dokumen Apa?

| Situasi | Dokumen | Lokasi |
|---------|---------|--------|
| Terima task baru | Task Notes | Personal (notion/txt) |
| Belum yakin approach | Spike Notes | `docs/notes/` |
| Task kompleks (>2 hari) | Design Doc | `docs/design/` |
| Selama coding | Working Notes | Personal |
| Ada keputusan arsitektur | ADR | `docs/adr/` |

---

## 📁 Folder Structure

```
docs/
├── adr/                    # Architecture Decision Records
│   └── NNN-*.md
├── design/                 # Technical Design Documents
│   └── *.md
├── notes/                  # Spike notes, exploration logs
│   └── spike-*.md
├── guides/                 # Panduan (termasuk file ini)
│   └── developer-workflow.md
└── roadmap/                # Sprint planning
    └── *.md
```

---

## 💡 Mindset

1. **Dokumen = Thinking Tool** — Nulis membantu menjernihkan pikiran
2. **Dokumen = Memory Extension** — Nggak perlu ingat semua di kepala
3. **Dokumen = Future Self Helper** — 3 bulan lagi kamu akan berterima kasih
4. **Nggak perlu perfect** — Yang penting membantu, bukan cantik
5. **Iterative** — Boleh update seiring waktu

---

## ✅ Task Checklist (Copy-Paste untuk Setiap Task)

```markdown
## Task: [Nama]

### Phase 1: Understanding
- [ ] Pahami goal & acceptance criteria
- [ ] Identifikasi scope (IN/OUT)
- [ ] Tanya hal yang ambigu
- [ ] Identifikasi dependencies

### Phase 2: Planning
- [ ] Spike (jika perlu): ___
- [ ] Design doc (jika kompleks): ___
- [ ] Breakdown implementation steps

### Phase 3: Implementation
- [ ] Step 1: ___
- [ ] Step 2: ___
- [ ] Step 3: ___
- [ ] Tests
- [ ] Manual testing

### Phase 4: Review
- [ ] Self-review
- [ ] Create PR
- [ ] ADR (jika ada keputusan arsitektur)
- [ ] Update dokumentasi (jika perlu)
```

---

*Last updated: 2026-01-14*
