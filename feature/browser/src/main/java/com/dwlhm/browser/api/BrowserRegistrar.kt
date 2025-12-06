package com.dwlhm.browser.api

import androidx.hilt.navigation.compose.hiltViewModel
import com.dwlhm.browser.ui.BrowserScreen
import com.dwlhm.browser.ui.BrowserViewModel
import com.dwlhm.navigation.api.RouteRegistrar
import java.net.URLDecoder

fun registerBrowserScreen(routeRegistrar: RouteRegistrar) {
    routeRegistrar.register(
        route = "browser?url={url}",
        content = { _, backStackEntry ->
            val viewModel: BrowserViewModel = hiltViewModel()
            val encodedUrl = backStackEntry.arguments?.getString("url")
            val initialUrl = if (encodedUrl != null) {
                URLDecoder.decode(encodedUrl, "UTF-8")
            } else {
                "https://www.google.com"
            }
            BrowserScreen(
                viewModel = viewModel,
                initialUrl = initialUrl
            )
        }
    )
}
