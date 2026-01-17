package com.dwlhm.browser

interface BrowserRuntime {
    fun createSession(
        sessionId: String,
        isIncognito: Boolean
    ): BrowserSession
    fun shutdown()
}