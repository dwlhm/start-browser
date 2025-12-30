package com.dwlhm.tabmanager.api

import com.dwlhm.datastore.room.tabmanager.api.StoredTab
import kotlinx.coroutines.flow.Flow

interface TabPersistence {
    fun observeAll(): Flow<List<StoredTab>>
    suspend fun persist(tab: StoredTab)
    suspend fun remove(tabId: TabId)
}