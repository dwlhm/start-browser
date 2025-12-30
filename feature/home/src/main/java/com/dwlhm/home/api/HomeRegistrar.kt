package com.dwlhm.home.api

import androidx.hilt.navigation.compose.hiltViewModel
import com.dwlhm.home.ui.HomeScreen
import com.dwlhm.home.ui.HomeViewModel
import com.dwlhm.navigation.api.RouteRegistrar
import java.net.URLEncoder

fun registerHomeScreen(routeRegistrar: RouteRegistrar) {
    routeRegistrar.register(
        route = "home",
        content = { navController, _ ->
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onSearchClick = { uri ->
                    val encodedUrl = URLEncoder.encode(uri, "UTF-8")
                    navController.navigate("browser?url=$encodedUrl")
                },
                onOpenTab = {
                    navController.navigate("browser")
                }
            )
        }
    )
}