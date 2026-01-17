package com.dwlhm.browser.api

import com.dwlhm.browser.BrowserMountController
import com.dwlhm.browser.BrowserSession

class DefaultBrowserMountController(
    private val session: BrowserSession,
): BrowserMountController {

    override fun attach(view: Any) {
        session.attachToView(view)
    }

    override fun detach() {
        session.detachFromView()
    }
}