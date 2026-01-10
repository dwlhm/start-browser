package com.dwlhm.startbrowser

import android.app.ActivityManager
import android.app.Application
import android.os.Process
import com.dwlhm.browser.BrowserRuntime
import com.dwlhm.browser.api.BrowserRuntimeController
import com.dwlhm.tabmanager.api.BackgroundTabManager
import com.dwlhm.tabmanager.api.DefaultTabManager
import com.dwlhm.tabmanager.api.TabManagerRegistry
import com.dwlhm.tabmanager.api.TabMode
import com.dwlhm.tabmanager.api.TabSessionManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {
    var browserRuntime: BrowserRuntime? = null
        private set

    var tabSessionManager: TabSessionManager? = null
        private set

    override fun onCreate() {
        super.onCreate()

        // Only initialize GeckoRuntime in the main process
        // Child processes (content, gpu, socket) should NOT create their own runtime
        if (isMainProcess()) {
            browserRuntime = BrowserRuntimeController(this)
            
            val defaultTabManager = DefaultTabManager(browserRuntime!!)
            val backgroundTabManager = BackgroundTabManager()

            val tabRegistry = TabManagerRegistry(
                managers = mapOf(
                    TabMode.DEFAULT to defaultTabManager,
                    TabMode.BACKGROUND to backgroundTabManager,
                )
            )

            tabSessionManager = TabSessionManager(tabRegistry, TabMode.DEFAULT)
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