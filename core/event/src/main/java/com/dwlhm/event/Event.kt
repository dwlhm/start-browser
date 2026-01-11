package com.dwlhm.event

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

data class TabInfoChangedEvent(
    val tabId: String,
    val url: String,
    val title: String,
): Event
