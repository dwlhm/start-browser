package com.dwlhm.browser

import com.dwlhm.browser.session.SessionManager
import com.dwlhm.browser.session.SessionRegistry
import com.dwlhm.navigation.api.RouteRegistrar

fun registerBrowserShell(
    routeRegistrar: RouteRegistrar,
    sessionManager: SessionManager,
    sessionRegistry: SessionRegistry,
) {
    routeRegistrar.register(
        route = "browser",
        content = { navController, backStackEntry ->
            BrowserShellRoute(
                onNavigateUp = {
                    sessionManager.minimizeSession()
                    navController.popBackStack()
                },
                onGoToHome = {
                    sessionManager.minimizeSession()
                    navController.navigate("dashboard-session")
                },
                sessionManager = sessionManager,
                sessionRegistry = sessionRegistry,
            )
        }
    )
}