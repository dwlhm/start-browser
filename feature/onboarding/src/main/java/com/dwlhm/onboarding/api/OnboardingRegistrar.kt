package com.dwlhm.onboarding.api

import com.dwlhm.navigation.api.RouteRegistrar
import com.dwlhm.onboarding.ui.OnboardingScreen
import com.dwlhm.onboarding.ui.OnboardingViewModel

fun registerOnboardingScreen(routeRegistrar: RouteRegistrar) {
    routeRegistrar.register(
        route = "onboarding",
        content = {
            OnboardingScreen()
        }
    )
}