package com.dwlhm.tabmanager.api

import com.dwlhm.browser.BrowserSession
import com.dwlhm.browser.BrowserViewHost

class TabCoordinator(
    private val tabRegistry: TabManagerRegistry,
) {
    private val defaultTabManager = tabRegistry.manager(TabMode.DEFAULT)
    private val backgroundTabManager = tabRegistry.manager(TabMode.BACKGROUND)

    /**
     * Activates a tab with the given mode.
     * Returns a TabHandle containing the session and viewHost for UI binding.
     */
    fun activateTab(tabMode: TabMode = TabMode.DEFAULT): TabHandle {
        val manager = tabRegistry.manager(tabMode)
        val session = manager.acquire()
        val viewHost = manager.provideViewHost()
        return TabHandle(session, viewHost, tabMode)
    }

    /**
     * Deactivates a tab, releasing its resources.
     */
    fun deactivateTab(handle: TabHandle) {
        val manager = tabRegistry.manager(handle.mode)
        manager.release(handle.session)
    }

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