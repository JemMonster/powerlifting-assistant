package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.api.ProfileResponse
import com.powerlifting.assistant.data.api.UpdateProfileRequest
import com.powerlifting.assistant.data.api.UserProfileDto
import com.powerlifting.assistant.data.api.PowerliftingApi
import com.powerlifting.assistant.data.auth.FirebaseTokenProvider

class ProfileRepository(
    private val api: PowerliftingApi,
    private val tokenProvider: FirebaseTokenProvider
) {
    suspend fun getProfile(): ProfileResponse {
        val auth = tokenProvider.bearerToken()
        return api.getProfile(auth)
    }

    suspend fun updateProfile(req: UpdateProfileRequest): UserProfileDto {
        val auth = tokenProvider.bearerToken(true)
        return api.updateProfile(auth, req)
    }
}
