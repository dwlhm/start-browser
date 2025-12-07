package com.dwlhm.data.di

import android.content.Context
import com.dwlhm.data.browser.LastVisitedRepositoryImpl
import com.dwlhm.domain.browser.LastVisitedRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideLastVisitedRepository(
        @ApplicationContext context: Context
    ): LastVisitedRepository {
        return LastVisitedRepositoryImpl(context)
    }
}

