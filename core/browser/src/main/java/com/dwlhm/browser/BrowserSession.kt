package com.dwlhm.browser

import kotlinx.coroutines.flow.StateFlow

interface BrowserSession {
    val activeUrl: StateFlow<String?>
    val activeTitle: StateFlow<String?>
    val canGoBack: StateFlow<Boolean>
    val canGoForward: StateFlow<Boolean>

    var sessionCallback: BrowserSessionCallback?
    fun setCallback(callback: BrowserSessionCallback)

    fun attachToView(view: Any)
    fun detachFromView()
    fun loadUrl(url: String)
    fun reload()
    fun stop()
    fun goBack(): Boolean
    fun goForward(): Boolean
    fun destroy()
}