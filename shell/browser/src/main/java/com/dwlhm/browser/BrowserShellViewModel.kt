package com.dwlhm.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.browser.api.BrowserUiState
import com.dwlhm.event.EventDispatcher
import com.dwlhm.event.MediaActivatedEvent
import com.dwlhm.event.MediaDeactivatedEvent
import com.dwlhm.event.MediaMetadataChangedEvent
import com.dwlhm.event.MediaStateChangedEvent
import com.dwlhm.event.TabInfoChangedEvent
import com.dwlhm.utils.normalizeUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserShellViewModel(
    private val browserSession: BrowserSession,
    private val sessionId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private var mediaSession: BrowserMediaSession? = null

    init {
        observeBrowserSession()
    }

    private fun observeBrowserSession() {
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

        // pasang session callback
        browserSession.sessionCallback = object : BrowserSessionCallback {

            override fun onTabInfoChanged(title: String, url: String) {
                EventDispatcher.dispatch(TabInfoChangedEvent(sessionId, url, title))
            }

            override fun onMediaActivated(mediaSession: BrowserMediaSession) {
                this@BrowserShellViewModel.mediaSession = mediaSession
                EventDispatcher.dispatch(MediaActivatedEvent(sessionId, mediaSession))
            }

            override fun onMediaDeactivated() {
                mediaSession = null
                EventDispatcher.dispatch(MediaDeactivatedEvent(sessionId))
            }

            override fun onMediaMetadataChanged(mediaMetadata: BrowserMediaMetadata) {
                val session = mediaSession ?: return
                EventDispatcher.dispatch(
                    MediaMetadataChangedEvent(sessionId, session, mediaMetadata)
                )
            }

            override fun onMediaStateChanged(state: BrowserMediaState) {
                val session = mediaSession ?: return
                EventDispatcher.dispatch(
                    MediaStateChangedEvent(sessionId, session, state)
                )
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
