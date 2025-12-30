package com.dwlhm.datastore.room.tabmanager.api

import kotlinx.coroutines.flow.Flow

interface TabRepository {
    fun observeAll(): Flow<List<StoredTab>>
    suspend fun save(tab: StoredTab)
    suspend fun delete(id: String)
}