package com.dwlhm.gecko

import android.content.Context
import com.dwlhm.webview.WebViewEngine
import org.mozilla.geckoview.GeckoRuntime

object GeckoViewRuntime {
    @Volatile
    private var runtime: GeckoRuntime? = null

    fun get(context: Context): GeckoRuntime {
        return runtime ?: synchronized(this) {
            runtime ?: GeckoRuntime.create(context).also { runtime = it }
        }
    }

    /**
     * Mengembalikan WebViewEngine berbasis Gecko
     */
    fun asEngine(context: Context): WebViewEngine {
        val runtime = get(context)
        return GeckoViewEngine(runtime)
    }
}