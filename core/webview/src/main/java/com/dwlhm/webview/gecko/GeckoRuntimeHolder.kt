package com.dwlhm.webview.gecko

import android.content.Context
import org.mozilla.geckoview.GeckoRuntime

/**
 * Singleton holder untuk GeckoRuntime
 * Tujuannya agar GeckoRuntime hanya dibuat sekali
 * dan bisa dipakai berulang kali oleh seluruh GeckoView di aplikasi.
 */
object GeckoRuntimeHolder {
    @Volatile
    private var runtime: GeckoRuntime? = null

    /**
     * get GeckoRuntime instance to be consumed by GeckoView
     */
    fun get(context: Context) : GeckoRuntime {
        return runtime ?: synchronized(this) {
            runtime ?: GeckoRuntime.create(context).also { runtime = it }
        }
    }
}