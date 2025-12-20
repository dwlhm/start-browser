package com.dwlhm.tabmanager.internal

import com.dwlhm.webview.WebViewSession
import com.dwlhm.webview.tabmanager.TabId

internal data class TabEntry (
    val id: TabId,
    val session: WebViewSession
)