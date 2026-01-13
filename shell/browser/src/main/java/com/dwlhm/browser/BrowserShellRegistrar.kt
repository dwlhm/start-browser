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
                    // Suspend current tab sebelum navigasi kembali
                    tabSessionManager.suspendCurrentTab()
                    navController.popBackStack()
                },
                onGoToHome = {
                    // Suspend current tab sebelum navigasi ke home/dashboard
                    // Jika tab memutar media, akan tetap aktif di background
                    // Jika tidak, akan sepenuhnya di-suspend
                    tabSessionManager.suspendCurrentTab()
                    navController.navigate("dashboard-session")
                },
                session,
            )
        }
    )
}