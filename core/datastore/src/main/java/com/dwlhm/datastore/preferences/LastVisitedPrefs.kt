package com.dwlhm.datastore.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.lastVisitedDataStore by preferencesDataStore(
    name = "last_visited_prefs"
)

/**
 * Data class for last visited preferences
 */
data class LastVisitedPrefsData(
    val url: String = "",
    val title: String = ""
)

object LastVisitedPrefs {
    private val LAST_VISITED_URL = stringPreferencesKey("last_visited_url")
    private val LAST_VISITED_TITLE = stringPreferencesKey("last_visited_title")

    /**
     * Get last visited data as Flow for reactive updates
     */
    fun lastVisitedFlow(context: Context): Flow<LastVisitedPrefsData> {
        return context.lastVisitedDataStore.data
            .map { preferences ->
                LastVisitedPrefsData(
                    url = preferences[LAST_VISITED_URL] ?: "",
                    title = preferences[LAST_VISITED_TITLE] ?: ""
                )
            }
    }

    /**
     * Get last visited data synchronously (suspending)
     */
    suspend fun lastVisited(context: Context): LastVisitedPrefsData {
        return lastVisitedFlow(context).first()
    }

    /**
     * Save last visited URL and title
     */
    suspend fun setLastVisited(context: Context, url: String, title: String) {
        context.lastVisitedDataStore.edit { preferences ->
            preferences[LAST_VISITED_URL] = url
            preferences[LAST_VISITED_TITLE] = title
        }
    }
}