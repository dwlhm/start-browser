package com.dwlhm.startbrowser.services

import android.content.Context
import com.dwlhm.browser.BrowserMediaSession
import com.dwlhm.browser.BrowserMediaState
import com.dwlhm.event.EventDispatcher
import com.dwlhm.event.MediaActivatedEvent
import com.dwlhm.event.MediaDeactivatedEvent
import com.dwlhm.event.MediaMetadataChangedEvent
import com.dwlhm.event.MediaStateChangedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * Manager untuk menghandle background media playback.
 * Single source of truth - listen events dan kontrol service.
 */
class MediaPlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var isServiceRunning = false
    private val activeMediaTabs = mutableSetOf<String>()
    private val jobs = mutableListOf<Job>()
    
    private var currentMediaSession: BrowserMediaSession? = null

    fun initialize() {
        observeMediaEvents()
    }

    private fun observeMediaEvents() {
        // Listen for media activation
        jobs += scope.launch {
            EventDispatcher.events.filterIsInstance<MediaActivatedEvent>().collect { event ->
                activeMediaTabs.add(event.tabId)
                currentMediaSession = event.mediaSession
                
                startServiceIfNeeded()
                MediaPlaybackService.setMediaSession(event.mediaSession)
            }
        }

        // Listen for media metadata changes
        jobs += scope.launch {
            EventDispatcher.events.filterIsInstance<MediaMetadataChangedEvent>().collect { event ->
                if (isServiceRunning) {
                    MediaPlaybackService.updateMetadata(context, event.mediaMetadata)
                }
            }
        }

        // Listen for media state changes
        jobs += scope.launch {
            EventDispatcher.events.filterIsInstance<MediaStateChangedEvent>().collect { event ->

                when (event.state) {
                    BrowserMediaState.PLAY -> {
                        startServiceIfNeeded()
                        MediaPlaybackService.updateState(context, event.state)
                    }
                    BrowserMediaState.PAUSE -> {
                        if (isServiceRunning) {
                            MediaPlaybackService.updateState(context, event.state)
                        }
                    }
                    BrowserMediaState.STOP -> {
                        activeMediaTabs.remove(event.tabId)
                        stopServiceIfNoMedia()
                    }
                }
            }
        }

        // Listen for media deactivation
        jobs += scope.launch {
            EventDispatcher.events.filterIsInstance<MediaDeactivatedEvent>().collect { event ->
                activeMediaTabs.remove(event.tabId)
                currentMediaSession = null
                MediaPlaybackService.setMediaSession(null)
                stopServiceIfNoMedia()
            }
        }
    }

    private fun startServiceIfNeeded() {
        if (!isServiceRunning) {
            MediaPlaybackService.startService(context)
            isServiceRunning = true
        }
    }

    private fun stopServiceIfNoMedia() {
        if (activeMediaTabs.isEmpty() && isServiceRunning) {
            MediaPlaybackService.stopService(context)
            isServiceRunning = false
        }
    }

    fun stopService() {
        if (isServiceRunning) {
            MediaPlaybackService.stopService(context)
            isServiceRunning = false
            activeMediaTabs.clear()
        }
    }

    /**
     * Cleanup resources.
     */
    fun destroy() {
        jobs.forEach { it.cancel() }
        jobs.clear()
        stopService()
        currentMediaSession = null
        MediaPlaybackService.setMediaSession(null)
    }
}
