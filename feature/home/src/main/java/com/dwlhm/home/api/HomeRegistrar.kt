package com.dwlhm.home.api

import androidx.hilt.navigation.compose.hiltViewModel
import com.dwlhm.home.ui.HomeScreen
import com.dwlhm.home.ui.HomeViewModel
import com.dwlhm.navigation.api.RouteRegistrar

fun registerHomeScreen(routeRegistrar: RouteRegistrar) {
    routeRegistrar.register(
        route = "home",
        content = { navController ->
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onSearchClick = { navController.navigate("browser") }
            )
        }
    )
}