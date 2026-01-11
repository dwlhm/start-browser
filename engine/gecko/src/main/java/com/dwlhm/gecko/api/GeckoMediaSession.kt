package com.dwlhm.gecko.api

import com.dwlhm.browser.BrowserMediaSession
import org.mozilla.geckoview.MediaSession

class GeckoMediaSession(
    private val geckoMediaSession: MediaSession
): BrowserMediaSession {
    override fun isActive(): Boolean {
        return geckoMediaSession.isActive
    }

    override fun muteAudio(state: Boolean) {
        geckoMediaSession.muteAudio(state)
    }

    override fun nextTrack() {
        geckoMediaSession.nextTrack()
    }

    override fun pause() {
        geckoMediaSession.pause()
    }

    override fun stop() {
        geckoMediaSession.stop()
    }

    override fun play() {
        geckoMediaSession.play()
    }

    override fun previousTrack() {
        geckoMediaSession.previousTrack()
    }
}