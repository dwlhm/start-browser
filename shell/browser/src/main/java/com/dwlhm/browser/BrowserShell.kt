package com.dwlhm.browser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.dwlhm.browser.ui.BrowserView
import com.dwlhm.ui.button.IconButton
import com.dwlhm.ui.input.InputUri
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowRight
import compose.icons.feathericons.Square

@Composable
fun BrowserShell(
    initialUrl: String?,
    onNavigateUp: () -> Unit,
    onGoToHome: () -> Unit,
    viewModel: BrowserShellViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init(initialUrl)
    }

    BackHandler {
        val handled = viewModel.browserSession.goBack()
        if (!handled) {
            onNavigateUp()
        }
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
                BrowserView(
                    session = viewModel.browserSession,
                    modifier = Modifier.fillMaxSize(),
                    context = LocalContext.current
                )
            }
            Box {
                Row {
                    if (uiState.canGoForward) {
                        IconButton (
                            onClick = {
                                viewModel.browserSession.goForward()
                            }
                        ) {
                            Icon(
                                imageVector = FeatherIcons.ArrowRight,
                                contentDescription = "Forward",
                                tint = Color.Black
                            )
                        }
                    }
                    InputUri(
                        value = uiState.inputUrl,
                        modifier = Modifier.weight(1f),
                        onValueChange = viewModel::onUrlChange,
                        onSubmit = viewModel::onUrlSubmit,
                    )
                    IconButton(
                        onClick = {
                            onGoToHome()
                        }
                    ) {
                        Icon(
                            imageVector = FeatherIcons.Square,
                            contentDescription = "Home",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}