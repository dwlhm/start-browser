package com.dwlhm.browser

import com.dwlhm.navigation.api.RouteRegistrar
import com.dwlhm.tabmanager.api.TabCoordinator
import com.dwlhm.tabmanager.api.TabHandle
import com.dwlhm.tabmanager.api.TabListCoordinator
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLDecoder
import androidx.compose.runtime.collectAsState

fun registerBrowserShell(
    routeRegistrar: RouteRegistrar,
    tabListCoordinator: TabListCoordinator,
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
                tabListCoordinator = tabListCoordinator
            )
        }
    )
}