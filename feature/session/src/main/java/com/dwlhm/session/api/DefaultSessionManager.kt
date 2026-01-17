package com.dwlhm.session.api

import com.dwlhm.browser.BrowserSession
import com.dwlhm.browser.BrowserSessionFactory
import com.dwlhm.browser.session.SessionDescriptor
import com.dwlhm.browser.session.SessionFocusController
import com.dwlhm.browser.session.SessionManager
import com.dwlhm.browser.session.SessionRegistry
import com.dwlhm.data.store.session.SessionRuntimeStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class DefaultSessionManager(
    private val sessionRegistry: SessionRegistry,
    private val sessionFactory: BrowserSessionFactory,
    private val sessionRuntimeStore: SessionRuntimeStore,
    private val sessionFocusController: SessionFocusController,
): SessionManager {
    private var _currentSession: MutableStateFlow<BrowserSession?> = MutableStateFlow(null)
    override val currentSession: StateFlow<BrowserSession?> = _currentSession

    override fun createSession(
        initialUrl: String,
        isIncognito: Boolean,
    ): String {
        val sessionId = UUID.randomUUID().toString()
        val sessionDescriptor = SessionDescriptor(
            id = sessionId,
            url = initialUrl,
            title = "New Session",
            isIncognito = isIncognito,
            isMediaSession = false
        )

        sessionRegistry.addSession(sessionDescriptor)
        sessionRegistry.setForegroundSession(sessionId)

        val browserSession = sessionFactory.create(initialUrl, isIncognito)

        _currentSession.value?.let { sessionFocusController.onBackground(it) }

        _currentSession.value = browserSession

        sessionRuntimeStore.put(sessionId, browserSession)

        return sessionId
    }

    override suspend fun openSession(sessionId: String) {
        if (sessionRegistry.foregroundSessionId.value == sessionId) return

        val descriptor = sessionRegistry.getSession(sessionId) ?: return
        var session = sessionRuntimeStore.get(descriptor.id)

        if (session == null) {
            val browserSession = sessionFactory.create(descriptor.url, descriptor.isIncognito)
            sessionRuntimeStore.put(sessionId, browserSession)
            session = browserSession
        }

        _currentSession.value?.let {
            sessionFocusController.onBackground(it)
        }

        sessionFocusController.onForeground(session)
        sessionRegistry.setForegroundSession(sessionId)

        if (descriptor.isMediaSession) {
            sessionRegistry.setMediaSession(sessionId)
        }

        _currentSession.value = session
    }

    override fun minimizeSession() {
        val sessionId = sessionRegistry.foregroundSessionId.value ?: return
        val session = sessionRuntimeStore.get(sessionId) ?: return

        sessionFocusController.onBackground(session)
        sessionRegistry.setForegroundSession(null)
        _currentSession.value = null
    }


    override fun closeSession(sessionId: String) {
        val session = sessionRuntimeStore.get(sessionId) ?: return

        sessionFocusController.nonactiveSession(session)
        sessionRuntimeStore.remove(sessionId)
        sessionRegistry.removeSession(sessionId)

        if (sessionRegistry.foregroundSessionId.value == sessionId) {
            _currentSession.value = null
            sessionRegistry.setForegroundSession(null)
        }
    }
}