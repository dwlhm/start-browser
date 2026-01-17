package com.dwlhm.startbrowser

import android.app.ActivityManager
import android.app.Application
import android.os.Process
import androidx.room.Room
import com.dwlhm.browser.BrowserRuntime
import com.dwlhm.browser.api.BrowserRuntimeController
import com.dwlhm.browser.api.DefaultBrowserSessionFactory
import com.dwlhm.browser.session.SessionFocusController
import com.dwlhm.browser.session.SessionManager
import com.dwlhm.browser.session.SessionRegistry
import com.dwlhm.data.api.AppDatabase
import com.dwlhm.data.store.session.SessionRuntimeStore
import com.dwlhm.event.EventCollector
import com.dwlhm.media.MediaStateRegistry
import com.dwlhm.media.MediaStateRegistryListener
import com.dwlhm.media.api.MediaPlaybackCoordinator
import com.dwlhm.session.api.DefaultSessionFocusController
import com.dwlhm.session.api.DefaultSessionManager
import com.dwlhm.session.api.DefaultSessionRegistry
import com.dwlhm.session.api.SessionEventListener
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
    val sessionRegistry: SessionRegistry by lazy { DefaultSessionRegistry(database.sessionDao(), applicationScope) }
    val sessionFocusController: SessionFocusController by lazy { DefaultSessionFocusController() }
    val sessionRuntimeStore: SessionRuntimeStore by lazy { SessionRuntimeStore() }
    val sessionManager: SessionManager by lazy { DefaultSessionManager(
        sessionRegistry,
        DefaultBrowserSessionFactory(browserRuntime),
        sessionRuntimeStore,
        sessionFocusController
    ) }
    private val sessionEventListener: SessionEventListener by lazy { SessionEventListener(
        database.sessionDao(),
        applicationScope,
    ) }

    private var mediaPlaybackCoordinator: MediaPlaybackCoordinator? = null
    val mediaStateRegistry: MediaStateRegistry by lazy { MediaStateRegistry() }
    private var mediaStateRegistryListener: MediaStateRegistryListener? = null

    override fun onCreate() {
        super.onCreate()

        if (isMainProcess()) {
            sessionEventListener.observeEvent()

            mediaPlaybackCoordinator = MediaPlaybackCoordinator(
                context = applicationContext,
                mediaServiceClass = MediaPlaybackService::class.java,
                mediaArtworkStore = MediaStores.artworkStore,
                mediaSessionStore = MediaStores.sessionStore,
            )

            mediaPlaybackCoordinator?.listenToMediaEvent(EventCollector(applicationScope))

            mediaStateRegistryListener = MediaStateRegistryListener(mediaStateRegistry, applicationScope).apply {
                observeEvents()
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        mediaPlaybackCoordinator?.destroy()

        sessionRuntimeStore.clear()

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
