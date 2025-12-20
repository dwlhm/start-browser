package com.dwlhm.home.ui

import androidx.lifecycle.ViewModel
import com.dwlhm.home.api.Home
import com.dwlhm.home.internal.HomeRepository
import com.dwlhm.tabmanager.api.TabId
import com.dwlhm.tabmanager.api.TabManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val tabManager: TabManager
) : ViewModel() {
    private val _homeState = MutableStateFlow<Home?>(null)
    val homeState: StateFlow<Home?> = _homeState

    val tabs = tabManager.tabs
    val activeTabId = tabManager.activeTabId

    fun switchTab(tabId: TabId) {
        tabManager.switchTab(tabId)
    }

    fun closeTab(tabId: TabId) {
        tabManager.closeTab(tabId)
    }
}