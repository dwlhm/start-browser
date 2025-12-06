package com.dwlhm.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

/**
 * Composable wrapper untuk Android WebView dengan Jetpack WebKit
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    initialUrl: String = "",
    enableJavaScript: Boolean = true,
    enableDomStorage: Boolean = true,
    enableDarkMode: Boolean = false,
    userAgent: String? = null,
    onPageStarted: ((String) -> Unit)? = null,
    onPageFinished: ((String) -> Unit)? = null,
    onProgressChanged: ((Int) -> Unit)? = null,
) {
    // Load initial URL when state is first created
    LaunchedEffect(initialUrl) {
        if (initialUrl.isNotEmpty() && state.lastLoadedUrl != initialUrl) {
            state.loadUrl(initialUrl)
            state.lastLoadedUrl = initialUrl
        }
    }
    
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                // Simpan referensi WebView ke state
                state.webView = this
                
                // Konfigurasi WebSettings
                settings.apply {
                    javaScriptEnabled = enableJavaScript
                    domStorageEnabled = enableDomStorage
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    
                    // Cache settings
                    cacheMode = WebSettings.LOAD_DEFAULT
                    
                    // Mixed content mode untuk HTTPS
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    
                    // Media playback
                    mediaPlaybackRequiresUserGesture = false
                    
                    // Custom user agent jika ada
                    userAgent?.let { userAgentString = it }
                }
                
                // Dark mode support menggunakan AndroidX WebKit
                if (enableDarkMode && WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
                }
                
                // Safe browsing jika didukung
                if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
                    WebSettingsCompat.setSafeBrowsingEnabled(settings, true)
                }
                
                // WebViewClient untuk handling navigasi
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let {
                            state.currentUrl = it
                            state.isLoading = true
                            onPageStarted?.invoke(it)
                        }
                        updateNavigationState(view)
                    }
                    
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        state.isLoading = false
                        url?.let { onPageFinished?.invoke(it) }
                        updateNavigationState(view)
                    }
                    
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        // Biarkan WebView handle URL secara internal
                        return false
                    }
                    
                    private fun updateNavigationState(view: WebView?) {
                        view?.let {
                            state.canGoBack = it.canGoBack()
                            state.canGoForward = it.canGoForward()
                        }
                    }
                }
                
                // WebChromeClient untuk progress dan title
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        state.progress = newProgress / 100f
                        onProgressChanged?.invoke(newProgress)
                    }
                    
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        state.pageTitle = title ?: ""
                    }
                    
                    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                        super.onReceivedIcon(view, icon)
                        state.favicon = icon
                    }
                }
                
                // Load initial URL jika ada
                if (initialUrl.isNotEmpty()) {
                    loadUrl(initialUrl)
                }
            }
        },
        update = { webView ->
            // Update settings jika ada perubahan
            webView.settings.javaScriptEnabled = enableJavaScript
            webView.settings.domStorageEnabled = enableDomStorage
        }
    )
    
    // Cleanup saat composable di-dispose
    DisposableEffect(Unit) {
        onDispose {
            state.webView?.apply {
                stopLoading()
                clearHistory()
            }
            state.webView = null
        }
    }
}
