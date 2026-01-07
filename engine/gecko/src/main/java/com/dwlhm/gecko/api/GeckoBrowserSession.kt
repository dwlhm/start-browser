package com.dwlhm.gecko.api

import android.util.Log
import com.dwlhm.browser.BrowserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

class GeckoBrowserSession(
    private val session: GeckoSession
): BrowserSession {
    private val _currentUrl = MutableStateFlow<String?>(null)
    private val _currentTitle = MutableStateFlow<String?>(null)
    private val _canGoBack = MutableStateFlow(false)
    private val _canGoForward = MutableStateFlow(false)

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
                Log.d("url", request.uri)
                _currentUrl.value = request.uri
                // Allow all navigation requests
                return null
            }
        }
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                _currentTitle.value = title
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
                }
            }
        }
    }

}