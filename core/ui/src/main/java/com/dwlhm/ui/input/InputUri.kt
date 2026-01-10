package com.dwlhm.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InputUri(
    value: String,
    modifier: Modifier = Modifier,
    rounded: Dp = 8.dp,
    backgroundColor: Color = Color.White,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onEditingChanged: ((Boolean) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val currentOnEditingChanged by rememberUpdatedState(onEditingChanged)

    // Notify parent ONLY when focus changes
    LaunchedEffect(isFocused) {
        currentOnEditingChanged?.invoke(isFocused)
    }

    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = Color.Black
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Uri
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onSubmit(value)
                focusManager.clearFocus()
            }
        ),
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .background(backgroundColor, RoundedCornerShape(rounded))
            .border(
                width = 1.dp,
                color = if (isFocused) Color(0xFF4285F4) else Color(0xFFCCCCCC),
                shape = RoundedCornerShape(rounded)
            )
            .padding(12.dp),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    BasicText(
                        text = "Search or enter address",
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}
