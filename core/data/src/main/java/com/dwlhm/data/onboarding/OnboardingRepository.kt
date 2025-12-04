package com.dwlhm.data.onboarding

import android.content.Context
import com.dwlhm.datastore.preferences.OnboardingPrefs
import com.dwlhm.domain.onboarding.OnboardingRepository

class OnboardingRepositoryImpl(
    private val context: Context
) : OnboardingRepository {
    override suspend fun hasOnboarded(): Boolean {
        return OnboardingPrefs.hasOnboarded(context)
    }

    override suspend fun setOnboarded() {
        return OnboardingPrefs.setOnboarded(context)
    }
}