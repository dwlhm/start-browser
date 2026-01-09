package com.dwlhm.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.dwlhm.tabmanager.api.TabCoordinator
import com.dwlhm.tabmanager.api.TabMode

@Composable
fun BrowserShellRoute(
    initialUrl: String?,
    onNavigateUp: () -> Unit,
    onGoToHome: () -> Unit,
    tabCoordinator: TabCoordinator,
    tabMode: TabMode = TabMode.DEFAULT,
) {
    val tabHandle = remember { tabCoordinator.activateTab(tabMode) }

    val viewModel = remember {
        BrowserShellViewModel(browserSession = tabHandle.session)
    }

    LaunchedEffect(Unit) {
        viewModel.loadInitialUrl(initialUrl)
    }

    DisposableEffect(tabHandle) {
        onDispose {
            tabCoordinator.deactivateTab(tabHandle)
        }
    }

    BrowserShell(
        onNavigateUp = onNavigateUp,
        onGoToHome = onGoToHome,
        viewModel = viewModel,
        viewHost = tabHandle.viewHost
    )
}