package com.dwlhm.browser.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dwlhm.browser.BrowserViewHost
import com.dwlhm.gecko.ui.GeckoBrowserView

@Composable
fun BrowserView(
    browserViewHost: BrowserViewHost,
    modifier: Modifier,
    context: Context,
) {
    GeckoBrowserView(
        viewHost = browserViewHost,
        modifier = modifier,
        context = context
    )
}