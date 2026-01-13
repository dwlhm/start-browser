package com.dwlhm.startbrowser.services.media

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.dwlhm.browser.BrowserMediaState

/**
 * Controller untuk mengelola Android MediaSession.
 * 
 * Prinsip:
 * - Single Responsibility: Hanya mengelola MediaSessionCompat
 * - Menerima callback interface untuk menghindari direct dependency
 * - Lifecycle dikelola secara eksplisit via initialize() dan release()
 * 
 * @param context Application context
 */
class MediaSessionController(
    private val context: Context
) {
    private var mediaSession: MediaSessionCompat? = null
    
    /**
     * Token untuk integrasi dengan notification.
     */
    val sessionToken: MediaSessionCompat.Token?
        get() = mediaSession?.sessionToken
    
    /**
     * Callback interface untuk aksi dari MediaSession.
     * Memisahkan concerns - controller tidak tahu tentang service.
     */
    interface MediaSessionCallback {
        fun onPlay()
        fun onPause()
        fun onStop()
        fun onSkipToNext()
        fun onSkipToPrevious()
    }
    
    /**
     * Inisialisasi MediaSession dengan callback.
     * 
     * @param callback Handler untuk aksi dari MediaSession
     */
    fun initialize(callback: MediaSessionCallback) {
        if (mediaSession != null) {
            release()
        }
        
        mediaSession = MediaSessionCompat(context, "StartBrowserMediaSession").apply {
            setCallback(createMediaSessionCallback(callback))
            isActive = true
        }
    }
    
    /**
     * Update metadata pada MediaSession.
     * 
     * @param state MediaPlaybackState yang berisi metadata
     */
    fun updateMetadata(state: MediaPlaybackState) {
        val session = mediaSession ?: return
        
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, state.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, state.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, state.album ?: "")
            .apply {
                state.artwork?.let { bitmap ->
                    putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                }
            }
            .build()
        
        session.setMetadata(metadata)
    }
    
    /**
     * Update playback state pada MediaSession.
     * 
     * @param browserState State playback dari browser
     */
    fun updatePlaybackState(browserState: BrowserMediaState) {
        val session = mediaSession ?: return
        
        val state = mapToPlaybackState(browserState)
        
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
            .setActions(SUPPORTED_ACTIONS)
            .build()
        
        session.setPlaybackState(playbackState)
    }
    
    /**
     * Release semua resources.
     * Harus dipanggil saat service di-destroy.
     */
    fun release() {
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
        callback: MediaSessionCallback
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
