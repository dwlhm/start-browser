package com.dwlhm.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.data.datastore.onboarding.OnboardingDatastore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingDatastore: OnboardingDatastore
) : ViewModel() {

    private val _navigateToHome = Channel<Unit>()
    val navigateToHome = _navigateToHome.receiveAsFlow()

    fun onFinish() {
        viewModelScope.launch {
            onboardingDatastore.setOnboarded()
            _navigateToHome.send(Unit)
        }
    }
}