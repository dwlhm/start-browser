package com.dwlhm.tabmanager.api

import com.dwlhm.browser.BrowserSessionCallback
import com.dwlhm.event.EventDispatcher
import com.dwlhm.event.TabClosedEvent
import com.dwlhm.event.TabCreatedEvent
import com.dwlhm.event.TabInfoChangedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.util.UUID

class TabSessionManager(
    private val tabRegistry: TabManagerRegistry,
    private val tabMode: TabMode,
    private val eventDispatcher: EventDispatcher
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    val allTabs = MutableStateFlow<MutableMap<String, TabHandle>>(mutableMapOf())
    val selectedTab = MutableStateFlow<TabHandle?>(null)

    init {
        observeSelectedTab()
    }

    fun createTab() {
        val manager = tabRegistry.manager(tabMode)
        val session = manager.newSession()
        val viewHost = manager.provideViewHost()
        val id = UUID.randomUUID().toString()

        val currentTabHandle = TabHandle(id, session, viewHost, tabMode)

        eventDispatcher.dispatch(TabCreatedEvent(
            tabId = id,
            initialUrl = ""
        ))

        allTabs.update { allTabs ->
            allTabs[id] = currentTabHandle
            allTabs
        }

        selectedTab.update { currentTabHandle }
    }

    fun openTab(id: String) {
        val requestedTab = allTabs.value[id]

        if (requestedTab != null) {
            selectedTab.update { requestedTab }
        } else {
            // Tab not in memory, restore it with the same ID
            val manager = tabRegistry.manager(tabMode)
            val session = manager.newSession()
            val viewHost = manager.provideViewHost()

            val restoredTabHandle = TabHandle(id, session, viewHost, tabMode)

            allTabs.update { tabs ->
                tabs[id] = restoredTabHandle
                tabs
            }

            selectedTab.update { restoredTabHandle }
        }
    }

    fun closeTab(id: String) {
        allTabs.update { tabs ->
            tabs.remove(id)
            tabs
        }

        // Clear selection if the closed tab was selected
        if (selectedTab.value?.id == id) {
            selectedTab.update { null }
        }

        eventDispatcher.dispatch(TabClosedEvent(
            tabId = id
        ))
    }

    private fun observeSelectedTab() {
        selectedTab.onEach { tabHandle ->
            tabHandle?.session?.sessionCallback = object : BrowserSessionCallback {
                override fun onTabInfoChanged(title: String, url: String) {
                    eventDispatcher.dispatch(
                        TabInfoChangedEvent(
                            tabId = tabHandle.id,
                            title = title,
                            url = url
                        )
                    )
                }
            }
        }.launchIn(scope)
    }
}