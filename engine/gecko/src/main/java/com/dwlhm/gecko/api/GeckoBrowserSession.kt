package com.dwlhm.gecko.api

import android.util.Log
import com.dwlhm.browser.BrowserMediaMetadata
import com.dwlhm.browser.BrowserMediaState
import com.dwlhm.browser.BrowserSession
import com.dwlhm.event.EventDispatcher
import com.dwlhm.event.MediaActivatedEvent
import com.dwlhm.event.MediaDeactivatedEvent
import com.dwlhm.event.MediaMetadataChangedEvent
import com.dwlhm.event.MediaStateChangedEvent
import com.dwlhm.event.TabInfoChangedEvent
import com.dwlhm.event.TabLocationChangedEvent
import com.dwlhm.event.TabTitleChangedEvent
import com.dwlhm.event.ToolbarVisibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.MediaSession
import kotlin.math.abs

class GeckoBrowserSession(
    private val sessionId: String,
    private val session: GeckoSession,
): BrowserSession {
    private var lastScrollY = 0
    private var isToolbarVisible = true
    private val _currentUrl = MutableStateFlow<String?>(null)
    private val _currentTitle = MutableStateFlow<String?>(null)
    private val _canGoBack = MutableStateFlow(false)
    private val _canGoForward = MutableStateFlow(false)
    private var _hasActiveMedia: Boolean = false

    override val activeSessionId = sessionId
    
    override val hasActiveMedia: Boolean
        get() = _hasActiveMedia

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
        session.setFocused(false)
        
        if (!keepActive) {
            session.setActive(false)
        }
    }

    override fun setActive(state: Boolean) {
        session.setActive(state)
    }

    override fun setFocused(state: Boolean) {
        session.setFocused(state)
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
                isToolbarVisible = true
                EventDispatcher.dispatch(
                    ToolbarVisibilityEvent(sessionId,  true)
                )

                val url = request.uri
                // TODO: Implement proper adblock rules (EasyList)
                val deny = url.contains("ads") || url.contains("tracker")

                return if (deny) {
                    GeckoResult.deny()
                } else {
                    null
                }
            }

            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: List<GeckoSession.PermissionDelegate.ContentPermission?>,
                hasUserGesture: Boolean
            ) {
                EventDispatcher.dispatch(
                    TabLocationChangedEvent(
                        activeSessionId,
                        url ?: "",
                        _currentTitle.value ?: "",
                    )
                )
            }
        }

        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                _currentTitle.value = title

                EventDispatcher.dispatch(
                    TabTitleChangedEvent(
                        activeSessionId,
                        _currentUrl.value ?: "",
                        title ?: "",
                    )
                )
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

                    EventDispatcher.dispatch(
                        TabInfoChangedEvent(
                            activeSessionId,
                            currentItem.uri,
                            currentItem.title,
                        )
                    )
                }
            }
        }

        session.mediaSessionDelegate = object : MediaSession.Delegate {
            override fun onActivated(session: GeckoSession, mediaSession: MediaSession) {
                _hasActiveMedia = true

                EventDispatcher.dispatch(
                    MediaActivatedEvent(
                        sessionId,
                        GeckoMediaSession(mediaSession)
                    )
                )
            }

            override fun onDeactivated(session: GeckoSession, mediaSession: MediaSession) {
                _hasActiveMedia = false

                EventDispatcher.dispatch(
                    MediaDeactivatedEvent(
                        sessionId,
                    )
                )
            }

            override fun onMetadata(
                session: GeckoSession,
                mediaSession: MediaSession,
                meta: MediaSession.Metadata
            ) {
                if (meta.artwork == null) {
                    EventDispatcher.dispatch(
                        MediaMetadataChangedEvent(
                            sessionId,
                            GeckoMediaSession(mediaSession),
                            BrowserMediaMetadata(
                                meta.album,
                                meta.artist,
                                null,
                                meta.title
                            )
                        )
                    )
                    return
                }

                meta.artwork!!.getBitmap(128).accept { bitmap ->
                    EventDispatcher.dispatch(
                        MediaMetadataChangedEvent(
                            sessionId,
                            GeckoMediaSession(mediaSession),
                            BrowserMediaMetadata(
                                meta.album,
                                meta.artist,
                                bitmap,
                                meta.title
                            )
                        )
                    )
                }
            }

            override fun onPlay(session: GeckoSession, mediaSession: MediaSession) {
                EventDispatcher.dispatch(
                    MediaStateChangedEvent(
                        sessionId,
                        GeckoMediaSession(mediaSession),
                        BrowserMediaState.PLAY,
                    )
                )
            }

            override fun onPause(session: GeckoSession, mediaSession: MediaSession) {
                EventDispatcher.dispatch(
                    MediaStateChangedEvent(
                        sessionId,
                        GeckoMediaSession(mediaSession),
                        BrowserMediaState.PAUSE,
                    )
                )
            }

            override fun onStop(session: GeckoSession, mediaSession: MediaSession) {
                EventDispatcher.dispatch(
                    MediaStateChangedEvent(
                        sessionId,
                        GeckoMediaSession(mediaSession),
                        BrowserMediaState.STOP,
                    )
                )
            }
        }

        session.scrollDelegate = object : GeckoSession.ScrollDelegate {
            override fun onScrollChanged(session: GeckoSession, scrollX: Int, scrollY: Int) {
                val threshold = 50
                val delta = scrollY - lastScrollY

                if (delta > threshold && isToolbarVisible) {
                    isToolbarVisible = false
                    EventDispatcher.dispatch(
                        ToolbarVisibilityEvent(sessionId, false)
                    )
                }
                else if (delta < -threshold && !isToolbarVisible) {
                    isToolbarVisible = true
                    EventDispatcher.dispatch(
                        ToolbarVisibilityEvent(sessionId,  true)
                    )
                }

                if (abs(delta) > threshold) {
                    lastScrollY = scrollY
                }
            }
        }
    }

}