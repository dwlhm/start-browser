package com.dwlhm.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dwlhm.tabmanager.api.TabListCoordinator

@Composable
fun BrowserShellRoute(
    initialUrl: String?,
    onNavigateUp: () -> Unit,
    onGoToHome: () -> Unit,
    tabListCoordinator: TabListCoordinator,
) {
    val activeTab by tabListCoordinator.selectedTab.collectAsState()

    // Create tab if needed (in LaunchedEffect to avoid side-effect in composition)
    LaunchedEffect(Unit) {
        if (tabListCoordinator.selectedTab.value == null) {
            tabListCoordinator.createTab()
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
            tabListCoordinator.closeTab(currentTab.id)
        }
    }

    BrowserShell(
        onNavigateUp = onNavigateUp,
        onGoToHome = onGoToHome,
        viewModel = viewModel,
        viewHost = currentTab.viewHost
    )
}