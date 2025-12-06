package com.dwlhm.browser.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class BrowserUiState(
    val currentUrl: String = "",
    val inputUrl: String = "",
    val isLoading: Boolean = false,
    val progress: Float = 0.2f,
    val pageTitle: String = "",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isUrlBarFocused: Boolean = false,
    val themeColor: Color? = null
)

@HiltViewModel
class BrowserViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()
    
    fun updateInputUrl(url: String) {
        _uiState.update { it.copy(inputUrl = url) }
    }
    
    fun updateCurrentUrl(url: String) {
        _uiState.update { it.copy(currentUrl = url, inputUrl = url) }
    }
    
    fun updateLoadingState(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }
    
    fun updateProgress(progress: Float) {
        _uiState.update { it.copy(progress = progress) }
    }
    
    fun updatePageTitle(title: String) {
        _uiState.update { it.copy(pageTitle = title) }
    }
    
    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { 
            it.copy(canGoBack = canGoBack, canGoForward = canGoForward) 
        }
    }
    
    fun setUrlBarFocused(focused: Boolean) {
        _uiState.update { it.copy(isUrlBarFocused = focused) }
    }
    
    fun updateThemeColor(color: Color?) {
        _uiState.update { it.copy(themeColor = color) }
    }
    
    fun submitUrl(url: String): String {
        val trimmedUrl = url.trim()
        val formattedUrl = when {
            trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://") -> trimmedUrl
            trimmedUrl.contains(".") && !trimmedUrl.contains(" ") -> "https://$trimmedUrl"
            else -> "https://www.google.com/search?q=${trimmedUrl.replace(" ", "+")}"
        }
        _uiState.update { it.copy(inputUrl = formattedUrl, isUrlBarFocused = false) }
        return formattedUrl
    }
}
