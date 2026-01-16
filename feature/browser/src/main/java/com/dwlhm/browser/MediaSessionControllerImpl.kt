package com.dwlhm.browser

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

/**
 * Implementasi MediaSessionController untuk mengelola Android MediaSession.
 * 
 * Prinsip:
 * - Single Responsibility: Hanya mengelola MediaSessionCompat
 * - Menerima callback interface untuk menghindari direct dependency
 * - Lifecycle dikelola secara eksplisit via initialize() dan release()
 * 
 * @param context Application context
 */
class MediaSessionControllerImpl(
    private val context: Context
) : MediaSessionController {
    private var mediaSession: MediaSessionCompat? = null
    
    override val sessionToken: MediaSessionCompat.Token?
        get() = mediaSession?.sessionToken
    
    override fun initialize(callback: MediaSessionController.MediaSessionCallback) {
        if (mediaSession != null) {
            release()
        }
        
        mediaSession = MediaSessionCompat(context, "StartBrowserMediaSession").apply {
            setCallback(createMediaSessionCallback(callback))
            isActive = true
        }
    }
    
    override fun updateMetadata(
        title: String,
        artist: String,
        album: String?,
        artwork: android.graphics.Bitmap?
    ) {
        val session = mediaSession ?: return
        
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album ?: "")
            .apply {
                artwork?.let { bitmap ->
                    putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                }
            }
            .build()
        
        session.setMetadata(metadata)
    }
    
    override fun updatePlaybackState(state: BrowserMediaState) {
        val session = mediaSession ?: return
        
        val playbackStateCompat = mapToPlaybackState(state)
        
        val playbackState = PlaybackStateCompat.Builder()
            .setState(playbackStateCompat, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
            .setActions(SUPPORTED_ACTIONS)
            .build()
        
        session.setPlaybackState(playbackState)
    }
    
    override fun release() {
        mediaSession?.run {
            isActive = false
            release()
        }
        mediaSession = null
    }
    
    /**
     * Konversi BrowserMediaState ke PlaybackStateCompat state.
     */
    private fun mapToPlaybackState(state: BrowserMediaState): Int {
        return when (state) {
            BrowserMediaState.PLAY -> PlaybackStateCompat.STATE_PLAYING
            BrowserMediaState.PAUSE -> PlaybackStateCompat.STATE_PAUSED
            BrowserMediaState.STOP -> PlaybackStateCompat.STATE_STOPPED
        }
    }
    
    /**
     * Membuat callback wrapper untuk MediaSessionCompat.
     */
    private fun createMediaSessionCallback(
        callback: MediaSessionController.MediaSessionCallback
    ): MediaSessionCompat.Callback {
        return object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                callback.onPlay()
            }
            
            override fun onPause() {
                callback.onPause()
            }
            
            override fun onStop() {
                callback.onStop()
            }
            
            override fun onSkipToNext() {
                callback.onSkipToNext()
            }
            
            override fun onSkipToPrevious() {
                callback.onSkipToPrevious()
            }
        }
    }
    
    companion object {
        /**
         * Actions yang didukung oleh MediaSession.
         */
        private const val SUPPORTED_ACTIONS = 
            PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_STOP or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    }
}
