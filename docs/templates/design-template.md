# Design: [Nama Fitur]

## Metadata
- **Author:** [Nama]
- **Created:** [YYYY-MM-DD]
- **Status:** Draft / In Review / Approved / Implemented
- **Related Task:** [Link ke Jira/Linear]

---

## Overview

[1-2 paragraf menjelaskan fitur ini tentang apa dan kenapa dibuat]

---

## Goals

Apa yang ingin dicapai:

- [ ] Goal 1
- [ ] Goal 2
- [ ] Goal 3

---

## Non-Goals

Apa yang BUKAN scope kali ini (penting untuk set boundary):

- Non-goal 1
- Non-goal 2

---

## Current State (jika relevan)

[Kondisi sekarang sebelum ada fitur ini. Skip jika fitur baru dari nol]

---

## Proposed Solution

### High-Level Design

[Jelaskan approach secara umum dalam 1-2 paragraf]

```
┌─────────┐     ┌─────────┐     ┌─────────┐
│   UI    │ ──▶ │ Domain  │ ──▶ │  Data   │
└─────────┘     └─────────┘     └─────────┘
```

### Data Layer

**Entity:**
```kotlin
data class Example(
    val id: String,
    val name: String
)
```

**Dao:**
- `ExampleDao` — operasi CRUD untuk Example

**Repository:**
- `ExampleRepository` — abstraksi data source

### Domain Layer

**UseCase:**
- `GetExampleUseCase` — ...
- `SaveExampleUseCase` — ...

### UI Layer

**ViewModel:**
- `ExampleViewModel` — mengelola state untuk Example screen

**Screen:**
- `ExampleScreen` — UI composable

### Flow Diagram

```
[User tap button]
       │
       ▼
[ExampleScreen] ──▶ [ExampleViewModel.doAction()]
                            │
                            ▼
                    [SaveExampleUseCase()]
                            │
                            ▼
                    [ExampleRepository.save()]
                            │
                            ▼
                    [ExampleDao.insert()]
                            │
                            ▼
                        [Room DB]
```

---

## Implementation Plan

Urutan langkah implementasi:

1. [ ] **Data Layer**
   - [ ] Create Entity
   - [ ] Create Dao
   - [ ] Create Repository

2. [ ] **Domain Layer**
   - [ ] Create UseCases

3. [ ] **UI Layer**
   - [ ] Create ViewModel
   - [ ] Create Screen

4. [ ] **Integration**
   - [ ] Wire up DI (Hilt modules)
   - [ ] Add navigation

5. [ ] **Testing**
   - [ ] Unit tests
   - [ ] Integration tests

---

## Alternatives Considered

| Alternative | Description | Pros | Cons | Verdict |
|-------------|-------------|------|------|---------|
| [Opsi 1] | ... | ... | ... | ❌ Rejected |
| [Opsi 2] | ... | ... | ... | ❌ Rejected |
| [Chosen] | ... | ... | ... | ✅ Selected |

**Kenapa memilih approach ini:**
[Jelaskan reasoning]

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| [Risk 1] | High/Medium/Low | [Cara mitigasi] |

---

## Open Questions

Pertanyaan yang belum terjawab:

- [ ] Question 1?
- [ ] Question 2?

---

## Dependencies

- [ ] [Dependency 1 - status]
- [ ] [Dependency 2 - status]

---

## Testing Strategy

- **Unit Tests:** ...
- **Integration Tests:** ...
- **Manual Testing:** ...

---

## Rollout Plan (jika relevan)

- [ ] Phase 1: ...
- [ ] Phase 2: ...

---

## References

- [Link ke ADR jika ada]
- [Link ke Figma/design]
- [Link ke dokumentasi terkait]
