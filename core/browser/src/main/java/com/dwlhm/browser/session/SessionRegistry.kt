package com.dwlhm.browser.session

import kotlinx.coroutines.flow.StateFlow

interface SessionRegistry {
    val sessions: StateFlow<List<SessionDescriptor>>
    val foregroundSessionId: StateFlow<String?>
    val mediaSessionId: StateFlow<String?>

    fun addSession(session: SessionDescriptor)
    fun removeSession(sessionId: String)

    fun setForegroundSession(sessionId: String?)
    fun setMediaSession(sessionId: String)
    suspend fun getSession(sessionId: String): SessionDescriptor?
}
