package com.dwlhm.ui.systembar

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Composable to dynamically update system bar color and icon appearance
 */
@Composable
fun DynamicSystemBar(color: Color) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val window = (view.context as? Activity)?.window ?: return
    val useDarkIcons = color.luminance() > 0.5f

    SideEffect {
        // Update status bar color on pre-API 35 devices
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = color.toArgb()
        }

        // Update status bar icon colors
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = useDarkIcons
    }
}