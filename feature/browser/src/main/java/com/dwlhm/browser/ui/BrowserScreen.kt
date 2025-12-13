package com.dwlhm.browser.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun BrowserScreen(tabManager: BrowserTabManager, initialUrl: String) {
    val session by tabManager.activeSession.collectAsState()

    LaunchedEffect(initialUrl) {
        tabManager.addTab(initialUrl)
    }

    session?.ComposableView(modifier = Modifier.fillMaxSize())
}
