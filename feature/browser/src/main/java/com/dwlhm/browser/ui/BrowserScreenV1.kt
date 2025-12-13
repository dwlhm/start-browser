package com.dwlhm.browser.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.dwlhm.ui.systembar.DynamicSystemBar
import com.dwlhm.webview.BrowserWebView
import com.dwlhm.webview.WebViewState

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
    val focusManager = LocalFocusManager.current
    val tabManager = viewModel.tabManager
    
    // Initialize tabs
    LaunchedEffect(Unit) {
        viewModel.initializeTabs(initialUrl)
    }
    
    // Get active tab and its WebView state
    val activeTab = tabManager.activeTab
    val activeTabId = tabManager.activeTabId
    
    // Create or get WebViewState for active tab
    var webViewState by remember(activeTabId) { 
        mutableStateOf(tabManager.getActiveWebViewState() ?: WebViewState(activeTab?.initialUrl ?: initialUrl))
    }
    
    // Update webViewState when switching tabs
    LaunchedEffect(activeTabId) {
        tabManager.getActiveWebViewState()?.let {
            webViewState = it
        }
    }

    // Sync WebView state dengan ViewModel
    LaunchedEffect(webViewState.themeColor) {
        viewModel.updateThemeColor(webViewState.themeColor)
        viewModel.updateActiveTabInfo(
            url = webViewState.currentUrl,
            title = webViewState.pageTitle,
            themeColor = webViewState.themeColor
        )
    }
    
    LaunchedEffect(webViewState.canGoBack, webViewState.canGoForward) {
        viewModel.updateNavigationState(webViewState.canGoBack, webViewState.canGoForward)
    }
    
    // Animate the status bar color change
    val statusBarColor by animateColorAsState(
        targetValue = if (uiState.showTabList) Color(0xFFF5F5F5) else (uiState.themeColor ?: DefaultToolbarColor),
        animationSpec = tween(durationMillis = 300),
        label = "status_bar_color"
    )
    
    // Update status bar color when theme changes
    DynamicSystemBar(color = statusBarColor)
    
    // Handle back press
    BackHandler(enabled = uiState.showTabList || uiState.canGoBack) {
        when {
            uiState.showTabList -> viewModel.setShowTabList(false)
            uiState.canGoBack -> webViewState.goBack()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main browser content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // WebView Content - keyed by tab ID to preserve state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                key(activeTabId) {
                    BrowserWebView(
                        state = webViewState,
                        modifier = Modifier.fillMaxSize(),
                        initialUrl = activeTab?.initialUrl ?: initialUrl,
                        enableJavaScript = true,
                        enableDomStorage = true,
                        onPageStarted = { url ->
                            viewModel.updateLoadingState(true)
                            viewModel.updateCurrentUrl(url)
                        },
                        onPageFinished = { url ->
                            viewModel.onPageFinished(url, webViewState.pageTitle)
                            viewModel.updateActiveTabInfo(
                                url = url,
                                title = webViewState.pageTitle,
                                themeColor = webViewState.themeColor
                            )
                        },
                        onProgressChanged = { progress ->
                            viewModel.updateProgress(progress / 100f)
                        }
                    )
                }
            }

            // Loading Progress Bar
            AnimatedVisibility(
                visible = uiState.isLoading && !uiState.showTabList,
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

            // Browser Toolbar with dynamic theme color and tab button
            if (!uiState.showTabList) {
                BrowserToolbar(
                    url = uiState.inputUrl,
                    onUrlChange = { viewModel.updateInputUrl(it) },
                    onUrlSubmit = { 
                        val formattedUrl = viewModel.submitUrl(it)
                        webViewState.loadUrl(formattedUrl)
                        focusManager.clearFocus()
                    },
                    onFocusChanged = { viewModel.setUrlBarFocused(it) },
                    themeColor = uiState.themeColor,
                    tabCount = uiState.tabCount,
                    onTabButtonClick = { viewModel.toggleTabList() }
                )
            }
        }
        
        // Tab List Overlay
        TabListOverlay(
            visible = uiState.showTabList,
            tabs = tabManager.tabs,
            activeTabId = activeTabId,
            onTabClick = { tabId ->
                viewModel.switchToTab(tabId)
            },
            onTabClose = { tabId ->
                viewModel.closeTab(tabId)
            },
            onNewTab = {
                viewModel.createNewTab()
            },
            onDismiss = {
                viewModel.setShowTabList(false)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
