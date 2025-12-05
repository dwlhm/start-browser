package com.dwlhm.onboarding.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun AnimatedRotatingText(viewModel: AnimatedRotatingTextViewModel) {
    val index by viewModel.index.collectAsState()

    BasicText(
        text = viewModel.messageList[index],
    )
}