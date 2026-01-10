package com.dwlhm.dashboardsession

import com.dwlhm.navigation.api.RouteRegistrar
import com.dwlhm.tabmanager.api.TabSessionManager
import com.dwlhm.utils.normalizeUrl

fun registerDashboardSessionShell(
    routeRegistrar: RouteRegistrar,
    tabSessionManager: TabSessionManager,
) {
    routeRegistrar.register(
        route = "dashboard-session",
        content = { navController, _ ->
            fun onSearchClick(url: String) {
                tabSessionManager.createTab()
                navController.navigate("browser?url=$url")
            }
            DashboardSessionShell(
                onValueChange = { url ->
                    onSearchClick(normalizeUrl(url))
                }
            )
        }
    )
}