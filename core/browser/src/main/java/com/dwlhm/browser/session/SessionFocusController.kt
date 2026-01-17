package com.dwlhm.browser.session

import com.dwlhm.browser.BrowserSession

interface SessionFocusController {
    fun onForeground(session: BrowserSession)
    fun onBackground(session: BrowserSession)
    fun nonactiveSession(session: BrowserSession)
}