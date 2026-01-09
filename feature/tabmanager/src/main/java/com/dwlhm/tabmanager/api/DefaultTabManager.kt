package com.dwlhm.tabmanager.api

import com.dwlhm.browser.BrowserRuntime
import com.dwlhm.browser.BrowserSession
import com.dwlhm.browser.BrowserViewHost
import com.dwlhm.browser.TabManager

class DefaultTabManager(
    private val browserRuntime: BrowserRuntime
): TabManager {
    private var _currentTab: BrowserSession? = null
    private var _attachedView: Any? = null

    override fun active(): BrowserSession? {
        return _currentTab
    }

    override fun acquire(): BrowserSession {
        if (_currentTab != null) return _currentTab!!

        val browserSession = browserRuntime.createSession()
        _currentTab = browserSession
        return browserSession
    }

    override fun acquire(browserSession: BrowserSession) {
        if (_currentTab === browserSession) return

        detach()
        _currentTab = browserSession

        _attachedView?.let {
            browserSession.attachToView(it)
        }
    }

    override fun release(browserSession: BrowserSession) {
        if (_currentTab != browserSession) return

        detach()
        _currentTab = null
    }

    override fun provideViewHost(): BrowserViewHost {
        return object : BrowserViewHost {
            override fun attach(view: Any) {
                this@DefaultTabManager.attach(view)
            }

            override fun detach() {
                this@DefaultTabManager.detach()
            }
        }
    }

    fun attach(view: Any) {
        if (_attachedView === view) return

        detach()
        _attachedView = view
        _currentTab?.attachToView(view)
    }

     fun detach() {
         if (_currentTab != null) {
             _currentTab?.detachFromView()
             _attachedView = null
         }
    }
}