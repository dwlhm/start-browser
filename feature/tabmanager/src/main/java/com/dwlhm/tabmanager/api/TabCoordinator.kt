package com.dwlhm.tabmanager.api

import com.dwlhm.browser.BrowserSession

class TabCoordinator(
    private val defaultTabManager: DefaultTabManager,
    private val backgroundTabManager: BackgroundTabManager
) {
    fun moveToBackground() {
        val browserSession = defaultTabManager.active() ?: return
        backgroundTabManager.acquire(browserSession)
        defaultTabManager.release(browserSession)
    }

    fun moveToForeground(): BrowserSession {
        val browserSession = backgroundTabManager.active()
            ?: defaultTabManager.acquire()

        defaultTabManager.acquire(browserSession)
        backgroundTabManager.release(browserSession)

        return browserSession
    }
}