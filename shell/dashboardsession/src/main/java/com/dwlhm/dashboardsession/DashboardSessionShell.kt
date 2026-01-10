package com.dwlhm.dashboardsession

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwlhm.ui.input.InputUri

@Composable
fun DashboardSessionShell(
    onValueChange: (String) -> Unit
) {
    var inputUrl by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        BasicText(
            text = "Start Browser",
            style = TextStyle(
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
            )
        )

        Spacer(Modifier.height(24.dp))

        InputUri(
            value = inputUrl,
            modifier = Modifier.fillMaxWidth(),
            rounded = 8.dp,
            backgroundColor = Color(0xFFF0F0F0),
            onValueChange = {
                inputUrl = it
            },
            onSubmit = { it: String ->
                onValueChange(it)
            }
        )
    }
}