package com.dwlhm.datastore.room.tabmanager.di

import com.dwlhm.datastore.room.tabmanager.api.TabRepository
import com.dwlhm.datastore.room.tabmanager.internal.TabRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TabManagerModule {
    @Binds
    @Singleton
    abstract fun bindTabRepository(
        impl: TabRepositoryImpl
    ): TabRepository

}