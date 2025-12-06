package com.dwlhm.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
fun HomeScreen(
    viewModel: HomeViewModel,
    onSearchClick: (uri: String) -> Unit = {}
) {
    val homeState by viewModel.homeState.collectAsState()
    var inputUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
    ) {
        BasicText(
            text = "Start Browser",
            style = TextStyle(
                color = Color.Black,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        BasicText(
            text = "Welcome, ${homeState?.name ?: "User"}",
            style = TextStyle(
                color = Color.Gray,
                fontSize = 16.sp
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        InputUri(
            url = inputUrl,
            onUrlChange = { inputUrl = it },
            onUrlSubmit = { url ->
                onSearchClick(url)
            },
            onFocusChanged = {},
            backgroundColor = Color(0xFFF0F0F0)
        )
    }
}