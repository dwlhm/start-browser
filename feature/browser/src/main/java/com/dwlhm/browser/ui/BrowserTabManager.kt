package com.dwlhm.browser.ui

import com.dwlhm.webview.WebViewEngine
import com.dwlhm.webview.WebViewSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrowserTabManager @Inject constructor(
    private val engine: WebViewEngine
) {
    private val _tabs = mutableListOf<WebViewSession>()
    private val _activeSession = MutableStateFlow<WebViewSession?>(null)

    fun addTab(url: String) {
        val session = engine.createSession()   // engine-agnostic
        _tabs.add(session)
        _activeSession.value = session
        session.loadUrl(url)
    }

    val activeSession = _activeSession.asStateFlow()

    fun switchTab(index: Int) {
        if (index in _tabs.indices) {
            _activeSession.value = _tabs[index]
        }
    }

    fun closeTab(index: Int) {
        if (index in _tabs.indices) {
            val removed = _tabs.removeAt(index)
            removed.close()
            // set activeSession ke tab sebelumnya atau null
            _activeSession.value = _tabs.getOrNull(index - 1) ?: _tabs.firstOrNull()
        }
    }

}
