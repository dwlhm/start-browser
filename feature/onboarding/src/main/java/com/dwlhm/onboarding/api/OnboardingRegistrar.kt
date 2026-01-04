package com.dwlhm.onboarding.api

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.dwlhm.navigation.api.RouteRegistrar
import com.dwlhm.onboarding.ui.OnboardingScreen
import com.dwlhm.onboarding.ui.OnboardingViewModel

fun registerOnboardingScreen(routeRegistrar: RouteRegistrar) {
    routeRegistrar.register(
        route = "onboarding",
        content = { navController, _ ->
            val viewModel: OnboardingViewModel = hiltViewModel()

            LaunchedEffect(Unit) {
                viewModel.navigateToHome.collect {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            }
            
            OnboardingScreen(
                onFinish = viewModel::onFinish
            )
        }
    )
}