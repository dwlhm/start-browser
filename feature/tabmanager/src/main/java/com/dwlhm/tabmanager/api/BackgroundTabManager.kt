package com.dwlhm.tabmanager.api

import com.dwlhm.browser.BrowserSession
import com.dwlhm.browser.BrowserViewHost
import com.dwlhm.browser.TabManager

class BackgroundTabManager: TabManager {
    private var currentTab: BrowserSession? = null

    override fun active(): BrowserSession? {
        return currentTab
    }

    override fun newSession(): BrowserSession {
        error("BackgroundTabManager tidak membuat session baru")
    }

    override fun acquire(): BrowserSession {
        error("BackgroundTabManager tidak membuat session baru")
    }
    override fun acquire(browserSession: BrowserSession) {
        release(browserSession)
        browserSession.detachFromView()
        currentTab = browserSession
    }
    override fun release(browserSession: BrowserSession) {
        if (currentTab != browserSession) return
        currentTab = null
    }

    override fun provideViewHost(): BrowserViewHost {
        return object : BrowserViewHost {
            override fun attach(view: Any) {
                // noop -- background tab manager tidak perlu attach view
            }
            override fun detach() {
                // noop -- dari awal tidak ada view yang diattach
            }
        }
    }
}