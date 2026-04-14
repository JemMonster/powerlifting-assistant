package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.api.*
import com.powerlifting.assistant.data.auth.FirebaseTokenProvider

class NutritionRepository(
    private val api: PowerliftingApi,
    private val tokenProvider: FirebaseTokenProvider
) {
    suspend fun getToday(dateIso: String? = null): NutritionTodayResponse {
        val auth = tokenProvider.bearerToken()
        return api.getNutritionToday(auth, dateIso)
    }

    suspend fun updateGoals(caloriesGoal: Int, proteinGoalG: Int): NutritionGoalsDto {
        val auth = tokenProvider.bearerToken(true)
        return api.updateNutritionGoals(auth, UpdateNutritionGoalsRequest(caloriesGoal, proteinGoalG))
    }

    suspend fun addEntry(title: String, calories: Int, proteinG: Int): NutritionEntryDto {
        val auth = tokenProvider.bearerToken(true)
        return api.createNutritionEntry(auth, CreateNutritionEntryRequest(title, calories, proteinG))
    }

    suspend fun deleteEntry(id: String) {
        val auth = tokenProvider.bearerToken(true)
        api.deleteNutritionEntry(auth, id)
    }
}
