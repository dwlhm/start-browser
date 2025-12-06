package com.dwlhm.navigation.api

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun AppNavHost(
    navController: NavHostController,
    routeRegistrar: RouteRegistrar,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        routeRegistrar.getAllRoutes().forEach { (route, content) ->
            // Check if route has optional query parameter
            if (route.contains("?")) {
                val queryPart = route.substringAfter("?")
                val argName = queryPart.substringBefore("=").trim()
                
                composable(
                    route = route,
                    arguments = listOf(
                        navArgument(argName) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    content(navController, backStackEntry)
                }
            } else {
                composable(route) { backStackEntry ->
                    content(navController, backStackEntry)
                }
            }
        }
    }
}
