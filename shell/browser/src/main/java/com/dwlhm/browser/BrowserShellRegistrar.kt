package com.dwlhm.browser

import com.dwlhm.navigation.api.RouteRegistrar
import java.net.URLDecoder

fun registerBrowserShell(
    routeRegistrar: RouteRegistrar,
    session: BrowserSession,
) {
    val viewModel = BrowserShellViewModel(
        session = session,
    )

    routeRegistrar.register(
        route = "browser?url={url}",
        content = { navController, backStackEntry ->
            val encodeUrl = backStackEntry.arguments?.getString("url")
            val initialUrl = encodeUrl?.let {
                URLDecoder.decode(it, "UTF-8")
            }

            BrowserShell(
                initialUrl = initialUrl,
                onNavigateUp = {
                    navController.popBackStack()
                },
                onGoToHome = {
                    navController.navigate("home")
                },
                viewModel = viewModel,
            )
        }
    )
}