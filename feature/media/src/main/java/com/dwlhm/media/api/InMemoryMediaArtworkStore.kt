package com.dwlhm.media.api

import android.graphics.Bitmap
import com.dwlhm.data.store.media.MediaAssetStore
import java.util.concurrent.ConcurrentHashMap

class InMemoryMediaArtworkStore: MediaAssetStore<Bitmap> {
    private val _store = ConcurrentHashMap<String, Bitmap>()

    override fun put(key: String, asset: Bitmap) {
        _store[key] = asset
    }

    override fun get(key: String): Bitmap? {
        return _store[key]
    }

    override fun delete(key: String) {
        _store.remove(key)
    }


}