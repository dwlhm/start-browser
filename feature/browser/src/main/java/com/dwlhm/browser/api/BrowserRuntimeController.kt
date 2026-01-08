package com.dwlhm.browser.api

import android.content.Context
import com.dwlhm.browser.BrowserRuntime
import com.dwlhm.browser.BrowserSession
import com.dwlhm.gecko.api.GeckoBrowserRuntime

class BrowserRuntimeController(
    context: Context
): BrowserRuntime {
    private val geckoRuntime = GeckoBrowserRuntime.getInstance(context)

    override fun createSession(): BrowserSession {
        return geckoRuntime.createSession()
    }

    override fun shutdown() {
       geckoRuntime.shutdown()
    }
}