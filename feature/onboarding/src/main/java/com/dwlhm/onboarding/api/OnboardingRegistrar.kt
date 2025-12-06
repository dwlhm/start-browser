package com.dwlhm.onboarding.api

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.dwlhm.datastore.preferences.OnboardingPrefs
import com.dwlhm.navigation.api.RouteRegistrar
import com.dwlhm.onboarding.ui.OnboardingScreen
import kotlinx.coroutines.launch

fun registerOnboardingScreen(routeRegistrar: RouteRegistrar) {
    routeRegistrar.register(
        route = "onboarding",
        content = { navController ->
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            
            OnboardingScreen(
                onFinish = {
                    scope.launch {
                        OnboardingPrefs.setOnboarded(context)
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }
            )
        }
    )
}