package com.dwlhm.startbrowser.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.dwlhm.home.api.registerHomeScreen
import com.dwlhm.navigation.api.AppNavHost
import com.dwlhm.navigation.api.RouteRegistrar

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

    registerHomeScreen(routeRegistrar)

    // SystemBarScaffold otomatis handle:
    // - Background naik ke area status bar (edge-to-edge)
    // - Warna icon status bar auto-detect dari luminance background
    // - Padding konten supaya tidak nabrak status bar
    SystemBarScaffold {
        AppNavHost(
            navController,
            routeRegistrar,
            startDestination = "home"
        )
    }
}