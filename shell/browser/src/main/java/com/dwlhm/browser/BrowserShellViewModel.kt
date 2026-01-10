package com.dwlhm.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.browser.api.BrowserUiState
import com.dwlhm.utils.normalizeUrl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserShellViewModel(
    private val browserSession: BrowserSession,
): ViewModel() {
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState = _uiState.asStateFlow()
    private var _observeJob: Job? = null

    init {
        _observeJob = observeBrowserSession()
    }

    override fun onCleared() {
        super.onCleared()
        _observeJob?.cancel()
    }

    fun loadInitialUrl(initialUrl: String?) {
        if (initialUrl != null) {
            browserSession.loadUrl(initialUrl)
        }
    }

    fun onUrlSubmit(inputUrl: String) {
        browserSession.loadUrl(normalizeUrl(inputUrl))
    }

    fun onUrlChange(newValue: String) {
        _uiState.update { ui ->
            ui.copy(inputUrl = newValue)
        }
    }

    fun goBack(): Boolean {
        return browserSession.goBack()
    }

    fun goForward(): Boolean {
        browserSession.goForward()
        return true
    }

    private fun observeBrowserSession(): Job {
        return viewModelScope.launch {
            launch {
                browserSession.activeUrl.collect { url ->
                    _uiState.update {
                        it.copy(inputUrl = url ?: "")
                    }
                }
            }

            launch {
                browserSession.canGoForward.collect { canGoForward ->
                    _uiState.update {
                        it.copy(canGoForward = canGoForward)
                    }
                }
            }
        }
    }
}