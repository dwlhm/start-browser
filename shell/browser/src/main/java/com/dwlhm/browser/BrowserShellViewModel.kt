package com.dwlhm.browser

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.browser.api.BrowserSessionController
import com.dwlhm.browser.api.BrowserUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class BrowserShellViewModel(
    session: BrowserSession,
): ViewModel() {
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState = _uiState.asStateFlow()
    val browserSession = BrowserSessionController(
        session = session,
    )

    init {
        observeBrowserSession()
    }

    fun init(initialUrl: String?) {
        if (initialUrl == null) {
            return
        }

        browserSession.loadUrl(initialUrl)
    }

    fun onUrlSubmit(inputUrl: String) {
        val url = normalizeUrl(inputUrl)
        browserSession.loadUrl(url)
    }

    fun onUrlChange(newValue: String) {
        _uiState.update { ui ->
            ui.copy(inputUrl = newValue)
        }
    }

    private fun normalizeUrl(inputUrl: String): String {
        val trimmed = inputUrl.trim()
        return when {
            trimmed.startsWith("http") -> trimmed
            trimmed.contains(" ") ->
                "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
            else -> "https://$trimmed"
        }
    }

    private fun observeBrowserSession() {
        viewModelScope.launch {
            browserSession.activeUrl.collect { url ->
                _uiState.update {
                    it.copy(inputUrl = url ?: "")
                }
            }
        }

        viewModelScope.launch {
            browserSession.canGoForward.collect { canGoForward ->
                _uiState.update {
                    it.copy(canGoForward = canGoForward)
                }
            }
        }
    }
}