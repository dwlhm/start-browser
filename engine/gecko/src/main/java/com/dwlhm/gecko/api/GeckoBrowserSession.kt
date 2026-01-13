package com.dwlhm.gecko.api

import com.dwlhm.browser.BrowserMediaMetadata
import com.dwlhm.browser.BrowserMediaState
import com.dwlhm.browser.BrowserSession
import com.dwlhm.browser.BrowserSessionCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.MediaSession

class GeckoBrowserSession(
    private val session: GeckoSession,
): BrowserSession {
    private val _currentUrl = MutableStateFlow<String?>(null)
    private val _currentTitle = MutableStateFlow<String?>(null)
    private val _canGoBack = MutableStateFlow(false)
    private val _canGoForward = MutableStateFlow(false)
    
    /**
     * Internal tracking untuk status media playback.
     * Di-update oleh mediaSessionDelegate callbacks.
     */
    private var _hasActiveMedia: Boolean = false
    
    override val hasActiveMedia: Boolean
        get() = _hasActiveMedia
    
    override var sessionCallback: BrowserSessionCallback? = null
    
    override fun setCallback(callback: BrowserSessionCallback) {
        sessionCallback = callback
    }

    override val activeUrl: StateFlow<String?>
        get() = _currentUrl

    override val activeTitle: StateFlow<String?>
        get() = _currentTitle

    override val canGoBack: StateFlow<Boolean>
        get() = _canGoBack

    override val canGoForward: StateFlow<Boolean>
        get() = _canGoForward

    override fun attachToView(view: Any) {
        val geckoView = view as GeckoView
        // Release any existing session first
        geckoView.releaseSession()
        geckoView.setSession(session)

        session.setActive(true)
        session.setFocused(true)
    }

    override fun detachFromView() {
        session.setActive(true)
        session.setFocused(false)
    }

    override fun suspendSession(keepActive: Boolean) {
        // Selalu hilangkan focus saat suspend
        session.setFocused(false)
        
        // Hanya set active false jika tidak perlu tetap aktif (misalnya untuk media)
        if (!keepActive) {
            session.setActive(false)
        }
        // Jika keepActive = true, session tetap active agar media bisa jalan di background
    }

    override fun loadUrl(url: String) {
        session.loadUri(url)
    }

    override fun reload() {
        session.reload()
    }

    override fun stop() {
        session.stop()
    }

    override fun goBack(): Boolean {
        if (_canGoBack.value) {
            session.goBack()

            return true
        }
        return false
    }

    override fun goForward(): Boolean {
        if (_canGoForward.value) {
            session.goForward()

            return true
        }

        return false
    }

    override fun destroy() {
        session.stop()
    }

    init {
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                _canGoBack.value = canGoBack
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                _canGoForward.value = canGoForward
            }

            override fun onLoadRequest(
                session: GeckoSession,
                request: GeckoSession.NavigationDelegate.LoadRequest
            ): GeckoResult<AllowOrDeny?>? {
                val url = request.uri
                // TODO: Implement proper adblock rules (EasyList)
                val deny = url.contains("ads") || url.contains("tracker")
                return if (deny) {
                    GeckoResult.deny()   // <-- THIS
                } else {
                    null  // allow
                }
            }
        }

        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                _currentTitle.value = title
            }
        }

        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                _currentUrl.value = url
            }
        }

        session.historyDelegate = object : GeckoSession.HistoryDelegate {
            override fun onHistoryStateChange(
                session: GeckoSession,
                historyList: GeckoSession.HistoryDelegate.HistoryList
            ) {
                val currentIndex = historyList.currentIndex
                if (currentIndex >= 0 && currentIndex < historyList.size) {
                    val currentItem = historyList[currentIndex]
                    _currentUrl.value = currentItem.uri
                    _currentTitle.value = currentItem.title
                    sessionCallback?.onTabInfoChanged(
                        currentItem.title,
                        currentItem.uri
                    )
                }
            }
        }

        session.mediaSessionDelegate = object : MediaSession.Delegate {
            override fun onActivated(session: GeckoSession, mediaSession: MediaSession) {
                // Track media state - session ini sekarang punya media aktif
                _hasActiveMedia = true
                
                sessionCallback?.onMediaActivated(
                    GeckoMediaSession(
                        mediaSession
                    )
                )
            }

            override fun onDeactivated(session: GeckoSession, mediaSession: MediaSession) {
                // Track media state - media sudah tidak aktif
                _hasActiveMedia = false
                
                sessionCallback?.onMediaDeactivated()
            }

            override fun onMetadata(
                session: GeckoSession,
                mediaSession: MediaSession,
                meta: MediaSession.Metadata
            ) {
                if (meta.artwork == null) {
                    sessionCallback?.onMediaMetadataChanged(
                        BrowserMediaMetadata(
                            meta.album,
                            meta.artist,
                            null,
                            meta.title
                        )
                    )
                    return
                }

                meta.artwork!!.getBitmap(128).accept { bitmap ->
                    sessionCallback?.onMediaMetadataChanged(
                        BrowserMediaMetadata(
                            meta.album,
                            meta.artist,
                            bitmap,
                            meta.title,
                        )
                    )
                }
            }

            override fun onPlay(session: GeckoSession, mediaSession: MediaSession) {
                sessionCallback?.onMediaStateChanged(
                    BrowserMediaState.PLAY
                )
            }

            override fun onPause(session: GeckoSession, mediaSession: MediaSession) {
                sessionCallback?.onMediaStateChanged(
                    BrowserMediaState.PAUSE
                )
            }

            override fun onStop(session: GeckoSession, mediaSession: MediaSession) {
                sessionCallback?.onMediaStateChanged(
                    BrowserMediaState.STOP
                )
            }
        }
    }

}