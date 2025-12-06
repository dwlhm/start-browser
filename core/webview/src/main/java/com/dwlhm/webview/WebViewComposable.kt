package com.dwlhm.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

/**
 * JavaScript interface to receive theme color from the webpage
 */
private class ThemeColorInterface(
    private val onThemeColorExtracted: (Color?) -> Unit
) {
    @JavascriptInterface
    fun setThemeColor(colorString: String?) {
        val color = colorString?.let { parseColor(it) }
        onThemeColorExtracted(color)
    }
    
    private fun parseColor(colorString: String): Color? {
        return try {
            val cleanColor = colorString.trim()
            when {
                // Handle hex colors: #RGB, #RRGGBB, #RRGGBBAA
                cleanColor.startsWith("#") -> {
                    val hex = cleanColor.removePrefix("#")
                    when (hex.length) {
                        3 -> {
                            // #RGB -> #RRGGBB
                            val r = hex[0].toString().repeat(2).toInt(16)
                            val g = hex[1].toString().repeat(2).toInt(16)
                            val b = hex[2].toString().repeat(2).toInt(16)
                            Color(r, g, b)
                        }
                        6 -> {
                            val colorInt = hex.toLong(16)
                            Color(
                                red = ((colorInt shr 16) and 0xFF).toInt() / 255f,
                                green = ((colorInt shr 8) and 0xFF).toInt() / 255f,
                                blue = (colorInt and 0xFF).toInt() / 255f
                            )
                        }
                        8 -> {
                            val colorInt = hex.toLong(16)
                            Color(
                                red = ((colorInt shr 24) and 0xFF).toInt() / 255f,
                                green = ((colorInt shr 16) and 0xFF).toInt() / 255f,
                                blue = ((colorInt shr 8) and 0xFF).toInt() / 255f,
                                alpha = (colorInt and 0xFF).toInt() / 255f
                            )
                        }
                        else -> null
                    }
                }
                // Handle rgb(r, g, b) and rgba(r, g, b, a)
                cleanColor.startsWith("rgb") -> {
                    val values = cleanColor
                        .substringAfter("(")
                        .substringBefore(")")
                        .split(",")
                        .map { it.trim() }
                    
                    if (values.size >= 3) {
                        val r = values[0].toIntOrNull() ?: return null
                        val g = values[1].toIntOrNull() ?: return null
                        val b = values[2].toIntOrNull() ?: return null
                        val a = if (values.size >= 4) values[3].toFloatOrNull() ?: 1f else 1f
                        Color(r / 255f, g / 255f, b / 255f, a)
                    } else null
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * JavaScript code to extract dominant/background color from the page
 */
private const val EXTRACT_THEME_COLOR_JS = """
    (function() {
        function getComputedBgColor(element) {
            if (!element) return null;
            var style = window.getComputedStyle(element);
            var bg = style.backgroundColor;
            if (bg && bg !== 'rgba(0, 0, 0, 0)' && bg !== 'transparent') {
                return bg;
            }
            return null;
        }
        
        function rgbToHex(rgb) {
            if (!rgb || rgb === 'transparent' || rgb === 'rgba(0, 0, 0, 0)') return null;
            var match = rgb.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/);
            if (match) {
                var r = parseInt(match[1]).toString(16).padStart(2, '0');
                var g = parseInt(match[2]).toString(16).padStart(2, '0');
                var b = parseInt(match[3]).toString(16).padStart(2, '0');
                return '#' + r + g + b;
            }
            return rgb;
        }
        
        function isValidColor(color) {
            if (!color) return false;
            // Skip white/near-white and transparent
            if (color === '#ffffff' || color === '#fff' || color === 'white') return false;
            if (color === 'transparent' || color === 'rgba(0, 0, 0, 0)') return false;
            return true;
        }
        
        var themeColor = null;
        
        // 1. Try header/nav elements first (often contains brand color)
        var headerSelectors = ['header', 'nav', '.header', '.navbar', '.nav', '#header', '#nav', '.top-bar', '.topbar'];
        for (var i = 0; i < headerSelectors.length; i++) {
            var header = document.querySelector(headerSelectors[i]);
            if (header) {
                var headerBg = getComputedBgColor(header);
                if (headerBg && isValidColor(rgbToHex(headerBg))) {
                    themeColor = headerBg;
                    break;
                }
            }
        }
        
        // 2. Try body background
        if (!themeColor) {
            var bodyBg = getComputedBgColor(document.body);
            if (bodyBg && isValidColor(rgbToHex(bodyBg))) {
                themeColor = bodyBg;
            }
        }
        
        // 3. Try html element background
        if (!themeColor) {
            var htmlBg = getComputedBgColor(document.documentElement);
            if (htmlBg && isValidColor(rgbToHex(htmlBg))) {
                themeColor = htmlBg;
            }
        }
        
        // 4. Sample most common background color from top elements
        if (!themeColor) {
            var colorCounts = {};
            var elements = document.querySelectorAll('header, nav, div, section, main, aside');
            var count = Math.min(elements.length, 50);
            
            for (var j = 0; j < count; j++) {
                var el = elements[j];
                var rect = el.getBoundingClientRect();
                // Only consider elements in the viewport top area
                if (rect.top < window.innerHeight * 0.3 && rect.width > 100) {
                    var bg = getComputedBgColor(el);
                    if (bg) {
                        var hex = rgbToHex(bg);
                        if (isValidColor(hex)) {
                            colorCounts[hex] = (colorCounts[hex] || 0) + 1;
                        }
                    }
                }
            }
            
            var maxCount = 0;
            for (var color in colorCounts) {
                if (colorCounts[color] > maxCount) {
                    maxCount = colorCounts[color];
                    themeColor = color;
                }
            }
        }
        
        // Convert to hex if needed
        if (themeColor && themeColor.startsWith('rgb')) {
            themeColor = rgbToHex(themeColor);
        }
        
        ThemeColorBridge.setThemeColor(themeColor);
    })();
"""

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
                
                // Add JavaScript interface untuk theme color extraction
                addJavascriptInterface(
                    ThemeColorInterface { color ->
                        state.themeColor = color
                    },
                    "ThemeColorBridge"
                )
                
                // WebViewClient untuk handling navigasi
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let {
                            state.currentUrl = it
                            state.isLoading = true
                            // Reset theme color saat memulai loading halaman baru
                            state.themeColor = null
                            onPageStarted?.invoke(it)
                        }
                        updateNavigationState(view)
                    }
                    
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        state.isLoading = false
                        url?.let { onPageFinished?.invoke(it) }
                        updateNavigationState(view)
                        
                        // Extract theme color setelah halaman selesai loading
                        view?.evaluateJavascript(EXTRACT_THEME_COLOR_JS, null)
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
