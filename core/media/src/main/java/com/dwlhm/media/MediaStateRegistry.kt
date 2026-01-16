package com.dwlhm.media

import com.dwlhm.browser.BrowserMediaMetadata
import com.dwlhm.browser.BrowserMediaSession
import com.dwlhm.browser.BrowserMediaState

/**
 * Registry untuk track media state per tab.
 * 
 * Tanggung Jawab:
 * - Track state media per tab (general purpose registry)
 * - Provide query methods untuk state lookup
 * - Handle playback policy enforcement (single playback)
 * 
 * Prinsip:
 * - General purpose - bisa di-extend untuk future features
 * - Stateless operations - hanya track, tidak handle lifecycle
 * - Policy enforcement - enforce single playback policy
 */
class MediaStateRegistry {
    
    /**
     * State media per tab.
     */
    data class TabMediaState(
        val tabId: String,
        val mediaSession: BrowserMediaSession,
        var state: BrowserMediaState,
        var metadata: BrowserMediaMetadata?
    )
    
    private val stateByTab = mutableMapOf<String, TabMediaState>()
    private var currentlyPlayingTabId: String? = null
    
    /**
     * Register atau update media state untuk tab.
     */
    fun register(tabId: String, mediaSession: BrowserMediaSession, initialState: BrowserMediaState = BrowserMediaState.PAUSE) {
        stateByTab[tabId] = TabMediaState(
            tabId = tabId,
            mediaSession = mediaSession,
            state = initialState,
            metadata = null
        )
    }
    
    /**
     * Update state untuk tab yang sudah terdaftar.
     * 
     * @return true jika update berhasil, false jika tab tidak terdaftar
     */
    fun updateState(tabId: String, state: BrowserMediaState): Boolean {
        val tabState = stateByTab[tabId] ?: return false
        tabState.state = state
        
        // Handle single playback policy
        when (state) {
            BrowserMediaState.PLAY -> {
                // Tab ini mulai playing - clear reference tab lain
                currentlyPlayingTabId = tabId
            }
            BrowserMediaState.PAUSE, BrowserMediaState.STOP -> {
                // Tab ini pause/stop - clear reference jika ini yang sedang playing
                if (currentlyPlayingTabId == tabId) {
                    currentlyPlayingTabId = null
                }
            }
        }
        
        return true
    }
    
    /**
     * Update metadata untuk tab.
     */
    fun updateMetadata(tabId: String, metadata: BrowserMediaMetadata): Boolean {
        val tabState = stateByTab[tabId] ?: return false
        tabState.metadata = metadata
        return true
    }
    
    /**
     * Unregister tab (media deactivated).
     */
    fun unregister(tabId: String) {
        stateByTab.remove(tabId)
        if (currentlyPlayingTabId == tabId) {
            currentlyPlayingTabId = null
        }
    }
    
    /**
     * Get state untuk tab tertentu.
     */
    fun getState(tabId: String): TabMediaState? {
        return stateByTab[tabId]
    }
    
    /**
     * Cek apakah tab sedang playing.
     */
    fun isPlaying(tabId: String): Boolean {
        return currentlyPlayingTabId == tabId
    }
    
    /**
     * Cek apakah tab punya media aktif.
     */
    fun hasMedia(tabId: String): Boolean {
        return stateByTab.containsKey(tabId)
    }
    
    /**
     * Get tab yang sedang playing (jika ada).
     */
    fun getCurrentlyPlaying(): TabMediaState? {
        return currentlyPlayingTabId?.let { stateByTab[it] }
    }
    
    /**
     * Get semua tab dengan media aktif.
     */
    fun getAllTabsWithMedia(): List<TabMediaState> {
        return stateByTab.values.toList()
    }
    
    /**
     * Get tab yang perlu di-pause saat tab baru mulai play.
     * Untuk enforce single playback policy.
     */
    fun getTabToPauseForNewPlayback(newTabId: String): TabMediaState? {
        return if (currentlyPlayingTabId != null && currentlyPlayingTabId != newTabId) {
            stateByTab[currentlyPlayingTabId]
        } else {
            null
        }
    }
    
    /**
     * Clear semua state (untuk testing atau cleanup).
     */
    fun clear() {
        stateByTab.clear()
        currentlyPlayingTabId = null
    }
}
