package com.dwlhm.dashboardsession

import androidx.compose.runtime.rememberCoroutineScope
import com.dwlhm.browser.BrowserTab
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
                tabSessionManager.createTab(url)
                navController.navigate("browser")
            }
            DashboardSessionShell(
                sessions,
                onValueChange = { url: String ->
                    onSearchClick(url = normalizeUrl(url))
                },
                onSessionClick = { session ->
                    tabSessionManager.openTab(
                        BrowserTab(
                            id = session.id,
                            url = session.url
                        )
                    )
                    navController.navigate("browser")
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