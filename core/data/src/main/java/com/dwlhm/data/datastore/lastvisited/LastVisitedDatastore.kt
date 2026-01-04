package com.dwlhm.data.datastore.lastvisited

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.lastVisitedDataStore by preferencesDataStore(
    name = "last_visited_prefs"
)
class LastVisitedDatastore(
    private val context: Context,
) {
    private val _urlKey = stringPreferencesKey("last_visited_url")
    private val _titleKey = stringPreferencesKey("last_visited_title")
    private val _faviconKey = stringPreferencesKey("last_visited_favicon")
    private val _timestampKey = stringPreferencesKey("last_visited_timestamp")

    val lastVisitedFlow: Flow<LastVisitedData> = context.lastVisitedDataStore.data.map {
        LastVisitedData(
            url = it[_urlKey] ?: "",
            title = it[_titleKey] ?: "",
            favicon = it[_faviconKey] ?: "",
            timestamp = it[_timestampKey]?.toLongOrNull() ?: 0L
        )
    }

    suspend fun saveLastVisited(url: String, title: String, favicon: String) {
        context.lastVisitedDataStore.edit {
            it[_urlKey] = url
            it[_titleKey] = title
            it[_faviconKey] = favicon
            it[_timestampKey] = System.currentTimeMillis().toString()
        }
    }
}
