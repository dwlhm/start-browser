package com.dwlhm.startbrowser.store

import android.graphics.Bitmap
import com.dwlhm.browser.BrowserMediaSession
import com.dwlhm.data.store.media.MediaAssetStore
import com.dwlhm.media.api.InMemoryMediaArtworkStore
import com.dwlhm.media.api.InMemoryMediaSessionStore

object MediaStores {
    val artworkStore: MediaAssetStore<Bitmap> = InMemoryMediaArtworkStore()
    val sessionStore: MediaAssetStore<BrowserMediaSession> = InMemoryMediaSessionStore()
}