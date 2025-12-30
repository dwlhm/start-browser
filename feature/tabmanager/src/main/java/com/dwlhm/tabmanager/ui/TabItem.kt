package com.dwlhm.tabmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun TabItem(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) Color(0xFFF0F0F0) else Color(0xFF1E1E1E)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicText(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
            style = TextStyle(
                color = if (isActive) Color.Black else Color.White
            )
        )

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                text = "x",
                style = TextStyle(
                    color = Color.White
                )
            )
        }
    }
}
