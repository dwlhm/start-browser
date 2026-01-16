package com.dwlhm.media

import com.dwlhm.event.EventCollector
import com.dwlhm.event.MediaActivatedEvent
import com.dwlhm.event.MediaDeactivatedEvent
import com.dwlhm.event.MediaMetadataChangedEvent
import com.dwlhm.event.MediaStateChangedEvent
import kotlinx.coroutines.CoroutineScope

/**
 * Listener yang observe media events dan update MediaStateRegistry.
 * 
 * Tanggung Jawab:
 * - Listen ke media events
 * - Update MediaStateRegistry berdasarkan events
 * - Enforce single playback policy (pause tab lain saat ada yang mulai play)
 * 
 * Prinsip:
 * - Decoupled - TabSessionManager hanya dispatch events, tidak tahu tentang registry
 * - Event-driven - semua update via events
 */
class MediaStateRegistryListener(
    private val registry: MediaStateRegistry,
    scope: CoroutineScope
) {
    private val eventCollector = EventCollector(scope)
    
    fun observeEvents() {
        eventCollector.on<MediaActivatedEvent> { event ->
            registry.register(event.tabId, event.mediaSession)
        }
        
        eventCollector.on<MediaDeactivatedEvent> { event ->
            registry.unregister(event.tabId)
        }
        
        eventCollector.on<MediaStateChangedEvent> { event ->
            registry.updateState(event.tabId, event.state)
            
            // Enforce single playback: pause tab lain jika tab ini mulai play
            if (event.state == com.dwlhm.browser.BrowserMediaState.PLAY) {
                val tabToPause = registry.getTabToPauseForNewPlayback(event.tabId)
                tabToPause?.let {
                    try {
                        it.mediaSession.pause()
                    } catch (e: Exception) {
                        android.util.Log.e("MediaStateRegistryListener", "Error pausing tab ${it.tabId}", e)
                    }
                }
            }
        }
        
        eventCollector.on<MediaMetadataChangedEvent> { event ->
            registry.updateMetadata(event.tabId, event.mediaMetadata)
        }
    }
}
