package com.dwlhm.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * Helper class for collecting events with lifecycle awareness.
 *
 * Usage in ViewModel:
 * ```
 * class MyViewModel : ViewModel() {
 *     private val eventCollector = EventCollector(viewModelScope)
 *
 *     init {
 *         eventCollector.on<TabCreatedEvent> { event ->
 *             // Handle event
 *         }
 *     }
 * }
 * ```
 *
 * Usage in Composable:
 * ```
 * @Composable
 * fun MyScreen() {
 *     val scope = rememberCoroutineScope()
 *
 *     LaunchedEffect(Unit) {
 *         EventDispatcher.on<NavigationEvent>().collect { event ->
 *             // Handle event
 *         }
 *     }
 * }
 * ```
 */
class EventCollector(
    @PublishedApi internal val scope: CoroutineScope
) {
    @PublishedApi
    internal val jobs = mutableListOf<Job>()

    /**
     * Subscribe to a specific event type.
     */
    inline fun <reified T : Event> on(crossinline handler: suspend (T) -> Unit): Job {
        val job = scope.launch {
            EventDispatcher.events.filterIsInstance<T>().collect { event ->
                handler(event)
            }
        }
        jobs.add(job)
        return job
    }

    /**
     * Subscribe to all events.
     */
    fun onAny(handler: suspend (Event) -> Unit): Job {
        val job = scope.launch {
            EventDispatcher.events.collect { event ->
                handler(event)
            }
        }
        jobs.add(job)
        return job
    }

    /**
     * Cancel all event subscriptions.
     */
    fun cancelAll() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }
}
