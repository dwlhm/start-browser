package com.dwlhm.browser

/**
 * Callbacks for browser session events.
 * Set via BrowserSession.setCallback() after session creation.
 */
interface BrowserSessionCallback {
    fun onTabInfoChanged(title: String, url: String)
}
