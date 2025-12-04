package com.dwlhm.onboarding.internal

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.dwlhm.onboarding.api.Onboarding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

private val Context.datastore by preferencesDataStore(
    name = "onboarding"
)

class OnboardingRepository @Inject constructor(private val context: Context) {
    val messages = listOf(
        "Where do we go for today?",
        "Let’s explore something new!",
        "Have a great journey!",
        "97%...",
        "98%...",
        "99%...",
        "🥳"
    )

    fun hasOnboarded(): Flow<Onboarding> = flow {
        val hasOnboarded = context.datastore.data.first()[PerfKeys.HAS_ONBOARDED] == true
        emit(Onboarding(hasOnboarded = hasOnboarded))
    }

    suspend fun markHasOnboarded() {
        context.datastore.edit {
            context.datastore.edit {
                it[PerfKeys.HAS_ONBOARDED] = true
            }
        }
    }
}