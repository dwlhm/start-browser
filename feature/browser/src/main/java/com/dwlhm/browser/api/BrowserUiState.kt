package com.dwlhm.browser.api

data class BrowserUiState(
    val inputUrl: String = "",
    val hasSession: Boolean = false,
    val canGoForward: Boolean = false,
)
