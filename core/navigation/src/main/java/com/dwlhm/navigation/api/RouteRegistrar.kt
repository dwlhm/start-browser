package com.dwlhm.navigation.api

import androidx.compose.runtime.Composable

interface RouteRegistrar {
    fun register(
        route: String,
        content: @Composable () -> Unit
    )
    fun getAllRoutes(): Map<String, @Composable () -> Unit>
}

class RouteRegistrarImpl : RouteRegistrar {
    private val routes = mutableMapOf<String, @Composable () -> Unit>()

    override fun register(
        route: String,
        content: @Composable () -> Unit
    ) {
        routes[route] = content
    }

    override fun getAllRoutes(): Map<String, @Composable () -> Unit> {
        return routes
    }
}