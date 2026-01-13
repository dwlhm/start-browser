package com.dwlhm.startbrowser.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.dwlhm.browser.BrowserTab
import com.dwlhm.browser.registerBrowserShell
import com.dwlhm.dashboardsession.registerDashboardSessionShell
import com.dwlhm.data.datastore.onboarding.OnboardingDatastore
import com.dwlhm.navigation.api.AppNavHost
import com.dwlhm.navigation.api.RouteRegistrar
import com.dwlhm.onboarding.api.registerOnboardingScreen
import com.dwlhm.startbrowser.MainApplication
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun AppRoot(
    routeRegistrar: RouteRegistrar,
    context: Context,
    intentEvent: SharedFlow<Intent>
) {
    AppTheme(AppTheme.colors) {
        MainScreen(routeRegistrar, context, intentEvent)
    }
}

@Composable
fun MainScreen(
    routeRegistrar: RouteRegistrar,
    context: Context,
    intentEvent: SharedFlow<Intent>,
) {
    val app = context.applicationContext as MainApplication
    val navController = rememberNavController()
    var hasOnboarded by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        hasOnboarded = OnboardingDatastore(
            context = context
        ).hasOnboarded()
    }

    val didRegister = remember { mutableStateOf(false) }

    val tabSessionManager = app.tabSessionManager
    val sessions = app.database.sessionDao()

    LaunchedEffect(hasOnboarded) {
        if (hasOnboarded == null) return@LaunchedEffect

        intentEvent.collect { intent ->
            val destination = intent.getStringExtra("destination")
            val sessionId = intent.getStringExtra("tab_id")

            if (destination == "browser") {
                Log.d("BROWSER", "session id: $sessionId, active session id: ${tabSessionManager.selectedTab.value?.id}")
                
                val isFromNotification = sessionId != null
                val isSameTab = tabSessionManager.selectedTab.value?.id == sessionId
                
                if (!isSameTab && sessionId != null) {
                    tabSessionManager.openTab(
                        BrowserTab(
                            id = sessionId,
                            url = intent.dataString ?: ""
                        )
                    )
                }

                navController.navigate("browser") {
                    launchSingleTop = true
                }
                
                // Sync media state jika kembali dari notification dengan tab yang sama
                // Ini diperlukan karena GeckoView tidak fire callback jika state tidak berubah
                if (isFromNotification && isSameTab) {
                    tabSessionManager.syncMediaStateFromNotification(sessionId)
                }
            }
        }
    }

    if (!didRegister.value) {
        registerDashboardSessionShell(
            routeRegistrar = routeRegistrar,
            tabSessionManager = tabSessionManager,
            sessions = sessions.getAll(),
            removeSession = sessions::delete,
        )
        registerOnboardingScreen(routeRegistrar)
        registerBrowserShell(
            routeRegistrar = routeRegistrar,
            session = tabSessionManager.selectedTab,
            tabSessionManager = tabSessionManager
        )
        didRegister.value = true
    }

    if (hasOnboarded == null) return

    SystemBarScaffold {
        AppNavHost(
            navController,
            routeRegistrar,
            startDestination = if (hasOnboarded == true) "dashboard-session" else "onboarding"
        )
    }
}