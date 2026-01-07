package com.dwlhm.browser

import android.content.Context

interface BrowserRuntime {
    fun createSession(): BrowserSession
    fun shutdown()
}