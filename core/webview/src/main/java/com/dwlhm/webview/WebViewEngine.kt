package com.dwlhm.webview

interface WebViewEngine {
    fun createSession(): WebViewSession
}