package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.api.*
import com.powerlifting.assistant.data.auth.FirebaseTokenProvider

class ProgramRepository(
    private val api: PowerliftingApi,
    private val tokenProvider: FirebaseTokenProvider
) {
    suspend fun getActive(): ActiveProgramResponse {
        val auth = tokenProvider.bearerToken()
        return api.getActiveProgram(auth)
    }

    suspend fun generate(startDate: String? = null, weeks: Int? = null): TrainingProgramDto {
        val auth = tokenProvider.bearerToken(true)
        return api.generateProgram(auth, GenerateProgramRequest(startDate, weeks))
    }

    suspend fun calendar(from: String, to: String): CalendarResponse {
        val auth = tokenProvider.bearerToken()
        return api.getCalendar(auth, from, to)
    }
}
