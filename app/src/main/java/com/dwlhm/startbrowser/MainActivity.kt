package com.dwlhm.startbrowser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.dwlhm.home.api.registerHomeScreen
import com.dwlhm.navigation.api.AppNavHost
import com.dwlhm.navigation.api.RouteRegistrar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var routeRegistrar: RouteRegistrar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(routeRegistrar)
        }
    }
}

@Composable
fun MainScreen(routeRegistrar: RouteRegistrar) {
    val navController = rememberNavController()

    registerHomeScreen(routeRegistrar)

    AppNavHost(
        navController = navController,
        routeRegistrar = routeRegistrar,
        startDestination = "home"
    )
}
