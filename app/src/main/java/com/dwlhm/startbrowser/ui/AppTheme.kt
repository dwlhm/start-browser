package com.dwlhm.startbrowser.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color = Color.White,
    val statusBarIconDark: Boolean = false
)

private val LocalAppColors = staticCompositionLocalOf { AppColors() }

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

@Composable
fun AppTheme(
    colors: AppColors = AppTheme.colors,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppColors provides colors,
        content = content,
    )
}