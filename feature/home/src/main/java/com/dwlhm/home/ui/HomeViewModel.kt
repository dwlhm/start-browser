package com.dwlhm.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.home.api.Home
import com.dwlhm.home.internal.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {
    private val _homeState = MutableStateFlow<Home?>(null)
    val homeState: StateFlow<Home?> = _homeState

    fun loadHome(userId: String) {
        viewModelScope.launch {
            repository.getUserName(userId).collect {
                _homeState.value = it
            }
        }
    }
}