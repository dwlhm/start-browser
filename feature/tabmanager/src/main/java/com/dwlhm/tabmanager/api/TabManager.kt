package com.dwlhm.tabmanager.api

import com.dwlhm.webview.WebViewSession
import kotlinx.coroutines.flow.StateFlow

interface TabManager {
    val tabs: StateFlow<List<TabSnapshot>>
    val activeSession: StateFlow<WebViewSession?>
    val activeTabId: StateFlow<TabId?>

    fun addTab(
        url: String,
        defaultTabId: TabId? = null
    )
    fun switchTab(tabId: TabId, fallbackUrl: String)
    fun closeTab(tabId: TabId)
}