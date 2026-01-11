package com.dwlhm.gecko.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.dwlhm.browser.BrowserRuntime
import com.dwlhm.browser.BrowserSession
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings

class GeckoBrowserRuntime private constructor(
    private val geckoRuntime: GeckoRuntime
): BrowserRuntime {
    override fun createSession(): BrowserSession {
        val settings = GeckoSessionSettings.Builder()
            .useTrackingProtection(true)
            .build()

        val geckoSession = GeckoSession(settings)

        if (Looper.myLooper() == Looper.getMainLooper()) {
            geckoSession.open(geckoRuntime)
        } else {
            Handler(Looper.getMainLooper()).post {
                geckoSession.open(geckoRuntime)
            }
        }

        return GeckoBrowserSession(geckoSession)
    }

    override fun shutdown() {
        geckoRuntime.shutdown()
    }

    companion object {
        @Volatile
        private var instance: GeckoBrowserRuntime? = null

        fun getInstance(context: Context): GeckoBrowserRuntime {
            return instance ?: synchronized(this) {
                instance ?: GeckoBrowserRuntime(
                    GeckoRuntime.create(context)
                ).also { instance = it }
            }
        }
    }
}