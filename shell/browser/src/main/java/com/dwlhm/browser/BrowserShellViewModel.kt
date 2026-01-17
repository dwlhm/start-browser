package com.dwlhm.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.browser.api.BrowserUiState
import com.dwlhm.utils.normalizeUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserShellViewModel(
    private val browserSession: BrowserSession,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()


    init {
        observeBrowserSession()
    }

     fun observeBrowserSession() {
        // observe activeUrl
        viewModelScope.launch {
            browserSession.activeUrl.collect { url ->
                _uiState.update { it.copy(inputUrl = url ?: "") }
            }
        }

        // observe canGoForward
        viewModelScope.launch {
            browserSession.canGoForward.collect { canGoForward ->
                _uiState.update { it.copy(canGoForward = canGoForward) }
            }
        }
    }

    fun onUrlSubmit(inputUrl: String) {
        browserSession.loadUrl(normalizeUrl(inputUrl))
    }

    fun onUrlChange(newValue: String) {
        _uiState.update { it.copy(inputUrl = newValue) }
    }

    fun goBack(): Boolean = browserSession.goBack()

    fun goForward(): Boolean {
        browserSession.goForward()
        return true
    }
}
