package com.dwlhm.browser.api

import androidx.hilt.navigation.compose.hiltViewModel
import com.dwlhm.browser.ui.BrowserScreen
import com.dwlhm.browser.ui.BrowserTabManager
import com.dwlhm.browser.ui.BrowserViewModel
import com.dwlhm.navigation.api.RouteRegistrar
import java.net.URLDecoder

fun registerBrowserScreen(routeRegistrar: RouteRegistrar, tabManager: BrowserTabManager) {
    routeRegistrar.register(
        route = "browser?url={url}",
        content = { _, backStackEntry ->
            // Ambil URL dari route
            val encodedUrl = backStackEntry.arguments?.getString("url")
            val initialUrl = encodedUrl?.let { URLDecoder.decode(it, "UTF-8") }
                ?: "https://www.google.com"

            // BrowserScreen sekarang engine-agnostic
            BrowserScreen(
                tabManager = tabManager,
                initialUrl = initialUrl
            )
        }
    )
}
