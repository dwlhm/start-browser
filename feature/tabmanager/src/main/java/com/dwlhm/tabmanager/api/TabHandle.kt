package com.dwlhm.tabmanager.api

import com.dwlhm.browser.BrowserSession
import com.dwlhm.browser.BrowserViewHost

data class TabHandle(
    val id: String,
    val session: BrowserSession,
    val viewHost: BrowserViewHost,
    val mode: TabMode
)
