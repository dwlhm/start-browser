package com.dwlhm.webview.tabmanager

import com.dwlhm.webview.WebViewSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface TabManager {
    val tabs: StateFlow<List<WebViewSession>>
    val activeSession: StateFlow<WebViewSession?>

    fun addTab(url: String)
    fun switchTab(tabId: TabId)
    fun closeTab(tabId: TabId)
}