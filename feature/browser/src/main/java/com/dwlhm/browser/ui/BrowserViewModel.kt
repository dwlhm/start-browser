package com.dwlhm.browser.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.datastore.preferences.lastvisited.LastVisitedRepository
import com.dwlhm.tabmanager.api.SessionNavigator
import com.dwlhm.webview.WebViewSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val navigator: SessionNavigator,
    private val lastVisitedRepository: LastVisitedRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState = _uiState.asStateFlow()

    val activeSession: StateFlow<WebViewSession?> =
        navigator.activeSession

    init {
        observeNavigator()
    }

    private fun observeNavigator() {
        // URL
        viewModelScope.launch {
            navigator.currentUrl.collect { url ->
                _uiState.update {
                    it.copy(inputUrl = url ?: "")
                }
            }
        }

        // Forward State
        viewModelScope.launch {
            navigator.canGoForward.collect { canGoForward ->
                _uiState.update {
                    it.copy(canGoForward = canGoForward)
                }
            }
        }

        // Title State
        viewModelScope.launch {
            navigator.currentTitle.collect { title ->
                if (title != null) {
                    lastVisitedRepository.saveLastVisited(
                        url = _uiState.value.inputUrl,
                        title = title
                    )
                }
            }
        }

        viewModelScope.launch {
            navigator.activeSession.collect { session ->
                _uiState.update {
                    it.copy(hasSession = session != null)
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
        navigator.activeSession.value?.loadUrl(url)

        _uiState.update {
            it.copy(inputUrl = url)
        }
    }

    fun onBackPressed(): Boolean {
        return navigator.goBack()
    }

    fun onForwardPressed(): Boolean {
        return navigator.goForward()
    }

    /* =========================
       Init
       ========================= */

    fun init(initialUrl: String?) {
        if (initialUrl == null) {
            return
        }
        navigator.openInNewTab(initialUrl)
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