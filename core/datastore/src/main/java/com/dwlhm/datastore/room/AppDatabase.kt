package com.dwlhm.datastore.room

import androidx.room.Database
import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import com.dwlhm.datastore.room.tabmanager.internal.TabDao
import com.dwlhm.datastore.room.tabmanager.internal.TabEntity

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