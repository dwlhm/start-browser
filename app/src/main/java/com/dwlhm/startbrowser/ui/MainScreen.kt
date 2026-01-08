package com.dwlhm.startbrowser.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.dwlhm.browser.BrowserRuntime
import com.dwlhm.browser.api.BrowserRuntimeController
import com.dwlhm.browser.registerBrowserShell
import com.dwlhm.data.datastore.onboarding.OnboardingDatastore
import com.dwlhm.gecko.api.GeckoBrowserRuntime
import com.dwlhm.home.api.registerHomeScreen
import com.dwlhm.navigation.api.AppNavHost
import com.dwlhm.navigation.api.RouteRegistrar
import com.dwlhm.onboarding.api.registerOnboardingScreen
import com.dwlhm.startbrowser.MainApplication

@Composable
fun AppRoot(
    routeRegistrar: RouteRegistrar,
    context: Context
) {
    AppTheme(AppTheme.colors) {
        MainScreen(routeRegistrar, context)
    }
}

@Composable
fun MainScreen(
    routeRegistrar: RouteRegistrar,
    context: Context,
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

    // Only register routes in main process where these are initialized
    val tabManager = app.tabManager
    val viewHost = app.browserViewHost

    if (!didRegister.value && tabManager != null && viewHost != null) {
        registerHomeScreen(routeRegistrar)
        registerOnboardingScreen(routeRegistrar)
        registerBrowserShell(
            routeRegistrar = routeRegistrar,
            tabManager = tabManager,
            viewHost = viewHost,
        )
        didRegister.value = true
    }

    if (hasOnboarded == null) return

    SystemBarScaffold {
        AppNavHost(
            navController,
            routeRegistrar,
            startDestination = if (hasOnboarded == true) "home" else "onboarding"
        )
    }
}