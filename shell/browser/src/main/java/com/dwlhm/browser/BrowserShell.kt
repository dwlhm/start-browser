package com.dwlhm.browser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dwlhm.browser.ui.BrowserView
import com.dwlhm.ui.button.IconButton
import com.dwlhm.ui.input.InputUri
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowRight
import compose.icons.feathericons.Square

@Composable
fun BrowserShell(
    onNavigateUp: () -> Unit,
    onGoToHome: () -> Unit,
    viewModel: BrowserShellViewModel,
    viewHost: BrowserViewHost,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Back handler
    BackHandler {
        val handled = viewModel.goBack()
        if (!handled) onNavigateUp()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding() // agar tidak tertutupi keyboard
    ) {
        // Browser View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            BrowserView(
                browserViewHost = viewHost,
                modifier = Modifier.fillMaxSize(),
                context = context
            )
        }

        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Forward button
            IconButton(
                onClick = { viewModel.goForward() },
                enabled = uiState.canGoForward
            ) {
                Icon(
                    imageVector = FeatherIcons.ArrowRight,
                    contentDescription = "Forward",
                    tint = if (uiState.canGoForward) Color.Black else Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // URL input
            InputUri(
                value = uiState.inputUrl,
                modifier = Modifier.weight(1f),
                onValueChange = viewModel::onUrlChange,
                onSubmit = viewModel::onUrlSubmit,
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Home button
            IconButton(
                onClick = onGoToHome
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
