package com.dwlhm.gecko.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.mozilla.geckoview.GeckoRuntime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeckoModule {
    @Provides
    @Singleton
    fun provideGeckoRuntime(
        @ApplicationContext context: Context
    ): GeckoRuntime {
        return GeckoRuntime.create(context)

    }
}