package com.dwlhm.tabmanager.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class TabSessionManager(
    private val tabRegistry: TabManagerRegistry,
    private val tabMode: TabMode
) {
    val allTabs = MutableStateFlow<MutableMap<String, TabHandle>>(mutableMapOf())
    val selectedTab = MutableStateFlow<TabHandle?>(null)

    fun createTab() {
        val manager = tabRegistry.manager(tabMode)
        val session = manager.acquire()
        val viewHost = manager.provideViewHost()
        val id = UUID.randomUUID().toString()

        val currentTabHandle = TabHandle(id, session, viewHost, tabMode)

        allTabs.update { allTabs ->
            allTabs[id] = currentTabHandle
            allTabs
        }

        selectedTab.update { currentTabHandle }
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
    }
}