package com.dwlhm.browser.session

import com.dwlhm.browser.BrowserSession
import kotlinx.coroutines.flow.StateFlow

interface SessionManager {
    val currentSession: StateFlow<BrowserSession?>

    fun createSession(
        initialUrl: String,
        isIncognito: Boolean = false,
    ): String

    suspend fun openSession(sessionId: String)
    fun minimizeSession()
    fun closeSession(sessionId: String)
}

