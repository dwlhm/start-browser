package com.dwlhm.media.api

import android.graphics.Bitmap
import com.dwlhm.browser.BrowserMediaSession
import com.dwlhm.browser.BrowserMediaState

/**
 * Immutable state holder untuk media playback.
 * 
 * Prinsip:
 * - Semua field immutable (val)
 * - State changes menghasilkan instance baru
 * - Tidak ada side effects
 * 
 * @property tabId ID tab yang sedang memutar media
 * @property mediaSession Session controller dari browser engine
 * @property playbackState Status playback (PLAY/PAUSE/STOP)
 * @property title Judul media
 * @property artist Nama artis
 * @property album Nama album
 * @property artwork Cover art bitmap
 */
data class MediaPlaybackState(
    val tabId: String,
    val mediaSession: BrowserMediaSession,
    val playbackState: BrowserMediaState = BrowserMediaState.PAUSE,
    val title: String = "Media sedang diputar",
    val artist: String = "Start Browser",
    val album: String? = null,
    val artwork: Bitmap? = null
) {
    /**
     * Helper untuk cek apakah media sedang playing.
     */
    val isPlaying: Boolean
        get() = playbackState == BrowserMediaState.PLAY
    
    /**
     * Helper untuk cek apakah media sudah stopped.
     */
    val isStopped: Boolean
        get() = playbackState == BrowserMediaState.STOP
    
    /**
     * Buat state baru dengan playback state berbeda.
     */
    fun withPlaybackState(newState: BrowserMediaState): MediaPlaybackState {
        return copy(playbackState = newState)
    }
    
    /**
     * Buat state baru dengan metadata berbeda.
     */
    fun withMetadata(
        title: String?,
        artist: String?,
        album: String?,
        artwork: Bitmap?
    ): MediaPlaybackState {
        return copy(
            title = title ?: this.title,
            artist = artist ?: this.artist,
            album = album ?: this.album,
            artwork = artwork ?: this.artwork
        )
    }
}
