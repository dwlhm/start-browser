package com.dwlhm.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.onboarding.api.Onboarding
import com.dwlhm.onboarding.internal.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OnboardingViewModel @Inject constructor(): ViewModel() {
    private val _onboardingState = MutableStateFlow(Onboarding(hasOnboarded = false))

    val onboardingState: StateFlow<Onboarding> = _onboardingState
}