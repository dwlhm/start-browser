package com.dwlhm.browser.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    themeColor: Color? = null,
    tabCount: Int = 1,
    onTabButtonClick: () -> Unit = {}
) {
    // Animate color transition smoothly
    val backgroundColor by animateColorAsState(
        targetValue = themeColor ?: DefaultToolbarColor,
        animationSpec = tween(durationMillis = 300),
        label = "toolbar_background"
    )
    
    // Determine input field background based on toolbar luminance
    // Use lighter input for dark backgrounds, darker input for light backgrounds
    val isLightBackground = backgroundColor.luminance() > 0.5f
    val inputBackgroundColor = if (isLightBackground) {
        Color.White
    } else {
        Color.White.copy(alpha = 0.2f)
    }
    
    val tabButtonTextColor = if (isLightBackground) Color.DarkGray else Color.White
    val tabButtonBorderColor = if (isLightBackground) Color.DarkGray else Color.White
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(top = 8.dp, bottom = 16.dp, start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // URL Input field
        InputUri(
            url = url,
            modifier = Modifier.weight(1f),
            backgroundColor = inputBackgroundColor,
            onUrlChange = onUrlChange,
            onUrlSubmit = onUrlSubmit,
            onFocusChanged = onFocusChanged
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Tab button showing tab count
        TabCountButton(
            count = tabCount,
            textColor = tabButtonTextColor,
            borderColor = tabButtonBorderColor,
            onClick = onTabButtonClick
        )
    }
}

/**
 * Button showing current tab count
 */
@Composable
private fun TabCountButton(
    count: Int,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            fontSize = if (count > 99) 10.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}