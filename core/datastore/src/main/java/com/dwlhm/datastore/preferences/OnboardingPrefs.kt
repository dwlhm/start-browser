package com.dwlhm.datastore.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(
    name = "onboarding_prefs"
)

object OnboardingPrefs {
    val HAS_ONBOARDED = booleanPreferencesKey("has_onboarded")

    suspend fun hasOnboarded(context: Context): Boolean {
        return context.onboardingDataStore.data
            .map {
                it[HAS_ONBOARDED] ?: false
            }
            .first()
    }

    suspend fun setOnboarded(context: Context) {
        context.onboardingDataStore.edit {
            it[HAS_ONBOARDED] = true
        }
    }
}