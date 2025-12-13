package com.dwlhm.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow

interface WebViewSession {
    @Composable
    fun ComposableView(modifier: Modifier)
    fun attachToView(view: Any)
    fun loadUrl(url: String)
    fun close()
    val canGoBack: StateFlow<Boolean>
    fun goBack()
    val canGoForward: StateFlow<Boolean>
    fun goForward()
    val currentUrl: StateFlow<String?>
}