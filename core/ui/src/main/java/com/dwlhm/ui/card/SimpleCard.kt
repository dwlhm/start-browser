package com.dwlhm.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimpleCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF0F0F0),
    titleModifier: Modifier = Modifier,
    descriptionModifier: Modifier = Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        BasicText(
            text = title,
            style = TextStyle(
                fontSize = 16.sp,
                color = Color.Black
            ),
            modifier = titleModifier
                .padding(bottom = 4.dp)
        )
        BasicText(
            text = description,
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = .8f)
            ),
            modifier = descriptionModifier
        )
    }
}