package com.dwlhm.startbrowser.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.dwlhm.datastore.preferences.OnboardingPrefs
import com.dwlhm.browser.api.registerBrowserScreen
import com.dwlhm.home.api.registerHomeScreen
import com.dwlhm.navigation.api.AppNavHost
import com.dwlhm.navigation.api.RouteRegistrar
import com.dwlhm.onboarding.api.registerOnboardingScreen

@Composable
fun AppRoot(
    routeRegistrar: RouteRegistrar,
) {
    val colors = AppTheme.colors

    AppTheme(colors) {
        MainScreen(routeRegistrar)
    }
}

@Composable
fun MainScreen(
    routeRegistrar: RouteRegistrar,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    var hasOnboarded by remember { mutableStateOf(false) }
    
    // Check onboarding status
    LaunchedEffect(Unit) {
        hasOnboarded = OnboardingPrefs.hasOnboarded(context)
    }
    
    // Register all screens
    registerHomeScreen(routeRegistrar)
    registerOnboardingScreen(routeRegistrar)
    registerBrowserScreen(routeRegistrar)

    SystemBarScaffold {
        AppNavHost(
            navController,
            routeRegistrar,
            startDestination = if (hasOnboarded) "home" else "onboarding"
        )
    }
}