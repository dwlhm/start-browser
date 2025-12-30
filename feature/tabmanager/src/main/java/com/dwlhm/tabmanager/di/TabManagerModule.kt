package com.dwlhm.tabmanager.di

import com.dwlhm.tabmanager.internal.TabManagerImpl
import com.dwlhm.webview.WebViewEngine
import com.dwlhm.tabmanager.api.TabManager
import com.dwlhm.tabmanager.api.TabPersistence
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TabManagerModule {
    @Provides
    @Singleton
    fun provideTabManager(
        engine: WebViewEngine,
        tabPersistence: TabPersistence
    ): TabManager {
        return TabManagerImpl(engine, tabPersistence)
    }
}