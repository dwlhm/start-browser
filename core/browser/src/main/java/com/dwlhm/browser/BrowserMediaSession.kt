package com.dwlhm.browser

interface BrowserMediaSession {
    fun isActive(): Boolean
    fun muteAudio(state: Boolean)
    fun nextTrack()
    fun pause()
    fun stop()
    fun play()
    fun previousTrack()
}