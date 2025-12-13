package com.dwlhm.gecko

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dwlhm.webview.WebViewSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

class GeckoViewSession(
    private val session: GeckoSession
): WebViewSession {
    override fun attachToView(view: Any) {
        val geckoView = view as GeckoView
        geckoView.setSession(session)
    }

    @Composable
    override fun ComposableView(modifier: Modifier) {
        GeckoViewComposable(
            session = this,
            modifier = modifier
        )
    }

    private val _canGoBack = MutableStateFlow(false)
    private val _canGoForward = MutableStateFlow(false)

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
                // Allow all navigation requests
                return null
            }
        }
    }

    override fun loadUrl(url: String) {
        session.loadUri(url)
    }

    override fun close() {
        session.close()
    }

    override val canGoBack = _canGoBack.asStateFlow()

    override fun goBack() {
        if (_canGoBack.value) {
            session.goBack()
        }
    }

    override val canGoForward = _canGoForward.asStateFlow()

    override fun goForward() {
        if (_canGoForward.value) {
            session.goForward()
        }
    }

}