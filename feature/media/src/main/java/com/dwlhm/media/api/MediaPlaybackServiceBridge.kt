package com.dwlhm.media.api

import android.graphics.Bitmap
import com.dwlhm.browser.BrowserMediaSession

/**
 * Bridge untuk passing non-serializable data ke service.
 * 
 * Catatan: Ini masih menggunakan static reference, tapi:
 * - Data langsung di-consume setelah diakses
 * - Hanya digunakan saat initialize dan update metadata
 * - Alternative: Binder-based communication (lebih kompleks)
 * 
 * TODO: Pertimbangkan migrasi ke Binder jika diperlukan.
 */
object MediaPlaybackServiceBridge {
    private var pendingMediaSession: BrowserMediaSession? = null
    private var pendingArtwork: Bitmap? = null
    
    /**
     * Set MediaSession untuk diambil oleh service.
     * Dipanggil oleh Manager sebelum start service.
     */
    fun setMediaSession(session: BrowserMediaSession?) {
        pendingMediaSession = session
    }
    
    /**
     * Get dan consume MediaSession.
     * Session di-null-kan setelah diambil.
     */
    fun getMediaSession(): BrowserMediaSession? {
        val session = pendingMediaSession
        pendingMediaSession = null
        return session
    }
    
    /**
     * Set artwork untuk diambil oleh service.
     * Bitmap tidak bisa dikirim via Intent karena size limit.
     */
    fun setArtwork(artwork: Bitmap?) {
        pendingArtwork = artwork
    }
    
    /**
     * Get dan consume artwork.
     * Artwork di-null-kan setelah diambil.
     */
    fun consumeArtwork(): Bitmap? {
        val artwork = pendingArtwork
        pendingArtwork = null
        return artwork
    }
    
    /**
     * Clear semua pending data.
     */
    fun clear() {
        pendingMediaSession = null
        pendingArtwork = null
    }
}
