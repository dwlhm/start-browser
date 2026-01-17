package com.dwlhm.browser

interface BrowserSessionFactory {
    fun create(
        sessionId: String?,
        initialUrl: String,
        isIncognito: Boolean
    ): BrowserSession
}