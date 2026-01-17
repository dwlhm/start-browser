package com.dwlhm.session.api

import com.dwlhm.browser.BrowserSession
import com.dwlhm.browser.session.SessionFocusController

class DefaultSessionFocusController: SessionFocusController {
    override fun onForeground(session: BrowserSession) {
        session.setActive(true)
        session.setFocused(true)
    }

    override fun onBackground(session: BrowserSession) {
        session.setActive(true)
        session.setFocused(false)
    }

    override fun nonactiveSession(session: BrowserSession) {
        session.setActive(false)
        session.setFocused(false)
    }
}