package com.dwlhm.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.dwlhm.browser.api.DefaultBrowserMountController
import com.dwlhm.browser.session.SessionManager
import com.dwlhm.browser.session.SessionRegistry

@Composable
fun BrowserShellRoute(
    onNavigateUp: () -> Unit,
    onGoToHome: () -> Unit,
    sessionManager: SessionManager,
    sessionRegistry: SessionRegistry,
) {
    val currentSession by sessionManager.currentSession.collectAsState()
    val foregroundSessionId by sessionRegistry.foregroundSessionId.collectAsState()

    val activeSession = currentSession ?: return

    val viewModel = remember(activeSession, foregroundSessionId) {
        BrowserShellViewModel(
            browserSession = activeSession,
        )
    }

    key(activeSession) {
        BrowserShell(
            onNavigateUp = onNavigateUp,
            onGoToHome = onGoToHome,
            viewModel = viewModel,
            browserMountController = DefaultBrowserMountController(activeSession),
        )
    }
}