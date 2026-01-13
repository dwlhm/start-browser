# Git Workflow Guide

> Panduan Git workflow berdasarkan best practices industri.
>
> **Based on:**
> - [GitHub Flow](https://docs.github.com/en/get-started/using-github/github-flow) - Used by GitHub, simple & effective
> - [Conventional Commits](https://www.conventionalcommits.org/) - Used by Angular, Vue, Electron, etc.
> - [Trunk Based Development](https://trunkbaseddevelopment.com/) - Used by Google, Facebook

---

## 🌳 Branching Strategy: GitHub Flow

### Overview

```
main (production-ready)
  │
  ├── feature/bookmark-list ──────────────────────▶ PR ──▶ merge
  │
  ├── feature/dark-mode ──────────────────────────▶ PR ──▶ merge
  │
  ├── fix/crash-on-startup ───────────────────────▶ PR ──▶ merge
  │
  └── ... (short-lived branches)
```

### Rules

1. **`main` selalu deployable** — Kode di `main` harus selalu bisa di-release
2. **Branch dari `main`** — Semua feature branch dibuat dari `main`
3. **Short-lived branches** — Branch hidup maksimal beberapa hari, bukan minggu
4. **PR untuk merge** — Semua perubahan masuk via Pull Request
5. **Delete after merge** — Hapus branch setelah di-merge

---

## 🏷️ Branch Naming Convention

### Format

```
<type>/<short-description>
```

### Types

| Type | Kegunaan | Contoh |
|------|----------|--------|
| `feature/` | Fitur baru | `feature/bookmark-sync` |
| `fix/` | Bug fix | `fix/media-notification-crash` |
| `refactor/` | Refactoring tanpa ubah behavior | `refactor/session-management` |
| `docs/` | Dokumentasi | `docs/api-guide` |
| `test/` | Menambah/fix test | `test/bookmark-repository` |
| `chore/` | Maintenance (deps, config) | `chore/update-dependencies` |

### Guidelines

```bash
# ✅ Good: lowercase, hyphen-separated, descriptive
feature/bookmark-folder-support
fix/webview-memory-leak
refactor/clean-up-media-state

# ❌ Bad: uppercase, underscore, too vague
Feature/BookmarkFolder
fix/bug
feature/update
```

### Dengan Issue/Ticket Number (Opsional)

```bash
# Jika pakai issue tracker
feature/123-bookmark-sync
fix/456-crash-on-rotate
```

---

## 📝 Commit Message Convention: Conventional Commits

### Format

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

### Type

| Type | Deskripsi | Contoh |
|------|-----------|--------|
| `feat` | Fitur baru | `feat(bookmark): add folder support` |
| `fix` | Bug fix | `fix(media): resolve notification not showing` |
| `refactor` | Refactor tanpa ubah behavior | `refactor(session): simplify lifecycle` |
| `docs` | Dokumentasi | `docs: add API documentation` |
| `test` | Menambah/fix test | `test(bookmark): add repository tests` |
| `chore` | Maintenance | `chore: update gradle to 8.2` |
| `style` | Formatting, whitespace | `style: fix indentation` |
| `perf` | Performance improvement | `perf(webview): optimize image loading` |
| `ci` | CI/CD changes | `ci: add github actions workflow` |
| `build` | Build system changes | `build: configure proguard rules` |
| `revert` | Revert commit sebelumnya | `revert: feat(bookmark): add sync` |

### Scope (Opsional tapi Recommended)

Scope = module atau area yang diubah

```bash
feat(bookmark): add delete functionality
fix(media): resolve state sync issue
refactor(webview): extract navigation logic
test(home): add screen tests
```

Scope yang umum untuk project ini:
- `app`, `bookmark`, `media`, `session`, `tab`, `webview`, `home`, `navigation`, `ui`

### Subject

- Gunakan imperative mood: "add" bukan "added" atau "adds"
- Huruf kecil di awal
- Tanpa titik di akhir
- Maksimal 50 karakter

```bash
# ✅ Good
feat(bookmark): add folder creation dialog
fix(media): stop playback when tab closed

# ❌ Bad
feat(bookmark): Added folder creation dialog.   # past tense, titik
fix(media): Fixes the playback issue            # tidak imperative
feat: update stuff                               # tidak deskriptif
```

### Body (Opsional)

Untuk penjelasan lebih detail. Wrap di 72 karakter.

```
feat(session): add background playback support

Implement session persistence for background media playback.
This allows users to continue listening to audio when the app
is in the background.

- Add SessionManager for lifecycle handling
- Implement foreground service for media
- Add notification controls
```

### Footer (Opsional)

```
feat(bookmark): add sync with server

BREAKING CHANGE: bookmark database schema changed, 
migration required.

Closes #123
```

### Quick Examples

```bash
# Simple feature
git commit -m "feat(bookmark): add bookmark to folder"

# Bug fix with scope
git commit -m "fix(webview): resolve crash on invalid URL"

# Refactoring
git commit -m "refactor(media): extract notification builder"

# Documentation
git commit -m "docs: add git workflow guide"

# With body (use editor)
git commit
# Opens editor:
feat(session): implement session restoration

Sessions are now persisted to disk and restored when the app
restarts. This enables:
- Resume browsing after app killed
- Maintain tab history
- Preserve scroll position

Closes #456
```

---

## 🔄 Workflow: Day-to-Day

### Starting New Work

```bash
# 1. Pastikan main up-to-date
git checkout main
git pull origin main

# 2. Buat branch baru
git checkout -b feature/bookmark-folders

# 3. Kerjakan...
```

### During Development

```bash
# Commit secara atomic (satu commit = satu logical change)
git add app/src/main/java/feature/bookmark/
git commit -m "feat(bookmark): add folder entity and dao"

git add app/src/main/java/feature/bookmark/ui/
git commit -m "feat(bookmark): add folder selection dialog"

# Push regularly (backup + visibility)
git push origin feature/bookmark-folders
```

### Keeping Branch Updated

```bash
# Jika main sudah berubah, rebase branch kamu
git checkout main
git pull origin main
git checkout feature/bookmark-folders
git rebase main

# Resolve conflicts jika ada, lalu:
git push origin feature/bookmark-folders --force-with-lease
```

### Creating Pull Request

1. Push final changes
2. Buat PR di GitHub/GitLab
3. Isi deskripsi dengan:
   - Apa yang diubah
   - Kenapa diubah
   - Cara test
   - Screenshots (jika UI)

### After PR Merged

```bash
# Kembali ke main
git checkout main
git pull origin main

# Hapus branch lokal
git branch -d feature/bookmark-folders

# Hapus branch remote (biasanya auto via PR settings)
git push origin --delete feature/bookmark-folders
```

---

## 📋 PR (Pull Request) Best Practices

### PR Size

| Size | Lines Changed | Review Time | Recommendation |
|------|---------------|-------------|----------------|
| Small | < 200 | < 30 min | ✅ Ideal |
| Medium | 200-400 | 30-60 min | ⚠️ Acceptable |
| Large | 400+ | > 1 hour | ❌ Split if possible |

### PR Title

Ikuti format Conventional Commits:

```
feat(bookmark): add folder support
fix(media): resolve notification not showing
refactor(session): simplify lifecycle management
```

### PR Description Template

```markdown
## Summary
[Jelaskan singkat apa yang diubah]

## Changes
- [Change 1]
- [Change 2]
- [Change 3]

## Why
[Kenapa perubahan ini diperlukan]

## How to Test
1. [Step 1]
2. [Step 2]
3. [Expected result]

## Screenshots (if UI changes)
[Before/After screenshots]

## Checklist
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] Self-reviewed
- [ ] No breaking changes (or documented)

## Related Issues
Closes #123
```

### Review Process

1. **Self-review dulu** — Baca diff sendiri sebelum minta review
2. **Request review** — Tag reviewer yang relevan
3. **Address feedback** — Push fix commits atau diskusi jika tidak setuju
4. **Squash jika perlu** — Cleanup commits sebelum merge

---

## 🔀 Merge Strategy

### Options

| Strategy | Kapan Pakai | Pros | Cons |
|----------|-------------|------|------|
| **Squash & Merge** | Default | Clean history | Lose individual commits |
| **Rebase & Merge** | Preserve commits | Linear history | Must be clean |
| **Merge Commit** | Large features | Complete history | Messy graph |

### Recommended: Squash & Merge

- Gunakan untuk kebanyakan PR
- Hasil: 1 commit per PR di `main`
- PR title jadi commit message

---

## 🚨 Handling Common Scenarios

### Fixing Commit Message

```bash
# Fix commit terakhir
git commit --amend -m "fix(media): correct notification icon"

# Jika sudah push
git push --force-with-lease
```

### Squashing Local Commits

```bash
# Squash 3 commit terakhir menjadi 1
git rebase -i HEAD~3

# Di editor, ubah 'pick' menjadi 'squash' untuk commit yang mau digabung
pick abc1234 feat(bookmark): add entity
squash def5678 feat(bookmark): add dao
squash ghi9012 feat(bookmark): add repository
```

### Undoing Last Commit (belum push)

```bash
# Keep changes, undo commit
git reset --soft HEAD~1

# Discard changes completely
git reset --hard HEAD~1
```

### Reverting Pushed Commit

```bash
# Buat commit baru yang membatalkan commit sebelumnya
git revert <commit-hash>
git push
```

### Stashing Work in Progress

```bash
# Simpan sementara
git stash push -m "WIP: bookmark dialog"

# Lihat stash list
git stash list

# Apply kembali
git stash pop
```

---

## 📊 Git Hygiene

### Do's ✅

- Commit sering, push regularly
- Write meaningful commit messages
- Keep branches short-lived
- Rebase instead of merge untuk update branch
- Delete merged branches
- Review your own PR sebelum minta review

### Don'ts ❌

- Jangan commit langsung ke `main`
- Jangan commit file yang di-generate (build/, .gradle/)
- Jangan commit secrets/credentials
- Jangan force push ke shared branches
- Jangan buat PR yang terlalu besar
- Jangan ignore failing tests/lints

---

## 🛠️ Useful Git Aliases

Tambahkan ke `~/.gitconfig`:

```ini
[alias]
    # Status singkat
    s = status -s
    
    # Log yang readable
    lg = log --oneline --graph --decorate -20
    
    # Commit dengan message
    cm = commit -m
    
    # Amend tanpa edit message
    amend = commit --amend --no-edit
    
    # Checkout branch
    co = checkout
    
    # Branch baru
    cob = checkout -b
    
    # Push dengan set upstream
    pushup = push -u origin HEAD
    
    # Lihat branches
    br = branch -v
    
    # Delete merged branches
    cleanup = "!git branch --merged | grep -v '\\*\\|main\\|master' | xargs -n 1 git branch -d"
```

Usage:

```bash
git s                           # status
git lg                          # log
git cm "feat: add feature"      # commit
git cob feature/new-thing       # new branch
git pushup                      # push & set upstream
```

---

## 📋 Quick Reference Card

### Branch

```bash
git checkout -b feature/name    # Buat branch
git push -u origin HEAD         # Push pertama kali
git branch -d feature/name      # Hapus branch lokal
```

### Commit

```bash
git add -p                      # Add interaktif
git commit -m "type: message"   # Commit
git commit --amend              # Edit commit terakhir
```

### Sync

```bash
git pull --rebase origin main   # Update dari main
git push --force-with-lease     # Force push (safe)
```

### Undo

```bash
git reset --soft HEAD~1         # Undo commit, keep changes
git stash                       # Simpan sementara
git stash pop                   # Restore stash
```

---

## 🔗 References

- [Conventional Commits](https://www.conventionalcommits.org/)
- [GitHub Flow](https://docs.github.com/en/get-started/using-github/github-flow)
- [Git Best Practices](https://sethrobertson.github.io/GitBestPractices/)
- [How to Write a Git Commit Message](https://cbea.ms/git-commit/)
- [Trunk Based Development](https://trunkbaseddevelopment.com/)

---

*Last updated: 2026-01-14*
