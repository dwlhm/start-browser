package com.dwlhm.gecko.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.dwlhm.browser.BrowserSession
import com.dwlhm.browser.BrowserViewHost
import org.mozilla.geckoview.GeckoView

@Composable
fun GeckoBrowserView(
    viewHost: BrowserViewHost,
    modifier: Modifier,
    context: Context,
) {
    val geckoView = remember { GeckoView(context) }

    DisposableEffect(Unit) {
        viewHost.attach(geckoView)

        onDispose {
            viewHost.detach()
            geckoView.releaseSession()
        }
    }

    AndroidView(
        factory = { geckoView },
        modifier = modifier,
    )
}