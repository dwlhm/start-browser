package com.dwlhm.gecko

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.dwlhm.webview.WebViewSession
import org.mozilla.geckoview.GeckoView

@Composable
fun GeckoViewComposable(
    session: WebViewSession,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val geckoView = remember { GeckoView(context) }

    DisposableEffect(session) {
        session.attachToView(geckoView)
        onDispose {  }
    }

    AndroidView(
        modifier = modifier,
        factory = { geckoView }
    )
}