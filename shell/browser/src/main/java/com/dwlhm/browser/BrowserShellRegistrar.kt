package com.dwlhm.browser

import com.dwlhm.navigation.api.RouteRegistrar
import com.dwlhm.tabmanager.api.TabHandle
import com.dwlhm.tabmanager.api.TabSessionManager
import kotlinx.coroutines.flow.MutableStateFlow

fun registerBrowserShell(
    routeRegistrar: RouteRegistrar,
    session: MutableStateFlow<TabHandle?>,
    tabSessionManager: TabSessionManager,
) {
    routeRegistrar.register(
        route = "browser",
        content = { navController, backStackEntry ->
            BrowserShellRoute(
                onNavigateUp = {
                    tabSessionManager.suspendCurrentTab()
                    navController.popBackStack()
                },
                onGoToHome = {
                    tabSessionManager.suspendCurrentTab()
                    navController.navigate("dashboard-session")
                },
                session,
            )
        }
    )
}