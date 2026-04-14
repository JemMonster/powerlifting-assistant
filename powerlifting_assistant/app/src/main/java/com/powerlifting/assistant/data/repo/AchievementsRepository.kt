package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.api.*
import com.powerlifting.assistant.data.auth.FirebaseTokenProvider

class AchievementsRepository(
    private val api: PowerliftingApi,
    private val tokenProvider: FirebaseTokenProvider
) {
    suspend fun list(): List<AchievementDto> {
        val auth = tokenProvider.bearerToken()
        return api.getAchievements(auth)
    }

    suspend fun create(note: String, photoUrl: String? = null): AchievementDto {
        val auth = tokenProvider.bearerToken(true)
        return api.createAchievement(auth, CreateAchievementRequest(note, photoUrl))
    }

    suspend fun delete(id: String) {
        val auth = tokenProvider.bearerToken(true)
        api.deleteAchievement(auth, id)
    }
}
