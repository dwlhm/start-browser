package com.dwlhm.browser.api

import com.dwlhm.browser.BrowserRuntime
import com.dwlhm.browser.BrowserSession
import com.dwlhm.browser.BrowserSessionFactory

class DefaultBrowserSessionFactory(
    private val browserRuntime: BrowserRuntime
): BrowserSessionFactory {
    override fun create(
        initialUrl: String,
        isIncognito: Boolean
    ): BrowserSession {
        val session = browserRuntime.createSession()
        session.loadUrl(initialUrl)
        return session
    }
}