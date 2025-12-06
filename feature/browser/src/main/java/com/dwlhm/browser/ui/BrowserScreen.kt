package com.dwlhm.browser.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.dwlhm.webview.BrowserWebView
import com.dwlhm.webview.rememberWebViewState

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

        // Browser Toolbar
        BrowserToolbar(
            url = uiState.inputUrl,
            onUrlChange = { viewModel.updateInputUrl(it) },
            onUrlSubmit = { 
                val formattedUrl = viewModel.submitUrl(it)
                webViewState.loadUrl(formattedUrl)
                focusManager.clearFocus()
            },
            onFocusChanged = { viewModel.setUrlBarFocused(it) }
        )
    }
}
