package com.dwlhm.browser

import kotlinx.coroutines.flow.StateFlow

interface BrowserSession {
    val activeSessionId: String
    val activeUrl: StateFlow<String?>
    val activeTitle: StateFlow<String?>
    val canGoBack: StateFlow<Boolean>
    val canGoForward: StateFlow<Boolean>

    val hasActiveMedia: Boolean

    fun attachToView(view: Any)
    fun detachFromView()
    fun loadUrl(url: String)
    fun reload()
    fun stop()
    fun goBack(): Boolean
    fun goForward(): Boolean
    fun destroy()
    fun suspendSession(keepActive: Boolean = false)
    fun setActive(state: Boolean)
    fun setFocused(state: Boolean)
}