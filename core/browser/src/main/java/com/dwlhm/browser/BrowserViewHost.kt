package com.dwlhm.browser

interface BrowserViewHost {
    fun attach(view: Any)
    fun detach()
}