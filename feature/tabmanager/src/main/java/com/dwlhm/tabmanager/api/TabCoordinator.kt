package com.dwlhm.tabmanager.api

import com.dwlhm.browser.BrowserSession
import java.util.UUID

class TabCoordinator(
    tabRegistry: TabManagerRegistry,
) {
    private val defaultTabManager = tabRegistry.manager(TabMode.DEFAULT)
    private val backgroundTabManager = tabRegistry.manager(TabMode.BACKGROUND)

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