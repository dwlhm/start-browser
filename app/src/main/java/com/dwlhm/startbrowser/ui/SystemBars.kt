package com.dwlhm.startbrowser.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SetStatusBarStyle(
    darkIcons: Boolean,
    background: Color
) {
    val view = LocalView.current
    val window = (view.context as Activity).window

    SideEffect {
        // Chrome-like translucent tint
        window.statusBarColor = background.copy(alpha = 0.25f).toArgb()

        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = darkIcons
    }
}
