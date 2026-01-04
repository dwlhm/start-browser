package com.dwlhm.data.di

import android.content.Context
import androidx.room.Room
import com.dwlhm.data.room.tabs.TabDao
import com.dwlhm.data.api.AppDatabase
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
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "browser.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTabDao(
        database: AppDatabase,
    ): TabDao = database.tabDao()
}