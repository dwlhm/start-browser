package com.dwlhm.browser

interface TabManager: BrowserViewHost {
    fun active(): BrowserSession?

    fun acquire(): BrowserSession
    fun acquire(browserSession: BrowserSession)
    fun release(browserSession: BrowserSession)
}