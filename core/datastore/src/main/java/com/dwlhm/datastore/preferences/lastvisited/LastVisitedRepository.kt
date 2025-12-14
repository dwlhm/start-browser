package com.dwlhm.datastore.preferences.lastvisited

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LastVisitedRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val _urlKey = stringPreferencesKey("last_visited_url")
    private val _titleKey = stringPreferencesKey("last_visited_title")
    private val _timestampKey = stringPreferencesKey("last_visited_timestamp")

    val lastVisitedFlow: Flow<LastVisitedData> = context.lastVisitedDataStore.data.map { prefs ->
        LastVisitedData(
            url = prefs[_urlKey] ?: "",
            title = prefs[_titleKey] ?: "",
            timestamp = prefs[_timestampKey]?.toLongOrNull() ?: 0L
        )
    }

    suspend fun saveLastVisited(url: String, title: String) {
        context.lastVisitedDataStore.edit { prefs ->
            prefs[_urlKey] = url       // <-- pakai underscore sama seperti deklarasi
            prefs[_titleKey] = title   // <-- pakai underscore sama
            prefs[_timestampKey] = System.currentTimeMillis().toString()
        }
    }
}
