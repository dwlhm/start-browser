package com.dwlhm.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dwlhm.ui.card.SimpleCard

@Composable
fun LastVisitedComposable(
    modifier: Modifier = Modifier,
    viewModel: LastVisitedViewModel = hiltViewModel(),
    onLastVisitedClick: (url: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.hasLastVisited) {
        SimpleCard(
            title = uiState.title,
            description = uiState.url,
            modifier = modifier
                .fillMaxWidth()
                .clickable { onLastVisitedClick(uiState.url) }
        )
    } else {
        SimpleCard(
            title = "No recent visits",
            description = "Your browsing history will appear here",
            modifier = modifier.fillMaxWidth()
        )
    }
}