package com.dwlhm.browser.api

import com.dwlhm.browser.ui.BrowserScreen
import com.dwlhm.navigation.api.RouteRegistrar
import java.net.URLDecoder

fun registerBrowserScreen(routeRegistrar: RouteRegistrar) {
    routeRegistrar.register(
        route = "browser?url={url}",
        content = { _, backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url")
            val initialUrl = encodedUrl?.let { URLDecoder.decode(it, "UTF-8") }
                ?: "https://www.google.com"

            BrowserScreen(
                initialUrl = initialUrl
            )
        }
    )
}
