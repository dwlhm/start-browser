package com.dwlhm.data.api

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dwlhm.data.room.sessions.SessionDao
import com.dwlhm.data.room.sessions.SessionEntity
import com.dwlhm.data.room.tabs.TabDao
import com.dwlhm.data.room.tabs.TabEntity

@Database(
    entities = [
        TabEntity::class,
        SessionEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun tabDao(): TabDao
    abstract fun sessionDao(): SessionDao
}