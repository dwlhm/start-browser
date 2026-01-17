package com.dwlhm.dashboardsession

import com.dwlhm.browser.session.SessionManager
import com.dwlhm.browser.session.SessionRegistry
import com.dwlhm.navigation.api.RouteRegistrar

fun registerDashboardSessionShell(
    routeRegistrar: RouteRegistrar,
    sessionManager: SessionManager,
    sessionRegistry: SessionRegistry,
) {
    routeRegistrar.register(
        route = "dashboard-session",
        content = { navController, _ ->

            val viewModel by lazy { DashboardSessionViewModel(
                sessionRegistry = sessionRegistry,
                sessionManager = sessionManager,
                navController = navController,
            ) }
            DashboardSessionShell(
                viewModel,
            )
        }
    )
}