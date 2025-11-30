package com.dwlhm.navigation.api

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavHost(
    navController: NavHostController,
    routeRegistrar: RouteRegistrar,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        routeRegistrar.getAllRoutes().forEach { (route, content) ->
            composable(route) { content() }
        }
    }
}
