package com.dwlhm.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dwlhm.tabmanager.api.TabSessionManager

@Composable
fun BrowserShellRoute(
    initialUrl: String?,
    onNavigateUp: () -> Unit,
    onGoToHome: () -> Unit,
    tabSessionManager: TabSessionManager,
) {
    val activeTab by tabSessionManager.selectedTab.collectAsState()

    // Create tab if needed
    LaunchedEffect(Unit) {
        if (tabSessionManager.selectedTab.value == null) {
            tabSessionManager.createTab()
        }
    }

    // Wait for tab to be ready
    val currentTab = activeTab ?: return

    val viewModel = remember(currentTab.id) {
        BrowserShellViewModel(browserSession = currentTab.session)
    }

    LaunchedEffect(currentTab.id) {
        viewModel.loadInitialUrl(initialUrl)
    }

    DisposableEffect(currentTab.id) {
        onDispose {
            tabSessionManager.closeTab(currentTab.id)
        }
    }

    BrowserShell(
        onNavigateUp = onNavigateUp,
        onGoToHome = onGoToHome,
        viewModel = viewModel,
        viewHost = currentTab.viewHost
    )
}