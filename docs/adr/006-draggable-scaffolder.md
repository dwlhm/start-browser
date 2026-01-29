# ADR-005: Draggable Scaffolder

| Field         | Detail         |
|:--------------|:---------------|
| **Status**    | Proposed       |
| **Date**      | 2026-01-30     |
| **Deciders**  | dwlhm          |
| **Category**  | UI/UX Pattern  |
| **Priority**  | High           |

## Context

### Background
Navigating inside an application can often feel stagnant; typically, users only click from one point to another. Recently, there has been a growing trend in mobile UX that combines **draggable behavior** to bridge the gap between content layers. Implementing this will provide a more fluid and modern feel to the user interface.

### Goal
Add a `DraggableScaffold` component with the following behaviors:
1. Partial visibility: Show or hide content based on the user's scroll/drag distance.
2. Snap points: Support for multiple predefined checkpoints (breakpoints).
3. Customizability: Flexible styling that isn't tied to a specific design library.
4. Interoperability: Ensure touch event propagation works correctly for components both inside and behind the scaffold.

---

## Decision

### New Architecture

```
┌───────────────────────────────────────────────────────────────────────────────────┐
│   DraggablerScaffold                                                              │
|   ────────────────────────────────────────────────────────────────────────────────|
│   Responsibility:                                                                 |
│   * Detect and intercept user scroll/drag gestures                                |
|   * Provide flexible API for component customization                              |
|   * Manage snap logic and animation states                                        |
|   * Ensure proper touch propagation to underlying layers                          |
└───────────────────────────────────────────────────────────────────────────────────┘
```

### Rationale & Design Choice
* **Custom Implementation over Material3**: While Material3 provides a `BottomSheetScaffold`, it is highly opinionated regarding its design system and state management. A custom implementation allows us to have full control over the animation curves, gesture sensitivity, and adherence to our internal design guidelines without the overhead of Material3's constraints.

---

## Consequences

### Positive (Pros)
* **Design Flexibility**: Complete freedom to customize the UI/UX without fighting against library-specific constraints.
* **UX Enhancement**: Provides a more interactive and modern navigation experience.
* **Reusability**: Can be used across different modules within the `core:ui` package.

### Negative (Cons/Risks)
* **Maintenance Overhead**: We are responsible for handling edge cases in gesture handling and screen density variations.
* **Complexity**: Implementing smooth touch propagation (ensuring clicks work on layers behind the scaffold) requires careful management of the gesture system.

---

## Plan Implementation

### Step 1: Create Draggable Scaffold Component
* **File**: `core/ui/src/main/java/com/dwlhm/ui/scaffold/DraggableScaffold.kt`
* **Change**: Add `DraggableScaffold` component using Jetpack Compose gesture APIs.
* **Impact**: Provides a new reusable UI foundation.

### Step 2: Update Tests
* **File**: Unit and UI test files.
* **Change**:
    - Add Unit Tests for state logic (breakpoint calculations).
    - Add UI Tests to verify gesture recognition and snap behavior.
* **Impact**: Ensures long-term stability and prevents regression in UI behavior.

---

## Reference
- [Material3 Bottom Sheet](https://m3.material.io/components/bottom-sheets/overview)
- [Gestures in Jetpack Compose](https://developer.android.com/develop/ui/compose/gestures)

---

## Changelog
| Date       | Changes          |
|------------|------------------|
| 2026-01-30 | Initial proposal |

