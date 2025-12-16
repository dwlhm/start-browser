package com.dwlhm.startbrowser.di

import com.dwlhm.gecko.GeckoViewEngine
import com.dwlhm.webview.WebViewEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BrowserEngineModule {
    @Provides
    @Singleton
    fun provideWebViewEngine(
        geckoViewEngine: GeckoViewEngine
    ): WebViewEngine = geckoViewEngine
}