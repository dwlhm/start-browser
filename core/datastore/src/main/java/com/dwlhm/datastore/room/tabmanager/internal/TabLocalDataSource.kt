package com.dwlhm.datastore.room.tabmanager.internal

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TabLocalDataSource @Inject constructor(
    private val tabDao: TabDao,
) {
    fun observeAll(): Flow<List<TabEntity>> =
            tabDao.getAll()

    suspend fun save(tab: TabEntity) =
            tabDao.upsert(tab)

    suspend fun delete(id: String) =
            tabDao.delete(id)
}