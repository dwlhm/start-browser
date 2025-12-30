package com.dwlhm.datastore.di

import android.content.Context
import androidx.room.Room
import com.dwlhm.datastore.room.AppDatabase
import com.dwlhm.datastore.room.tabmanager.api.TabRepository
import com.dwlhm.datastore.room.tabmanager.internal.TabDao
import com.dwlhm.datastore.room.tabmanager.internal.TabRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "browser.db"
        )
            .fallbackToDestructiveMigration() // will be removed when it's not a poc
            .build()

    @Provides
    fun provideTabDao(
        database: AppDatabase,
    ): TabDao = database.tabDao()
}