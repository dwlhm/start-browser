package com.dwlhm.startbrowser.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.dwlhm.datastore.preferences.OnboardingPrefs
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
    
    var isLoading by remember { mutableStateOf(true) }
    var hasOnboarded by remember { mutableStateOf(false) }
    
    // Check onboarding status
    LaunchedEffect(Unit) {
        hasOnboarded = OnboardingPrefs.hasOnboarded(context)
        isLoading = false
    }
    
    // Register all screens
    registerHomeScreen(routeRegistrar)
    registerOnboardingScreen(routeRegistrar)

    // SystemBarScaffold otomatis handle:
    // - Background naik ke area status bar (edge-to-edge)
    // - Warna icon status bar auto-detect dari luminance background
    // - Padding konten supaya tidak nabrak status bar
    SystemBarScaffold {
        AppNavHost(
            navController,
            routeRegistrar,
            startDestination = if (hasOnboarded) "home" else "onboarding"
        )
    }
}