package com.dwlhm.domain.onboarding

interface OnboardingRepository {
    suspend fun hasOnboarded(): Boolean
    suspend fun setOnboarded()
}