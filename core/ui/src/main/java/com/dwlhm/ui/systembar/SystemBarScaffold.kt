package com.dwlhm.ui.systembar

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Configures status bar color and icon appearance.
 *
 * @param color Status bar background color
 * @param darkIcons Icon color: true = dark, false = light, null = auto-detect
 */
@Composable
fun SetStatusBar(
    color: Color,
    darkIcons: Boolean? = null
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val window = (view.context as? Activity)?.window ?: return
    val useDarkIcons = darkIcons ?: (color.luminance() > 0.5f)

    SideEffect {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = color.toArgb()
        }

        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = useDarkIcons
    }
}

/**
 * Edge-to-edge layout with integrated status bar styling.
 * Content is automatically padded to avoid system bar overlap.
 *
 * @param backgroundColor Background color extending into status bar area
 * @param darkStatusBarIcons Icon color: true = dark, false = light, null = auto-detect
 * @param content Screen content with automatic inset padding
 */
@Composable
fun SystemBarScaffold(
    backgroundColor: Color,
    darkStatusBarIcons: Boolean? = null,
    content: @Composable BoxScope.() -> Unit
) {
    SetStatusBar(backgroundColor, darkStatusBarIcons)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            content()
        }
    }
}