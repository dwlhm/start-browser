# ADR-004: Dynamic Toolbar

| Field        | Detail        |
|:-------------|:--------------|
| **Status**   | In Progress   |
| **Date**     | 2026-01-30    |
| **Deciders** | dwlhm         |
| **Category** | UI/UX Pattern |
| **Priority** | Moderate      |

## Context

### Background

As an Android browser, toolbar component in another Android browser like Firefox, Chrome and Brave had their toolbar auto hide and appear based on user scroll activity and so do we.

### Goal

Implement auto hide and show toolbar with behaviour:
1. **Hide** when user scrolls down.
2. **Show** when user scrolls up.
3. **Show** on a new URL session/load.

---

## Decision

### New Architecture

```
┌────────────────────────────────────────────────────────────────────────────┐
│   Browser Events (ScrollDelegate: onScrollChanged)                         │
|   ─────────────────────────────────────────────────────────────────────────|
│   Responsibility:                                                          |
│   * Detect is user sedang scrolled to bottom, top or diam                  |
|   * Detect if there is new URL to be processed                             |
└──────────────────────────────────┬─────────────────────────────────────────┘
                                   |
                                   ▼
┌────────────────────────────────────────────────────────────────────────────┐
|   Browser Shell ViewModel                                                  |
|   ─────────────────────────────────────────────────────────────────────────|
|   Responsibility:                                                          |
|   * Listen to toolbar event                                                |
|   * Change state of toolbar                                                |
└────────────────────────────────────────────────────────────────────────────┘
```

### Rationale & Design Choice

* **Custom Scroll Detection**: We used this because the existing api from GeckoView isn't work properly. There is ``onShowDynamicToolbar`` and ``onHideDynamicToolbar`` events from Gecko. But because the Gecko itself native for Android View architecture not Jetpack Compose so that things practically unused for us.
* **URL Change Awareness**: To improve user security and awareness, the toolbar will automatically reappear whenever a new URL is loaded. This ensures that if a user accidentally triggers a navigation, they are immediately informed of the current site they are visiting.

---

## Plan Implementation

### Step 1: Observe Scroll event from GeckoView

```
File: engine/gecko/src/main/java/com.dwlhm.gecko/api/GeckoBrowserSession.kt
Change: Add scroll listener and toolbar event
Impact: Increased complexity in the browser engine layer due to new event-stream logic
```

### Step 2: Update BrowserShellViewModel

```
File: shell/browser/src/main/java/com/dwlhm/browser/BrowserShellViewModel.kt
Change: 
  - Add event listener for toolbar event
  - Implement dynamic toolbar
Impact: UI Layer
```

### Step 3: Update Tests

```
File: Test files for `BrowserShellViewModel` and `GeckoBrowserSession`.
Change: 
  - Add Unit Tests for `ScrollDelegate` logic to ensure correct direction detection.
  - Add UI State tests in ViewModel to verify toolbar visibility transitions.
Impact: Improved stability and regression prevention for UI behavior.
```

---

## Reference

- [GeckoView ScrollDelegate API](https://mozilla.github.io/geckoview/javadoc/mozilla-central/org/mozilla/geckoview/GeckoSession.ScrollDelegate.html)

---

## Changelog

| Date       | Changes          |
|------------|------------------|
| 2026-01-30 | Initial proposal |
