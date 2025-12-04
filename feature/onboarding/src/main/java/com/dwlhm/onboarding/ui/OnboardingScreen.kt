package com.dwlhm.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val onboardingState by viewModel.onboardingState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText(
                text = "Let's",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
//                fontFamily = EuphoriaScript,
//                fontStyle = FontStyle.Italic,
//                fontSize = 18.sp,
//                color = Color.Black,
//                modifier = Modifier
//                    .align(Alignment.CenterHorizontally),
            )
            Row {
                BasicText(
                    text = "Start",
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
//                    fontFamily = InterScript,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                    fontSize = 24.sp
                )
                BasicText(
                    text = " the journey",
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 24.sp,
                    )
//                    fontFamily = InterScript,
//                    fontSize = 24.sp,
//                    color = Color.Black,
                )
            }
        }
    }
}