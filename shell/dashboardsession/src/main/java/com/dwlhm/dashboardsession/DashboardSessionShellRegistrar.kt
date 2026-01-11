package com.dwlhm.dashboardsession

import androidx.compose.runtime.rememberCoroutineScope
import com.dwlhm.data.room.sessions.SessionEntity
import com.dwlhm.navigation.api.RouteRegistrar
import com.dwlhm.tabmanager.api.TabSessionManager
import com.dwlhm.utils.normalizeUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun registerDashboardSessionShell(
    routeRegistrar: RouteRegistrar,
    tabSessionManager: TabSessionManager,
    sessions: Flow<List<SessionEntity>>,
    removeSession: suspend (id: String) -> Unit,
) {
    routeRegistrar.register(
        route = "dashboard-session",
        content = { navController, _ ->
            val scope = rememberCoroutineScope()

            fun onSearchClick(url: String) {
                tabSessionManager.createTab()
                navController.navigate("browser?url=$url")
            }
            DashboardSessionShell(
                sessions,
                onValueChange = { url: String ->
                    onSearchClick(normalizeUrl(url))
                },
                onSessionClick = { session ->
                    tabSessionManager.openTab(session.id)
                    navController.navigate("browser?url=${session.url}")
                },
                onSessionClose = { session ->
                    tabSessionManager.closeTab(session.id)
                    scope.launch {
                        removeSession(session.id)
                    }
                }
            )
        }
    )
}