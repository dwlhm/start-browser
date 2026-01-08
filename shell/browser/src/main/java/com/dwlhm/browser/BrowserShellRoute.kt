package com.dwlhm.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun BrowserShellRoute(
    initialUrl: String?,
    onNavigateUp: () -> Unit,
    onGoToHome: () -> Unit,
    tabManager: TabManager,
    viewHost: BrowserViewHost,
) {
    val viewModel: BrowserShellViewModel = remember {
        BrowserShellViewModel(
            tabManager = tabManager
        )
    }

    LaunchedEffect(Unit) {
        viewModel.onShellReady()
        viewModel.init(initialUrl)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onShellGone()
        }
    }

    BrowserShell(
        onNavigateUp,
        onGoToHome,
        viewModel,
        viewHost
    )
}