package com.dwlhm.tabmanager.internal

import com.dwlhm.webview.WebViewEngine
import com.dwlhm.webview.WebViewSession
import com.dwlhm.webview.tabmanager.TabId
import com.dwlhm.webview.tabmanager.TabManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TabManagerImpl @Inject constructor(
    private val engine: WebViewEngine
) : TabManager {

    private val _tabs = MutableStateFlow<List<TabEntry>>(emptyList())
    private val _activeTabId = MutableStateFlow<TabId?>(null)

    override val tabs = _tabs.map { entries -> entries.map { it.session } }
        .stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    override val activeSession =
        combine(_tabs, _activeTabId) { entries, activeId ->
            entries.firstOrNull { it.id == activeId }?.session
        }.stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    override fun addTab(url: String) {
        val session = engine.createSession()
        val tabId = TabId(UUID.randomUUID().toString())

        val entry = TabEntry(
            id = tabId,
            session = session
        )

        _tabs.value = _tabs.value + entry
        _activeTabId.value = tabId

        session.loadUrl(url)
    }

    override fun switchTab(tabId: TabId) {
        if (_tabs.value.any { it.id == tabId }) {
            _activeTabId.value = tabId
        }
    }

    override fun closeTab(tabId: TabId) {
        val currentTabs = _tabs.value
        val index = currentTabs.indexOfFirst { it.id == tabId }

        if (index == - 1) return

        val removed = currentTabs[index]
        removed.session.close()

        val newTabs = currentTabs.toMutableList().apply {
            removeAt(index)
        }

        _tabs.value = newTabs

        _activeTabId.value = when {
            newTabs.isEmpty() -> null
            index < newTabs.size -> newTabs[index].id
            else -> newTabs.lastOrNull()?.id
        }
    }
}