package com.dwlhm.tabmanager.internal

import com.dwlhm.webview.WebViewEngine
import com.dwlhm.tabmanager.api.TabId
import com.dwlhm.tabmanager.api.TabManager
import com.dwlhm.tabmanager.api.TabSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TabManagerImpl @Inject constructor(
    private val engine: WebViewEngine
) : TabManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _entries = MutableStateFlow<List<TabEntry>>(emptyList())
    private val _activeTabId = MutableStateFlow<TabId?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val tabs: StateFlow<List<TabSnapshot>> = _entries
        .flatMapLatest { entries ->
            if (entries.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    entries.map { entry ->
                        combine(
                            entry.session.currentTitle,
                            entry.session.currentUrl
                        ) { title, url ->
                            TabSnapshot(
                                id = entry.id,
                                title = title ?: "no title",
                                url = url ?: "no url"
                            )
                        }
                    }
                ) { snapshots -> 
                    val list = snapshots.toList()
                    list
                }
            }
        }
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            emptyList()
        )

    override val activeTabId = _activeTabId.stateIn(
        scope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    override val activeSession =
        combine(_entries, _activeTabId) { entries, activeId ->
            entries.firstOrNull { it.id == activeId }?.session
        }.stateIn(
            scope,
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

        _entries.value = _entries.value + entry

        _activeTabId.value = tabId

        session.loadUrl(url)
    }

    override fun switchTab(tabId: TabId) {
        if (_entries.value.any { it.id == tabId }) {
            _activeTabId.value = tabId
        }
    }

    override fun closeTab(tabId: TabId) {
        val currentTabs = _entries.value
        val index = currentTabs.indexOfFirst { it.id == tabId }

        if (index == - 1) return

        val removed = currentTabs[index]
        removed.session.close()

        val newTabs = currentTabs.toMutableList().apply {
            removeAt(index)
        }

        _entries.value = newTabs

        _activeTabId.value = when {
            newTabs.isEmpty() -> null
            index < newTabs.size -> newTabs[index].id
            else -> newTabs.lastOrNull()?.id
        }
    }
}