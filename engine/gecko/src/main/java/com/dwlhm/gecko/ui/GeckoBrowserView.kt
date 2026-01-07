package com.dwlhm.gecko.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.dwlhm.browser.BrowserSession
import org.mozilla.geckoview.GeckoView

@Composable
fun GeckoBrowserView(
    session: BrowserSession,
    modifier: Modifier,
    context: Context,
) {
    val geckoView = remember { GeckoView(context) }

    DisposableEffect(session) {
        session.attachToView(geckoView)

        onDispose {
            session.stop()
            geckoView.releaseSession()
        }
    }

    AndroidView(
        factory = { geckoView },
        modifier = modifier,
    )
}