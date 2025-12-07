package com.dwlhm.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.domain.browser.LastVisitedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class LastVisitedUiState(
    val url: String = "",
    val title: String = "",
    val hasLastVisited: Boolean = false
)

@HiltViewModel
class LastVisitedViewModel @Inject constructor(
    lastVisitedRepository: LastVisitedRepository
) : ViewModel() {
    
    val uiState: StateFlow<LastVisitedUiState> = lastVisitedRepository.getLastVisited()
        .map { data ->
            if (data.url.isNotEmpty()) {
                LastVisitedUiState(
                    url = data.url,
                    title = data.title.ifEmpty { 
                        // Fallback to formatted URL if title is empty
                        data.url.removePrefix("https://").removePrefix("http://").removeSuffix("/")
                    },
                    hasLastVisited = true
                )
            } else {
                LastVisitedUiState()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LastVisitedUiState()
        )
}