package com.dwlhm.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InputUri(
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onEditingChanged: ((Boolean) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    // Notify parent ONLY with semantic intent
    LaunchedEffect(isFocused) {
        onEditingChanged?.invoke(isFocused)
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .onFocusChanged {
                isFocused = it.isFocused
            }
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isFocused) Color(0xFF4285F4) else Color(0xFFCCCCCC)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = Color.Black
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onSubmit(value)
            }
        ),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    BasicText(
                        text = "Search or enter address",
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}