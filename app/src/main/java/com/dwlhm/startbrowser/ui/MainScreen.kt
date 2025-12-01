package com.dwlhm.startbrowser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        SetStatusBarStyle(
            colors.statusBarIconDark,
            background = colors.background
        )

        MainScreen(routeRegistrar)
    }
}

@Composable
fun MainScreen(
    routeRegistrar: RouteRegistrar,
) {
    val navController = rememberNavController()

    registerHomeScreen(routeRegistrar)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .statusBarsPadding()
    ) {

        Box(
            modifier = Modifier
                .background(AppTheme.colors.background)
        )

        AppNavHost(
            navController,
            routeRegistrar,
            startDestination = "home"
        )
    }

}