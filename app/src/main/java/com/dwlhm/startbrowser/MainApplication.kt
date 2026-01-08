package com.dwlhm.startbrowser

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import com.dwlhm.browser.BrowserRuntime
import com.dwlhm.browser.BrowserViewHost
import com.dwlhm.browser.TabManager
import com.dwlhm.browser.api.BrowserRuntimeController
import com.dwlhm.tabmanager.api.DefaultTabManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {
    var browserRuntime: BrowserRuntime? = null
        private set

    var tabManager: TabManager? = null
        private set

    var browserViewHost: BrowserViewHost? = null
        private set

    override fun onCreate() {
        super.onCreate()

        // Only initialize GeckoRuntime in the main process
        // Child processes (content, gpu, socket) should NOT create their own runtime
        if (isMainProcess()) {
            browserRuntime = BrowserRuntimeController(this)
            tabManager = DefaultTabManager(browserRuntime!!)
            browserViewHost = tabManager
        }
    }

    private fun isMainProcess(): Boolean {
        val pid = Process.myPid()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.runningAppProcesses?.any { processInfo ->
            processInfo.pid == pid && processInfo.processName == packageName
        } ?: false
    }
}