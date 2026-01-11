package com.dwlhm.data.room.sessions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions")
    fun getAll(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun findById(id: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun upsert(session: SessionEntity)

    @Query("DELETE from sessions WHERE id = :id")
    suspend fun delete(id: String)
}