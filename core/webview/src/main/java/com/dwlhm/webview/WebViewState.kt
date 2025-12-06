package com.dwlhm.webview

import android.graphics.Bitmap
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * State holder untuk WebView yang menyimpan state navigasi dan loading
 */
@Stable
class WebViewState(
    initialUrl: String = ""
) {
    var webView: WebView? by mutableStateOf(null)
        internal set
    
    var currentUrl: String by mutableStateOf(initialUrl)
        internal set
    
    var pageTitle: String by mutableStateOf("")
        internal set
    
    var isLoading: Boolean by mutableStateOf(false)
        internal set
    
    var progress: Float by mutableFloatStateOf(0f)
        internal set
    
    var canGoBack: Boolean by mutableStateOf(false)
        internal set
    
    var canGoForward: Boolean by mutableStateOf(false)
        internal set
    
    var favicon: Bitmap? by mutableStateOf(null)
        internal set
    
    var lastLoadedUrl: String by mutableStateOf("")
        internal set
    
    /**
     * Load URL baru
     */
    fun loadUrl(url: String) {
        val formattedUrl = formatUrl(url)
        webView?.loadUrl(formattedUrl)
    }
    
    /**
     * Reload halaman saat ini
     */
    fun reload() {
        webView?.reload()
    }
    
    /**
     * Navigasi ke halaman sebelumnya
     */
    fun goBack() {
        if (canGoBack) {
            webView?.goBack()
        }
    }
    
    /**
     * Navigasi ke halaman selanjutnya
     */
    fun goForward() {
        if (canGoForward) {
            webView?.goForward()
        }
    }
    
    /**
     * Stop loading halaman
     */
    fun stopLoading() {
        webView?.stopLoading()
    }
    
    /**
     * Format URL dengan menambahkan https:// jika belum ada
     */
    private fun formatUrl(url: String): String {
        val trimmedUrl = url.trim()
        return when {
            trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://") -> trimmedUrl
            trimmedUrl.contains(".") && !trimmedUrl.contains(" ") -> "https://$trimmedUrl"
            else -> "https://www.google.com/search?q=${trimmedUrl.replace(" ", "+")}"
        }
    }
}

/**
 * Remember WebViewState untuk digunakan di Composable
 */
@Composable
fun rememberWebViewState(initialUrl: String = ""): WebViewState {
    return remember { WebViewState(initialUrl) }
}
