package com.dwlhm.browser

import com.dwlhm.navigation.api.RouteRegistrar
import java.net.URLDecoder

fun registerBrowserShell(
    routeRegistrar: RouteRegistrar,
    tabManager: TabManager,
    viewHost: BrowserViewHost,
) {
    routeRegistrar.register(
        route = "browser?url={url}",
        content = { navController, backStackEntry ->
            val encodeUrl = backStackEntry.arguments?.getString("url")
            val initialUrl = encodeUrl?.let {
                URLDecoder.decode(it, "UTF-8")
            }

            BrowserShellRoute(
                initialUrl = initialUrl,
                onNavigateUp = {
                    navController.popBackStack()
                },
                onGoToHome = {
                    navController.navigate("home")
                },
                tabManager,
                viewHost
            )
        }
    )
}