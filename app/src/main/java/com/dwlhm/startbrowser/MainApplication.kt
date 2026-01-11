package com.dwlhm.startbrowser

import android.app.ActivityManager
import android.app.Application
import android.os.Process
import androidx.room.Room
import com.dwlhm.browser.BrowserRuntime
import com.dwlhm.browser.api.BrowserRuntimeController
import com.dwlhm.data.api.AppDatabase
import com.dwlhm.event.EventDispatcher
import com.dwlhm.media.api.MediaEventListener
import com.dwlhm.sessions.api.SessionListener
import com.dwlhm.tabmanager.api.BackgroundTabManager
import com.dwlhm.tabmanager.api.DefaultTabManager
import com.dwlhm.tabmanager.api.TabManagerRegistry
import com.dwlhm.tabmanager.api.TabMode
import com.dwlhm.tabmanager.api.TabSessionManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@HiltAndroidApp
class MainApplication: Application() {
    private val scope = CoroutineScope(Dispatchers.Main)

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

        TabSessionManager(tabRegistry, TabMode.DEFAULT, eventDispatcher)
    }
    val eventDispatcher: EventDispatcher by lazy { EventDispatcher }

    private var sessionListener: SessionListener? = null
    private var mediaEventListener: MediaEventListener? = null

    override fun onCreate() {
        super.onCreate()

        // Only initialize GeckoRuntime in the main process
        // Child processes (content, gpu, socket) should NOT create their own runtime
        if (isMainProcess()) {
            sessionListener = SessionListener(database.sessionDao(), scope).apply {
                observeEvent()
            }

            mediaEventListener = MediaEventListener(scope).apply {
                observeEvent()
            }
        }
    }

    private fun isMainProcess(): Boolean {
        val pid = Process.myPid()
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return activityManager.runningAppProcesses?.any { processInfo ->
            processInfo.pid == pid && processInfo.processName == packageName
        } ?: false
    }
}