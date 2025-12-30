package com.dwlhm.tabmanager.internal

import com.dwlhm.datastore.room.tabmanager.api.StoredTab
import com.dwlhm.datastore.room.tabmanager.api.TabRepository
import com.dwlhm.tabmanager.api.TabId
import com.dwlhm.tabmanager.api.TabPersistence
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TabPersistenceImpl @Inject constructor(
    private val tabRepository: TabRepository
): TabPersistence {
    override fun observeAll(): Flow<List<StoredTab>> =
        tabRepository.observeAll()

    override suspend fun persist(tab: StoredTab) =
        tabRepository.save(tab)

    override suspend fun remove(tabId: TabId) =
        tabRepository.delete(tabId.value)
}