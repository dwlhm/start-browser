package com.dwlhm.data.api

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dwlhm.data.room.tabs.TabDao
import com.dwlhm.data.room.tabs.TabEntity

@Database(
    entities = [
        TabEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun tabDao(): TabDao

}