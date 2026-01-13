package com.dwlhm.browser

import kotlinx.coroutines.flow.StateFlow

interface BrowserSession {
    val activeUrl: StateFlow<String?>
    val activeTitle: StateFlow<String?>
    val canGoBack: StateFlow<Boolean>
    val canGoForward: StateFlow<Boolean>

    /**
     * Apakah session ini sedang memutar media (audio/video).
     * Digunakan untuk menentukan apakah session harus tetap aktif saat di background.
     */
    val hasActiveMedia: Boolean

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

    /**
     * Menangguhkan session saat user meninggalkan browser view.
     *
     * @param keepActive Jika true, session tetap aktif (untuk background media playback).
     *                   Jika false, session sepenuhnya ditangguhkan.
     *
     * Perilaku:
     * - keepActive = true  -> setFocused(false), setActive(true)  -> media bisa jalan di background
     * - keepActive = false -> setFocused(false), setActive(false) -> session sepenuhnya suspended
     */
    fun suspendSession(keepActive: Boolean = false)
}