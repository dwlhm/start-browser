package com.dwlhm.webview.navigation

import com.dwlhm.webview.WebViewSession
import kotlinx.coroutines.flow.StateFlow

interface SessionNavigator {
    val activeSession: StateFlow<WebViewSession?>

    val currentUrl: StateFlow<String?>
    val currentTitle: StateFlow<String?>

    val canGoBack: StateFlow<Boolean>
    val canGoForward: StateFlow<Boolean>

    fun loadUrl(url: String)
    fun goBack(): Boolean
    fun goForward(): Boolean
}