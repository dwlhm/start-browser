package com.dwlhm.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSearchClick: () -> Unit = {}
) {
    val homeState by viewModel.homeState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        BasicText(
            text = "Start Browser",
            style = TextStyle(
                color = Color.Black,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Welcome message
        BasicText(
            text = "Welcome, ${homeState?.name ?: "User"}",
            style = TextStyle(
                color = Color.Gray,
                fontSize = 16.sp
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Search Bar / URL Bar - callback to parent for navigation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Color(0xFFF0F0F0))
                .clickable { onSearchClick() }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicText(
                text = "Search or enter URL",
                style = TextStyle(
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Hint text
        BasicText(
            text = "Tap to start browsing",
            style = TextStyle(
                color = Color.LightGray,
                fontSize = 12.sp
            )
        )
    }
}