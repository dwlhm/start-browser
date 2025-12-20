package com.dwlhm.tabmanager.di

import com.dwlhm.tabmanager.internal.TabSessionNavigatorImpl
import com.dwlhm.webview.navigation.SessionNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TabSessionNavigatorModule {

    @Provides
    @Singleton
    fun provideTabSessionNavigator(
        impl: TabSessionNavigatorImpl
    ): SessionNavigator = impl
}