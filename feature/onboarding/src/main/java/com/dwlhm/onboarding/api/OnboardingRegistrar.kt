package com.dwlhm.onboarding.api

import com.dwlhm.navigation.api.RouteRegistrar
import com.dwlhm.onboarding.ui.OnboardingScreen

fun registerOnboardingScreen(routeRegistrar: RouteRegistrar) {
    routeRegistrar.register(
        route = "onboarding",
        content = {
            OnboardingScreen()
        }
    )
}