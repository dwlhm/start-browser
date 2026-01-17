package com.dwlhm.browser.api

import com.dwlhm.browser.BrowserRuntime
import com.dwlhm.browser.BrowserSession
import com.dwlhm.browser.BrowserSessionFactory
import java.util.UUID

class DefaultBrowserSessionFactory(
    private val browserRuntime: BrowserRuntime
): BrowserSessionFactory {
    override fun create(
        sessionId: String?,
        initialUrl: String,
        isIncognito: Boolean
    ): BrowserSession {
        val activeSessionId = sessionId ?: UUID.randomUUID().toString()

        val session = browserRuntime.createSession(activeSessionId, isIncognito)
        session.loadUrl(initialUrl)
        return session
    }
}