package com.dwlhm.data.datastore.onboarding

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(
    name = "onboarding_prefs"
)

class OnboardingDatastore(
    private val context: Context,
) {
    private val _hasOnboarded = booleanPreferencesKey("has_onboarded")

    suspend fun hasOnboarded(): Boolean {
        return context.onboardingDataStore.data
            .map { it[_hasOnboarded] ?: false }
            .first()
    }

    suspend fun setOnboarded() {
        context.onboardingDataStore.edit {
            it[_hasOnboarded] = true
        }
    }
}