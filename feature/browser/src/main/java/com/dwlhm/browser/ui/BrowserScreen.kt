package com.dwlhm.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.dwlhm.ui.input.InputUri

@Composable
fun BrowserScreen(
    initialUrl: String,
    viewModel: BrowserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val session by viewModel.activeSession.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init(initialUrl)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                session?.ComposableView(modifier = Modifier.fillMaxSize())

            }
            Box {
                Row {
                    InputUri(
                        value = uiState.inputUrl,
                        modifier = Modifier.weight(1f),
                        onValueChange = viewModel::onUrlChange,
                        onSubmit = viewModel::onUrlSubmit,
                    )
                }
            }
        }
    }
}
