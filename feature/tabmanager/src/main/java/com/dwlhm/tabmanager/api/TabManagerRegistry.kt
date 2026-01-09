package com.dwlhm.tabmanager.api

import com.dwlhm.browser.TabManager

class TabManagerRegistry(
    private val managers: Map<TabMode, TabManager>
) {
    fun manager(mode: TabMode): TabManager {
        return managers.getValue(mode)
    }
}