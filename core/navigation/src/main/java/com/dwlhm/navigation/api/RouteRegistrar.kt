package com.dwlhm.navigation.api

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

interface RouteRegistrar {
    fun register(
        route: String,
        content: @Composable (NavHostController, NavBackStackEntry) -> Unit
    )
    fun getAllRoutes(): Map<String, @Composable (NavHostController, NavBackStackEntry) -> Unit>
}

class RouteRegistrarImpl : RouteRegistrar {
    private val routes = mutableMapOf<String, @Composable (NavHostController, NavBackStackEntry) -> Unit>()

    override fun register(
        route: String,
        content: @Composable (NavHostController, NavBackStackEntry) -> Unit
    ) {
        routes[route] = content
    }

    override fun getAllRoutes(): Map<String, @Composable (NavHostController, NavBackStackEntry) -> Unit> {
        return routes
    }
}