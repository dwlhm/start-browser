package com.dwlhm.startbrowser.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.dwlhm.ui.systembar.SystemBarScaffold


/**
 * Edge-to-edge layout using current [AppTheme] background color.
 *
 * @param content Screen content with automatic inset padding
 */
@Composable
fun SystemBarScaffold(
    content: @Composable BoxScope.() -> Unit
) {
    val colors = AppTheme.colors
    SystemBarScaffold (
        backgroundColor = colors.background,
        darkStatusBarIcons = null,
        content = content
    )
}
