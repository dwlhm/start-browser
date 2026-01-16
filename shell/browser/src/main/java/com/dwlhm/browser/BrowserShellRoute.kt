package com.dwlhm.browser

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.dwlhm.tabmanager.api.TabHandle
import kotlinx.coroutines.flow.Flow

@Composable
fun BrowserShellRoute(
    onNavigateUp: () -> Unit,
    onGoToHome: () -> Unit,
    sessionFlow: Flow<TabHandle?>,
) {
    val currentHandle by sessionFlow.collectAsState(initial = null)

    if (currentHandle == null) {
        return
    }

    val activeHandle = currentHandle!!

    val viewModel = remember(activeHandle.id) {
        BrowserShellViewModel(browserSession = activeHandle.session)
    }

    key(activeHandle) {
        BrowserShell(
            onNavigateUp = onNavigateUp,
            onGoToHome = onGoToHome,
            viewModel = viewModel,
            viewHost = activeHandle.viewHost
        )
    }
}