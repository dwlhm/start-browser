# UI Components

## SystemBars

Edge-to-edge status bar management with automatic icon color detection.

### Usage

#### Basic - Use theme background
```kotlin
@Composable
fun MyScreen() {
    SystemBarScaffold {
        // Content automatically padded
    }
}
```

#### Custom background color
```kotlin
@Composable
fun MyScreen() {
    SystemBarScaffold(backgroundColor = Color.Blue) {
        // Content
    }
}
```

#### Override status bar only
```kotlin
@Composable
fun MyScreen() {
    SetStatusBar(Color.Red)
    
    Column {
        // Your layout
    }
}
```

#### Force icon color
```kotlin
SetStatusBar(Color.Red, darkIcons = false) // White icons
SetStatusBar(Color.White, darkIcons = true) // Dark icons
```

### API Reference

**`SetStatusBar(color: Color, darkIcons: Boolean? = null)`**
- Configures status bar appearance
- `darkIcons = null` auto-detects from luminance

**`SystemBarScaffold(backgroundColor: Color, darkStatusBarIcons: Boolean? = null, content: @Composable BoxScope.() -> Unit)`**
- Full-screen layout with integrated status bar
- Content receives automatic inset padding

### Implementation Notes

- API < 35: Uses `window.statusBarColor`
- API 35+: Edge-to-edge with compose background
- Auto icon detection threshold: luminance > 0.5

