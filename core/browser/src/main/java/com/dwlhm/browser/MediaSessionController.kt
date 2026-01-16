package com.dwlhm.browser

import android.support.v4.media.session.MediaSessionCompat

/**
 * Kontrak untuk mengelola Android MediaSession.
 * 
 * Interface ini mendefinisikan kontrak untuk komponen yang mengelola MediaSession,
 * memungkinkan implementasi yang berbeda tanpa tight coupling.
 * 
 * Prinsip:
 * - Interface di core untuk dependency inversion
 * - Implementasi di feature/browser untuk konkret implementation
 * - Lifecycle dikelola secara eksplisit via initialize() dan release()
 */
interface MediaSessionController {
    /**
     * Token untuk integrasi dengan notification.
     */
    val sessionToken: MediaSessionCompat.Token?
    
    /**
     * Callback interface untuk aksi dari MediaSession.
     * Memisahkan concerns - controller tidak tahu tentang implementasi spesifik.
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
    fun initialize(callback: MediaSessionCallback)
    
    /**
     * Update metadata pada MediaSession.
     * 
     * @param title Judul media
     * @param artist Nama artis
     * @param album Nama album
     * @param artwork Cover art bitmap (optional)
     */
    fun updateMetadata(
        title: String,
        artist: String,
        album: String?,
        artwork: android.graphics.Bitmap?
    )
    
    /**
     * Update playback state pada MediaSession.
     * 
     * @param state State playback dari browser
     */
    fun updatePlaybackState(state: BrowserMediaState)
    
    /**
     * Release semua resources.
     * Harus dipanggil saat lifecycle berakhir.
     */
    fun release()
}
