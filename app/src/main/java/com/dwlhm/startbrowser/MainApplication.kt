package com.dwlhm.startbrowser

import android.app.ActivityManager
import android.app.Application
import android.graphics.Bitmap
import android.os.Process
import androidx.room.Room
import com.dwlhm.browser.BrowserRuntime
import com.dwlhm.browser.api.BrowserRuntimeController
import com.dwlhm.data.api.AppDatabase
import com.dwlhm.data.store.media.MediaAssetStore
import com.dwlhm.event.EventCollector
import com.dwlhm.event.EventDispatcher
import com.dwlhm.media.MediaStateRegistry
import com.dwlhm.media.MediaStateRegistryListener
import com.dwlhm.media.api.InMemoryMediaArtworkStore
import com.dwlhm.media.api.MediaPlaybackCoordinator
import com.dwlhm.sessions.api.SessionListener
import com.dwlhm.tabmanager.api.BackgroundTabManager
import com.dwlhm.tabmanager.api.DefaultTabManager
import com.dwlhm.tabmanager.api.TabManagerRegistry
import com.dwlhm.tabmanager.api.TabMode
import com.dwlhm.tabmanager.api.TabSessionManager
import com.dwlhm.startbrowser.services.MediaPlaybackService
import com.dwlhm.startbrowser.store.MediaStores
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@HiltAndroidApp
class MainApplication: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "browser.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val browserRuntime: BrowserRuntime by lazy { BrowserRuntimeController(this) }
    val tabSessionManager: TabSessionManager by lazy {
        val defaultTabManager = DefaultTabManager(browserRuntime)
        val backgroundTabManager = BackgroundTabManager()

        val tabRegistry = TabManagerRegistry(
            managers = mapOf(
                TabMode.DEFAULT to defaultTabManager,
                TabMode.BACKGROUND to backgroundTabManager,
            )
        )

        TabSessionManager(tabRegistry, TabMode.DEFAULT, eventDispatcher, mediaStateRegistry)
    }
    val eventDispatcher: EventDispatcher by lazy { EventDispatcher }

    private var sessionListener: SessionListener? = null
    private var mediaPlaybackCoordinator: MediaPlaybackCoordinator? = null
    val mediaStateRegistry: MediaStateRegistry by lazy { MediaStateRegistry() }
    private var mediaStateRegistryListener: MediaStateRegistryListener? = null

    override fun onCreate() {
        super.onCreate()

        if (isMainProcess()) {
            sessionListener = SessionListener(database.sessionDao(), applicationScope).apply {
                observeEvent()
            }

            mediaPlaybackCoordinator = MediaPlaybackCoordinator(
                context = applicationContext,
                mediaServiceClass = MediaPlaybackService::class.java,
                mediaArtworkStore = MediaStores.artworkStore,
                mediaSessionStore = MediaStores.sessionStore,
            )

            mediaPlaybackCoordinator?.listenToMediaEvent(EventCollector(applicationScope))
            
            // Initialize MediaStateRegistryListener untuk track media state per tab
            mediaStateRegistryListener = MediaStateRegistryListener(mediaStateRegistry, applicationScope).apply {
                observeEvents()
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        mediaPlaybackCoordinator?.destroy()
        applicationScope.cancel()
    }

    private fun isMainProcess(): Boolean {
        val pid = Process.myPid()
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return activityManager.runningAppProcesses?.any { processInfo ->
            processInfo.pid == pid && processInfo.processName == packageName
        } ?: false
    }
}
