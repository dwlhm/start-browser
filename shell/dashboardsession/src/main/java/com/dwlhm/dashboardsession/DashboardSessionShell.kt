package com.dwlhm.dashboardsession

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun DashboardSessionShell(
    viewModel: DashboardSessionViewModel,
) {
    val inputUrl by viewModel.inputUrl.collectAsState()

    DashboardSessionShellContent(
        inputUrl = inputUrl,
        sessions = viewModel.sessions,
        onValueChange = viewModel::onInputChange,
        onSubmit = viewModel::onSubmit,
        onSessionClick = viewModel::onSessionClick,
        onSessionClose = viewModel::onSessionClose
    )
}