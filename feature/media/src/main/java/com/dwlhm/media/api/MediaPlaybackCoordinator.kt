package com.dwlhm.media.api

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import com.dwlhm.browser.BrowserMediaMetadata
import com.dwlhm.browser.BrowserMediaSession
import com.dwlhm.browser.BrowserMediaState
import com.dwlhm.data.store.media.MediaAssetStore
import com.dwlhm.event.EventCollector
import com.dwlhm.event.MediaActivatedEvent
import com.dwlhm.event.MediaDeactivatedEvent
import com.dwlhm.event.MediaMetadataChangedEvent
import com.dwlhm.event.MediaStateChangedEvent
import com.dwlhm.media.MediaIntent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MediaPlaybackCoordinator(
    private val context: Context,
    private val mediaServiceClass: Class<*>,
    private val mediaArtworkStore: MediaAssetStore<Bitmap>,
    private val mediaSessionStore: MediaAssetStore<BrowserMediaSession>,
) {
    private var activeMediaTabId: String? = null
    private var activeMediaSession: BrowserMediaSession? = null
    private var isMediaServiceRunning: Boolean = false

    private var deactivationJob: Job? = null
    private val DEACTIVATION_DELAY_MS = 300L
    private val coordinatorScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun listenToMediaEvent(eventCollector: EventCollector) {
        eventCollector.on<MediaActivatedEvent> { event ->
            deactivationJob?.cancel(CancellationException("new media activated"))

            if (activeMediaTabId !== null && activeMediaTabId != event.tabId) {
                stopMediaService()
            }

            activeMediaTabId = event.tabId
            activeMediaSession = event.mediaSession

            startMediaService(event.mediaSession)
        }

        eventCollector.on<MediaDeactivatedEvent> { event ->
            if (activeMediaTabId != event.tabId) return@on

            deactivationJob?.cancel(CancellationException("media deactivated"))
            deactivationJob = coordinatorScope.launch {
                delay(DEACTIVATION_DELAY_MS)

                stopMediaService()

                activeMediaTabId = null
                activeMediaSession = null
            }
        }

        eventCollector.on<MediaMetadataChangedEvent> { event ->
            updateMetadataMediaService(event.mediaMetadata)
        }

        eventCollector.on<MediaStateChangedEvent> { event ->
            updateStateMediaService(event.state)
        }
    }

    fun startMediaService(mediaSession: BrowserMediaSession) {
        if (isMediaServiceRunning) return

        mediaSessionStore.put(MediaIntent.Extra.SESSION, mediaSession)

        val intent = Intent(context, mediaServiceClass).apply {
            action = MediaIntent.Action.INITIALIZE
            putExtra(MediaIntent.Extra.TAB_ID, activeMediaTabId)
            putExtra(MediaIntent.Extra.STATE, BrowserMediaState.PLAY)
        }

        context.startForegroundService(intent)
        isMediaServiceRunning = true
    }

    fun stopMediaService() {
        if (!isMediaServiceRunning) return

        val intent = Intent(context, mediaServiceClass)
        context.stopService(intent)
        isMediaServiceRunning = false
    }

    fun updateMetadataMediaService(metadata: BrowserMediaMetadata) {
        if (!isMediaServiceRunning) return

        metadata.artwork?.let { mediaArtworkStore.put(MediaIntent.Extra.ARTWORK, it) }

        val intent = Intent(context, mediaServiceClass).apply {
            action = MediaIntent.Action.UPDATE_METADATA
            putExtra(MediaIntent.Extra.TITLE, metadata.title)
            putExtra(MediaIntent.Extra.ARTIST, metadata.artist)
            putExtra(MediaIntent.Extra.ALBUM, metadata.album)
        }
        context.startService(intent)
    }

    fun updateStateMediaService(state: BrowserMediaState) {
        if (!isMediaServiceRunning) return

        val intent = Intent(context, mediaServiceClass).apply {
            action = MediaIntent.Action.UPDATE_STATE
            putExtra(MediaIntent.Extra.STATE, state.name)
        }
        context.startService(intent)
    }

    fun destroy() {
        deactivationJob?.cancel()
        stopMediaService()
        activeMediaTabId = null
        activeMediaSession = null
        coordinatorScope.cancel(CancellationException("media playback coordinator destroyed"))
    }
}