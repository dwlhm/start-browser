package com.dwlhm.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AnimatedRotatingTextViewModel @Inject constructor(
    val messageList: List<String>,
    val animationDuration: Long = 3000L,
    val onFinish: () -> Unit = {}
) : ViewModel() {
    val index = MutableStateFlow(0)

    init {
        startAnimation()
    }

     private fun startAnimation() {
        viewModelScope.launch {
            while (index.value < messageList.lastIndex) {
                delay(animationDuration)
                index.value++
            }

            // final step
            delay(animationDuration)
            onFinish()
        }
    }
}