package com.dwlhm.navigation.api

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

interface RouteRegistrar {
    fun register(
        route: String,
        content: @Composable (NavHostController) -> Unit
    )
    fun getAllRoutes(): Map<String, @Composable (NavHostController) -> Unit>
}

class RouteRegistrarImpl : RouteRegistrar {
    private val routes = mutableMapOf<String, @Composable (NavHostController) -> Unit>()

    override fun register(
        route: String,
        content: @Composable (NavHostController) -> Unit
    ) {
        routes[route] = content
    }

    override fun getAllRoutes(): Map<String, @Composable (NavHostController) -> Unit> {
        return routes
    }
}