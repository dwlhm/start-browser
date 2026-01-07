package com.dwlhm.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dwlhm.home.api.Home
import com.dwlhm.home.internal.HomeRepository
import com.dwlhm.tabmanager.api.TabId
import com.dwlhm.tabmanager.api.TabManager
import com.dwlhm.tabmanager.api.TabPersistence
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
) : ViewModel() {
    private val _homeState = MutableStateFlow<Home?>(null)
    val homeState: StateFlow<Home?> = _homeState

//    val tabs = tabPersistence.observeAll()
//
//    val activeTabId = tabManager.activeTabId

//    fun switchTab(tabId: TabId, fallbackUrl: String) {
//        tabManager.switchTab(tabId, fallbackUrl)
//    }

//    fun closeTab(tabId: TabId) {
//        tabManager.closeTab(tabId)
//
//        viewModelScope.launch {
//            tabPersistence.remove(tabId)
//        }
//    }
}