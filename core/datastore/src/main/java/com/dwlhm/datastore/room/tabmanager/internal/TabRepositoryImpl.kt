package com.dwlhm.datastore.room.tabmanager.internal

import com.dwlhm.datastore.room.tabmanager.api.StoredTab
import com.dwlhm.datastore.room.tabmanager.api.TabRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TabRepositoryImpl @Inject constructor(
    private val localDataSource: TabLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher,
): TabRepository {
    override fun observeAll(): Flow<List<StoredTab>> =
            localDataSource.observeAll().map { it.map { entity -> entity.toStoredTab() } }

    override suspend fun save(tab: StoredTab) =
        withContext(ioDispatcher) {
            localDataSource.save(TabEntity(tab.id, tab.url, tab.title))
        }

    override suspend fun delete(id: String) =
        withContext(ioDispatcher) {
            localDataSource.delete(id)
        }
}