package com.dwlhm.browser.ui

import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.dwlhm.webview.BrowserWebView
import com.dwlhm.webview.rememberWebViewState

/**
 * Default toolbar/status bar color when no theme color is available
 */
private val DefaultToolbarColor = Color(0xFFF5F5F5)

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    initialUrl: String = "https://www.google.com"
) {
    val uiState by viewModel.uiState.collectAsState()
    val webViewState = rememberWebViewState(initialUrl)
    val focusManager = LocalFocusManager.current
    
    // Sync WebView state dengan ViewModel
    LaunchedEffect(webViewState.currentUrl) {
        if (webViewState.currentUrl.isNotEmpty()) {
            viewModel.updateCurrentUrl(webViewState.currentUrl)
        }
    }
    
    LaunchedEffect(webViewState.isLoading) {
        viewModel.updateLoadingState(webViewState.isLoading)
    }
    
    LaunchedEffect(webViewState.progress) {
        viewModel.updateProgress(webViewState.progress)
    }
    
    LaunchedEffect(webViewState.pageTitle) {
        viewModel.updatePageTitle(webViewState.pageTitle)
    }
    
    LaunchedEffect(webViewState.canGoBack, webViewState.canGoForward) {
        viewModel.updateNavigationState(webViewState.canGoBack, webViewState.canGoForward)
    }
    
    // Sync theme color from WebView state to ViewModel
    LaunchedEffect(webViewState.themeColor) {
        viewModel.updateThemeColor(webViewState.themeColor)
    }
    
    // Animate the status bar color change
    val statusBarColor by animateColorAsState(
        targetValue = uiState.themeColor ?: DefaultToolbarColor,
        animationSpec = tween(durationMillis = 300),
        label = "status_bar_color"
    )
    
    // Update status bar color when theme changes
    DynamicStatusBar(color = statusBarColor)
    
    // Handle back press untuk navigasi WebView
    BackHandler(enabled = uiState.canGoBack) {
        webViewState.goBack()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // WebView Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            BrowserWebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize(),
                initialUrl = initialUrl,
                enableJavaScript = true,
                enableDomStorage = true
            )
        }


        // Loading Progress Bar
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(uiState.progress)
                        .height(3.dp)
                        .background(Color(0xFF4285F4))
                )
            }
        }

        // Browser Toolbar with dynamic theme color
        BrowserToolbar(
            url = uiState.inputUrl,
            onUrlChange = { viewModel.updateInputUrl(it) },
            onUrlSubmit = { 
                val formattedUrl = viewModel.submitUrl(it)
                webViewState.loadUrl(formattedUrl)
                focusManager.clearFocus()
            },
            onFocusChanged = { viewModel.setUrlBarFocused(it) },
            themeColor = uiState.themeColor
        )
    }
}

/**
 * Composable to dynamically update status bar color and icon appearance
 */
@Composable
private fun DynamicStatusBar(color: Color) {
    val view = LocalView.current
    if (view.isInEditMode) return
    
    val window = (view.context as? Activity)?.window ?: return
    val useDarkIcons = color.luminance() > 0.5f
    
    SideEffect {
        // Update status bar color on pre-API 35 devices
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = color.toArgb()
        }
        
        // Update status bar icon colors
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = useDarkIcons
    }
}
