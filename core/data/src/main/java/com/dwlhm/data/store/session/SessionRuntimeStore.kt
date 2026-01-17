package com.dwlhm.data.store.session

import com.dwlhm.browser.BrowserSession

class SessionRuntimeStore {
    private val sessions = mutableMapOf<String, BrowserSession>()

    fun get(sessionId: String): BrowserSession? {
        return sessions[sessionId]
    }
    fun put(sessionId: String, session: BrowserSession) {
        sessions[sessionId] = session
    }
    fun remove(sessionId: String) {
        sessions.remove(sessionId)
    }
    fun clear() {
        sessions.clear()
    }
}