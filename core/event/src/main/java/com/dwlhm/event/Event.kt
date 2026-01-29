package com.dwlhm.event

import com.dwlhm.browser.BrowserMediaMetadata
import com.dwlhm.browser.BrowserMediaSession
import com.dwlhm.browser.BrowserMediaState

/**
 * Base interface for all events in the application.
 * Implement this interface to create custom events.
 *
 * Example:
 * ```
 * data class TabCreatedEvent(val tabId: String) : Event
 * data class NavigationEvent(val url: String) : Event
 * object AppStartedEvent : Event
 * ```
 */
interface Event

data class TabCreatedEvent(
    val tabId: String,
    val initialUrl: String
): Event

data class TabClosedEvent(
    val tabId: String,
): Event

data class TabLocationChangedEvent(
    val tabId: String,
    val url: String,
    val title: String,
): Event

data class TabTitleChangedEvent(
    val tabId: String,
    val url: String,
    val title: String,
): Event

data class TabInfoChangedEvent(
    val tabId: String,
    val url: String,
    val title: String,
): Event

data class MediaActivatedEvent(
    val tabId: String,
    val mediaSession: BrowserMediaSession,
): Event

data class MediaMetadataChangedEvent(
    val tabId: String,
    val metadataSession: BrowserMediaSession,
    val mediaMetadata: BrowserMediaMetadata,
): Event

data class MediaStateChangedEvent(
    val tabId: String,
    val mediaSession: BrowserMediaSession,
    val state: BrowserMediaState,
): Event

data class MediaDeactivatedEvent(
    val tabId: String,
): Event

data class ToolbarVisibilityEvent(
    val sessionId: String,
    val visible: Boolean,
): Event