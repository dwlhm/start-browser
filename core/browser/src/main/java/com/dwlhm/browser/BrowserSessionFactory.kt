package com.dwlhm.browser

interface BrowserSessionFactory {
    fun create(
        initialUrl: String,
        isIncognito: Boolean
    ): BrowserSession
}