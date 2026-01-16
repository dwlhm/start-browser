package com.dwlhm.media.api

import com.dwlhm.browser.BrowserMediaSession
import com.dwlhm.data.store.media.MediaAssetStore

class InMemoryMediaSessionStore: MediaAssetStore<BrowserMediaSession> {
    private var store: MutableMap<String, BrowserMediaSession> = mutableMapOf()

    override fun put(key: String, asset: BrowserMediaSession) {
        store.put(key, asset)
    }

    override fun get(key: String): BrowserMediaSession? {
        return store[key]
    }

    override fun delete(key: String) {
        if (store[key] == null) return

        store.remove(key)
    }
}