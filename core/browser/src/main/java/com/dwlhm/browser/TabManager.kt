package com.dwlhm.browser

interface TabManager {
    fun active(): BrowserSession?

    fun newSession(): BrowserSession
    fun acquire(): BrowserSession
    fun acquire(browserSession: BrowserSession)
    fun release(browserSession: BrowserSession)

    fun provideViewHost(): BrowserViewHost
}