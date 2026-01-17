package com.dwlhm.session.api

import com.dwlhm.browser.session.SessionDescriptor
import com.dwlhm.browser.session.SessionRegistry
import com.dwlhm.data.room.sessions.SessionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DefaultSessionRegistry(
    private val sessionDao: SessionDao,
    private val scope: CoroutineScope,
): SessionRegistry {
    private val _sessions: MutableStateFlow<List<SessionDescriptor>> = MutableStateFlow(emptyList())
    private val _foregroundSessionId =
        MutableStateFlow<String?>(null)

    private val _mediaSessionId =
        MutableStateFlow<String?>(null)

    override val sessions = _sessions.asStateFlow()
    override val foregroundSessionId = _foregroundSessionId.asStateFlow()
    override val mediaSessionId = _mediaSessionId.asStateFlow()

    init {
        scope.launch {
            sessionDao.getAll()
                .collectLatest { entities ->
                    _sessions.value = entities.map { it.toDescriptor() }
                }
        }
    }

    override fun addSession(session: SessionDescriptor) {
        scope.launch {
            val now = System.currentTimeMillis()
            sessionDao.upsert(session.toEntity(createdAt = now, updatedAt = now))
        }
    }

    override fun removeSession(sessionId: String) {
        scope.launch {
            sessionDao.delete(sessionId)
        }

        if (_foregroundSessionId.value == sessionId) _foregroundSessionId.value = null
        if (_mediaSessionId.value == sessionId) _mediaSessionId.value = null
    }

    override fun setForegroundSession(sessionId: String?) {
        _foregroundSessionId.value = sessionId
    }

    override fun setMediaSession(sessionId: String) {
        _mediaSessionId.value = sessionId
    }

    override suspend fun getSession(sessionId: String): SessionDescriptor? {
        return sessionDao.findById(sessionId)?.toDescriptor()

    }
}