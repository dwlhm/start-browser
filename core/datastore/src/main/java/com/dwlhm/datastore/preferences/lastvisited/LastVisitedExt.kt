package com.dwlhm.datastore.preferences.lastvisited

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.lastVisitedDataStore by preferencesDataStore(
    name = "last_visited_prefs"
)