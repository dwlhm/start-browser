# Integration Test: Media Notification

## Overview

| Info | Value |
|------|-------|
| Type | Integration Test (androidTest) |
| Priority | 🟠 High |
| Phase | 4 (Integration) |
| Est. Time | 3-4 jam |
| Total Tests | 5 |

---

## Purpose

Integration tests memverifikasi bahwa semua komponen bekerja bersama dengan benar. Ini berbeda dari unit tests yang mengisolasi satu komponen.

```
┌─────────────────────────────────────────────────────────────────┐
│ Unit Test: Satu komponen                                        │
│                                                                  │
│   [MediaPlaybackManager] ← test ini saja                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ Integration Test: Multiple komponen bersama                     │
│                                                                  │
│   [GeckoSession] → [TabSessionManager] → [EventDispatcher]     │
│                           ↓                                      │
│               [MediaPlaybackManager] → [MediaPlaybackService]   │
│                           ↓                                      │
│                   [Notification]                                 │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Test Suite

### MediaNotificationIntegrationTest

**Location:** `app/src/androidTest/java/com/dwlhm/startbrowser/MediaNotificationIntegrationTest.kt`

**Difficulty:** ⭐⭐⭐ Hard (Real Android environment)
**Est. Time:** 3-4 jam

```kotlin
// File: app/src/androidTest/java/com/dwlhm/startbrowser/MediaNotificationIntegrationTest.kt

@RunWith(AndroidJUnit4::class)
@LargeTest
class MediaNotificationIntegrationTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    private lateinit var notificationManager: NotificationManager
    
    @Before
    fun setup() {
        notificationManager = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
```

---

## Test Cases

### 1. Play Media → Notification Appears

- [ ] `when media plays then notification should appear`

```kotlin
@Test
fun `when media plays then notification should appear`() {
    // Arrange - Navigate to page with media
    activityRule.scenario.onActivity { activity ->
        // Navigate to test page or mock media activation
    }
    
    // Act - Simulate media play
    // This might need to be done via the browser or mock events
    
    // Assert - Check notification exists
    Thread.sleep(1000) // Wait for notification
    
    val notifications = notificationManager.activeNotifications
    val mediaNotification = notifications.find { 
        it.notification.channelId == MediaNotificationBuilder.CHANNEL_ID 
    }
    
    assertNotNull("Media notification should exist", mediaNotification)
}
```

### 2. Pause from Notification

- [ ] `when pause clicked on notification then media should pause`

```kotlin
@Test
fun `when pause clicked on notification then media should pause`() {
    // This requires:
    // 1. Media to be playing
    // 2. Finding and clicking the notification action
    // 3. Verifying media state changed
    
    // Note: Clicking notification actions programmatically is complex
    // May need to use UIAutomator or test the intent directly
}
```

### 3. Play from Notification

- [ ] `when play clicked on notification then media should play`

### 4. Click Notification → Navigate to Tab

- [ ] `when notification clicked then should navigate to correct tab`

```kotlin
@Test
fun `when notification clicked then should navigate to correct tab`() {
    // Arrange - Start media and get notification
    // ...
    
    // Act - Click notification content intent
    val notification = getMediaNotification()
    val contentIntent = notification.notification.contentIntent
    
    // Send the pending intent
    contentIntent.send()
    
    // Assert - Should be on correct tab
    Thread.sleep(500)
    
    activityRule.scenario.onActivity { activity ->
        // Verify current tab is the media tab
        val currentTab = activity.tabSessionManager.selectedTab.value
        assertEquals(expectedTabId, currentTab?.id)
    }
}
```

### 5. Stop Media → Notification Dismissed

- [ ] `when media stops then notification should be dismissed after debounce`

```kotlin
@Test
fun `when media stops then notification should be dismissed after debounce`() {
    // Arrange - Media playing, notification visible
    // ...
    
    // Act - Stop media
    // ...
    
    // Assert - Wait for debounce (300ms + buffer)
    Thread.sleep(500)
    
    val notifications = notificationManager.activeNotifications
    val mediaNotification = notifications.find { 
        it.notification.channelId == MediaNotificationBuilder.CHANNEL_ID 
    }
    
    assertNull("Notification should be dismissed", mediaNotification)
}
```

---

## Setup Requirements

### Dependencies

```kotlin
// app/build.gradle.kts
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test:rules:1.5.0")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0") // For notification
```

### Test Page

Buat halaman HTML sederhana untuk testing:

```html
<!-- assets/test/media-test.html -->
<!DOCTYPE html>
<html>
<body>
    <video id="testVideo" controls>
        <source src="test-video.mp4" type="video/mp4">
    </video>
    <script>
        // Auto-play for testing (might need user interaction first)
        document.getElementById('testVideo').play();
    </script>
</body>
</html>
```

### Emulator/Device Requirements

- Android 8.0+ (for notification channels)
- Media permissions granted
- Notification permission granted (Android 13+)

---

## Notes

### Integration Test Challenges

1. **Timing:** Media events dan notification updates tidak instant
   - Solution: Use `Thread.sleep()` atau polling

2. **State isolation:** Tests bisa affect each other
   - Solution: Clear state di `@Before` dan `@After`

3. **Notification interaction:** Sulit click notification programmatically
   - Solution: Test intent/PendingIntent langsung

4. **Media playback:** Real media butuh network atau local files
   - Solution: Use local test assets

### When to Write Integration Tests

✅ Do:
- Test critical user flows
- Test component interactions
- Regression tests untuk bugs

❌ Don't:
- Test setiap edge case (use unit tests)
- Test UI details (use Espresso/Compose tests)

### Alternative: Manual Testing Checklist

Jika integration tests terlalu kompleks, gunakan manual checklist:

```markdown
## Manual Test: Media Notification

### Preconditions
- [ ] App installed
- [ ] Notification permission granted

### Test Steps
1. [ ] Open app
2. [ ] Navigate to YouTube
3. [ ] Play a video
4. [ ] Verify notification appears
5. [ ] Tap pause on notification
6. [ ] Verify video pauses
7. [ ] Tap play on notification
8. [ ] Verify video plays
9. [ ] Tap notification body
10. [ ] Verify navigates to tab
11. [ ] Stop video
12. [ ] Verify notification dismissed

### Expected Results
All steps pass without errors.
```

---

## Progress

| Test Case | Status |
|-----------|--------|
| Play → Notification appears | ⬜ |
| Pause from notification | ⬜ |
| Play from notification | ⬜ |
| Click → Navigate to tab | ⬜ |
| Stop → Notification dismissed | ⬜ |
| **Total** | **0/5** |
