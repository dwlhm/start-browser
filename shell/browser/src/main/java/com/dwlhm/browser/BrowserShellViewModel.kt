package com.dwlhm.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.browser.api.BrowserUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserShellViewModel(
    private val tabManager: TabManager,
): ViewModel() {
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState = _uiState.asStateFlow()
    private var _browserSession: BrowserSession? = null
    private var _observeJob: Job? = null

    fun onShellReady() {
        val session = tabManager.acquire()
        _browserSession = session

        _observeJob?.cancel()
        _observeJob = observeBrowserSession(session)
    }

    fun onShellGone() {
        _observeJob?.cancel()
        _observeJob = null
        _browserSession = null
    }

    fun init(initialUrl: String?) {
        if (initialUrl != null) {
            _browserSession?.loadUrl(initialUrl)
        }
    }

    fun onUrlSubmit(inputUrl: String) {
        val url = normalizeUrl(inputUrl)
        _browserSession?.loadUrl(url)
    }

    fun onUrlChange(newValue: String) {
        _uiState.update { ui ->
            ui.copy(inputUrl = newValue)
        }
    }

    fun goBack(): Boolean {
        val session = _browserSession ?: return false
        session.goBack()
        return true
    }

    fun goForward(): Boolean {
        val session = _browserSession ?: return false
        session.goForward()
        return true
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

    private fun observeBrowserSession(session: BrowserSession): Job {
        return viewModelScope.launch {
            launch {
                session.activeUrl.collect { url ->
                    _uiState.update {
                        it.copy(inputUrl = url ?: "")
                    }
                }
            }

            launch {
                session.canGoForward.collect { canGoForward ->
                    _uiState.update {
                        it.copy(canGoForward = canGoForward)
                    }
                }
            }
        }
    }
}