package com.dwlhm.tabmanager.di

import com.dwlhm.tabmanager.api.TabPersistence
import com.dwlhm.tabmanager.internal.TabPersistenceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TabPersistenceModule {
    @Provides
    @Singleton
    fun provideTabPersistence(
        impl: TabPersistenceImpl
    ): TabPersistence = impl
}