package com.dwlhm.data.di

import android.content.Context
import com.dwlhm.data.datastore.lastvisited.LastVisitedDatastore
import com.dwlhm.data.datastore.onboarding.OnboardingDatastore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatastoreModule {
    @Provides
    @Singleton
    fun provideLastVisitedDatastore(
        @ApplicationContext context: Context,
    ): LastVisitedDatastore = LastVisitedDatastore(context)

    @Provides
    @Singleton
    fun provideOnboardingDatastore(
        @ApplicationContext context: Context,
    ): OnboardingDatastore = OnboardingDatastore(context)
}