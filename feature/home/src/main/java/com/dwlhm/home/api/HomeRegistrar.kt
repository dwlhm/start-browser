package com.dwlhm.home.api

import com.dwlhm.home.internal.HomeRepository
import com.dwlhm.home.ui.HomeScreen
import com.dwlhm.home.ui.HomeViewModel
import com.dwlhm.navigation.api.RouteRegistrar

fun registerHomeScreen(routeRegistrar: RouteRegistrar) {
    routeRegistrar.register(
        route = "home",
        content = {
            val viewModel = HomeViewModel(HomeRepository())
            HomeScreen(viewModel)
        }
    )
}