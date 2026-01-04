package com.dwlhm.data.room.tabs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dwlhm.data.room.tabs.TabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {
    @Query("SELECT * FROM tabs")
    fun getAll(): Flow<List<TabEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun upsert(tab: TabEntity)

    @Query("DELETE from tabs WHERE id = :id")
    suspend fun delete(id: String)
}