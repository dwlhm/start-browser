package com.dwlhm.gecko

import com.dwlhm.webview.WebViewEngine
import com.dwlhm.webview.WebViewSession
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeckoViewEngine @Inject constructor(
    private val runtime: GeckoRuntime
): WebViewEngine {
    override fun createSession(): WebViewSession {
        val session = GeckoSession()
        session.open(runtime)
        return GeckoViewSession(session)
    }
}