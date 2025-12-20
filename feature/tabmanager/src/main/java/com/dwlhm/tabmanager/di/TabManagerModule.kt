package com.dwlhm.tabmanager.di

import com.dwlhm.tabmanager.internal.TabManagerImpl
import com.dwlhm.webview.WebViewEngine
import com.dwlhm.webview.tabmanager.TabManager
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
        engine: WebViewEngine
    ): TabManager {
        return TabManagerImpl(engine)
    }
}