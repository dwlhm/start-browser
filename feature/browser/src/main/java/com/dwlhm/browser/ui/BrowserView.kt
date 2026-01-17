package com.dwlhm.browser.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dwlhm.browser.BrowserMountController
import com.dwlhm.gecko.ui.GeckoBrowserView

@Composable
fun BrowserView(
    browserMountController: BrowserMountController,
    modifier: Modifier,
    context: Context,
) {
    GeckoBrowserView(
        mountController = browserMountController,
        modifier = modifier,
        context = context
    )
}