package com.dwlhm.browser

import android.graphics.Bitmap

data class BrowserMediaMetadata(
    val album: String?,
    val artist: String?,
    val artwork: Bitmap?,
    val title: String?,
)