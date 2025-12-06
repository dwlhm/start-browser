package com.dwlhm.browser.api

import androidx.hilt.navigation.compose.hiltViewModel
import com.dwlhm.browser.ui.BrowserScreen
import com.dwlhm.browser.ui.BrowserViewModel
import com.dwlhm.navigation.api.RouteRegistrar

fun registerBrowserScreen(routeRegistrar: RouteRegistrar) {
    routeRegistrar.register(
        route = "browser",
        content = { _ ->
            val viewModel: BrowserViewModel = hiltViewModel()
            BrowserScreen(viewModel)
        }
    )
}
