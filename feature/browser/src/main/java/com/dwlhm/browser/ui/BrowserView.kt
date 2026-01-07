package com.dwlhm.browser.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dwlhm.browser.BrowserSession
import com.dwlhm.gecko.ui.GeckoBrowserView

@Composable
fun BrowserView(
    session: BrowserSession,
    modifier: Modifier,
    context: Context,
) {
    GeckoBrowserView(
        session = session,
        modifier = modifier,
        context = context
    )
}