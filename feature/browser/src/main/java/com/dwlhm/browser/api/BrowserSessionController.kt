package com.dwlhm.browser.api

import com.dwlhm.browser.BrowserSession
import kotlinx.coroutines.flow.StateFlow

class BrowserSessionController(
    private val session: BrowserSession,
): BrowserSession {
    override val activeTitle: StateFlow<String?>
        get() = session.activeTitle

    override val activeUrl: StateFlow<String?>
        get() = session.activeUrl

    override val canGoBack: StateFlow<Boolean>
        get() = session.canGoBack

    override val canGoForward: StateFlow<Boolean>
        get() = session.canGoForward

    override fun attachToView(view: Any) {
        session.attachToView(view)
    }

    override fun detachFromView() {
        session.detachFromView()
    }

    override fun loadUrl(url: String) {
        session.loadUrl(url)
    }

    override fun reload() {
        session.reload()
    }

    override fun stop() {
        session.stop()
    }

    override fun goBack(): Boolean {
        return session.goBack()
    }

    override fun goForward(): Boolean {
        return session.goForward()
    }
}