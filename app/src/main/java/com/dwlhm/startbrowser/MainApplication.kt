package com.dwlhm.startbrowser

import android.app.Application
import android.webkit.CookieManager
import android.webkit.WebView
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WebView data directory untuk persistence
//        WebView.setDataDirectorySuffix("webview_data")
        
        // Configure CookieManager untuk persistent cookies
//        CookieManager.getInstance().apply {
//            setAcceptCookie(true)
//            setAcceptThirdPartyCookies(WebView(this@MainApplication), true)
//        }
    }
}