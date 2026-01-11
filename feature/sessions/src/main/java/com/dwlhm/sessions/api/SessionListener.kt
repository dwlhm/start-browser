package com.dwlhm.sessions.api

import com.dwlhm.data.room.sessions.SessionDao
import com.dwlhm.data.room.sessions.SessionEntity
import com.dwlhm.event.EventCollector
import com.dwlhm.event.TabCreatedEvent
import com.dwlhm.event.TabInfoChangedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SessionListener(
    private val sessionDao: SessionDao,
    private val scope: CoroutineScope,
) {
    private val eventCollector = EventCollector(scope)

    fun observeEvent() {
        eventCollector.on<TabCreatedEvent> { event ->
            scope.launch {
                listenToTabCreated(event)
            }
        }

        eventCollector.on<TabInfoChangedEvent> { event ->
            scope.launch {
                listenToTabInfoChanged(event)
            }
        }
    }

    suspend fun listenToTabCreated(tabInfo: TabCreatedEvent) {
        val now = System.currentTimeMillis()

        sessionDao.upsert(
            SessionEntity(
                id = tabInfo.tabId,
                url = tabInfo.initialUrl,
                title = "",
                favicon = null,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun listenToTabInfoChanged(tabInfo: TabInfoChangedEvent) {
        val now = System.currentTimeMillis()

        val existing = sessionDao.findById(tabInfo.tabId)
            ?: return
        sessionDao.upsert(
            existing.copy(
                url = tabInfo.url,
                title = tabInfo.title,
                updatedAt = now
            )
        )
    }
}