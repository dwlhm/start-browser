package com.dwlhm.browser

/**
 * Callbacks for browser session events.
 * Set via BrowserSession.setCallback() after session creation.
 */
interface BrowserSessionCallback {
    fun onTabInfoChanged(title: String, url: String)
    fun onMediaActivated(mediaSession: BrowserMediaSession)
    fun onMediaMetadataChanged(mediaMetadata: BrowserMediaMetadata)
    fun onMediaStateChanged(state: BrowserMediaState)
    fun onMediaDeactivated()
}
