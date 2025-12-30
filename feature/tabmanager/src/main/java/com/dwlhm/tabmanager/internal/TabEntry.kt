package com.dwlhm.tabmanager.internal

import com.dwlhm.webview.WebViewSession
import com.dwlhm.tabmanager.api.TabId

internal data class TabEntry (
    val id: TabId,
    val session: WebViewSession
)