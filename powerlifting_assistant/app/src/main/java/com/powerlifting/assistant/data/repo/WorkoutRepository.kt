package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.api.*
import com.powerlifting.assistant.data.auth.FirebaseTokenProvider

class WorkoutRepository(
    private val api: PowerliftingApi,
    private val tokenProvider: FirebaseTokenProvider
) {
    suspend fun startSession(req: StartWorkoutSessionRequest): WorkoutSessionResponse {
        val auth = tokenProvider.bearerToken(true)
        return api.startWorkoutSession(auth, req)
    }

    suspend fun getSessionDetail(sessionId: String): WorkoutSessionDetailResponse {
        val auth = tokenProvider.bearerToken()
        return api.getWorkoutSessionDetail(auth, sessionId)
    }

    suspend fun addSets(sessionId: String, sets: List<WorkoutSetDto>) {
        val auth = tokenProvider.bearerToken(true)
        api.addWorkoutSets(auth, sessionId, AddWorkoutSetsRequest(sets))
    }

    suspend fun finishSession(sessionId: String, durationSec: Int, wellbeingRating: Int? = null) {
        val auth = tokenProvider.bearerToken(true)
        api.finishWorkoutSession(auth, sessionId, FinishWorkoutWithRatingRequest(durationSec, wellbeingRating))
    }

    suspend fun getHistory(limit: Int = 30): WorkoutHistoryResponse {
        val auth = tokenProvider.bearerToken()
        return api.getWorkoutHistory(auth, limit)
    }
}
