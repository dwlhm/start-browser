package com.dwlhm.media.api

import android.util.Log
import com.dwlhm.event.EventCollector
import com.dwlhm.event.MediaActivatedEvent
import com.dwlhm.event.MediaMetadataChangedEvent
import com.dwlhm.event.MediaStateChangedEvent
import kotlinx.coroutines.CoroutineScope

class MediaEventListener(
    scope: CoroutineScope
) {
    private val eventCollector = EventCollector(scope)

    fun observeEvent() {
        eventCollector.on<MediaActivatedEvent> { event ->
            Log.d("MediaEventListener", "MediaActivatedEvent: $event")
        }

        eventCollector.on<MediaMetadataChangedEvent> { event ->
            Log.d("MediaEventListener", "MediaActivatedEvent: $event")
        }

        eventCollector.on<MediaStateChangedEvent> { event ->
            Log.d("MediaEventListener", "MediaActivatedEvent: $event")
        }
    }
}