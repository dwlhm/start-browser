package com.dwlhm.browser.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.dwlhm.ui.input.InputUri

/**
 * Default toolbar background color
 */
private val DefaultToolbarColor = Color(0xFFF5F5F5)

@Composable
fun BrowserToolbar(
    url: String,
    onUrlChange: (String) -> Unit,
    onUrlSubmit: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    themeColor: Color? = null
) {
    // Animate color transition smoothly
    val backgroundColor by animateColorAsState(
        targetValue = themeColor ?: DefaultToolbarColor,
        animationSpec = tween(durationMillis = 300),
        label = "toolbar_background"
    )
    
    // Determine input field background based on toolbar luminance
    // Use lighter input for dark backgrounds, darker input for light backgrounds
    val inputBackgroundColor = if (backgroundColor.luminance() > 0.5f) {
        Color.White
    } else {
        Color.White.copy(alpha = 0.2f)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(top = 8.dp, bottom = 16.dp, start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InputUri(
            url = url,
            modifier = Modifier.weight(1f),
            backgroundColor = inputBackgroundColor,
            onUrlChange = onUrlChange,
            onUrlSubmit = onUrlSubmit,
            onFocusChanged = onFocusChanged
        )
    }
}