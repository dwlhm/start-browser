package com.dwlhm.data.store.media

interface MediaAssetStore<T> {
    fun put(key: String, asset: T)
    fun get(key: String): T?
    fun delete(key: String)
}