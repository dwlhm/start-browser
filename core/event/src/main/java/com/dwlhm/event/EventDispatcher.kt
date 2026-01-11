package com.dwlhm.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * Global event dispatcher for broadcasting events across the app.
 * Uses SharedFlow to allow multiple subscribers.
 *
 * Usage:
 * ```
 * // Dispatch an event
 * EventDispatcher.dispatch(TabCreatedEvent(tabId = "123"))
 *
 * // Collect all events
 * EventDispatcher.events.collect { event ->
 *     when (event) {
 *         is TabCreatedEvent -> handleTabCreated(event)
 *         is NavigationEvent -> handleNavigation(event)
 *     }
 * }
 *
 * // Collect specific event type
 * EventDispatcher.on<TabCreatedEvent>().collect { event ->
 *     handleTabCreated(event)
 * }
 * ```
 */
object EventDispatcher {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _events = MutableSharedFlow<Event>(
        replay = 0,
        extraBufferCapacity = 64
    )
    
    /**
     * Flow of all dispatched events.
     */
    val events: SharedFlow<Event> = _events.asSharedFlow()
    
    /**
     * Dispatch an event to all subscribers.
     */
    fun dispatch(event: Event) {
        scope.launch {
            _events.emit(event)
        }
    }
    
    /**
     * Dispatch an event suspending until delivered.
     */
    suspend fun dispatchSuspend(event: Event) {
        _events.emit(event)
    }
    
    /**
     * Get a flow of events filtered by type.
     */
    inline fun <reified T : Event> on(function: () -> Unit) = events.filterIsInstance<T>()
}
