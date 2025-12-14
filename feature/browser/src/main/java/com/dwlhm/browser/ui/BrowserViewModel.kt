package com.dwlhm.browser.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.webview.WebViewSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val tabManager: BrowserTabManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeActiveSession()
    }

    val activeSession: StateFlow<WebViewSession?> =
        tabManager.activeSession

    private fun observeActiveSession() {
        viewModelScope.launch {
            tabManager.activeSession.collect { session ->
                _uiState.update {
                    it.copy(hasSession = session != null)
                }

                session?.let { observeSessionUrl(it) }
            }
        }
    }

    private var urlJob: Job? = null

    private fun observeSessionUrl(session: WebViewSession) {
        urlJob?.cancel()
        urlJob = viewModelScope.launch {
            session.currentUrl.collect { url ->
                _uiState.update {
                    it.copy(inputUrl = url ?: "")
                }
            }

        }
    }

    /* =========================
       UI intents
       ========================= */

    fun onUrlChange(value: String) {
        _uiState.update {
            it.copy(inputUrl = value)
        }
    }

    fun onUrlSubmit(url: String) {
        val url = normalizeUrl(_uiState.value.inputUrl)
        tabManager.activeSession.value?.loadUrl(url)

        _uiState.update {
            it.copy(inputUrl = url)
        }
    }

    fun onBackPressed(): Boolean {
        return tabManager.handleBack()
    }

    fun onForwardPressed(): Boolean {
        return tabManager.handleForward()
    }

    /* =========================
       Init
       ========================= */

    fun init(initialUrl: String) {
        if (tabManager.activeSession.value == null) {
            tabManager.addTab(initialUrl)
        }
    }

    private fun normalizeUrl(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.startsWith("http") -> trimmed
            trimmed.contains(" ") ->
                "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
            else -> "https://$trimmed"
        }
    }
}