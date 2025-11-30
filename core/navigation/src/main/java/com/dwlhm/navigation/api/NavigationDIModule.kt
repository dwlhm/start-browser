package com.dwlhm.navigation.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavigationDIModule {

    @Provides
    @Singleton
    fun provideRouteRegistrar(): RouteRegistrar = RouteRegistrarImpl()
}