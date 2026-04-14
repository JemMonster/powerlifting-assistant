package com.powerlifting.assistant.data.api

import retrofit2.http.*

interface PowerliftingApi {

    @GET("api/v1/profile")
    suspend fun getProfile(@Header("Authorization") auth: String): ProfileResponse

    @PUT("api/v1/profile")
    suspend fun updateProfile(
        @Header("Authorization") auth: String,
        @Body body: UpdateProfileRequest
    ): UserProfileDto

    @PUT("api/v1/nutrition/goals")
    suspend fun updateNutritionGoals(
        @Header("Authorization") auth: String,
        @Body body: UpdateNutritionGoalsRequest
    ): NutritionGoalsDto

    @GET("api/v1/nutrition/today")
    suspend fun getNutritionToday(
        @Header("Authorization") auth: String,
        @Query("date") date: String? = null
    ): NutritionTodayResponse

    @POST("api/v1/nutrition/entries")
    suspend fun createNutritionEntry(
        @Header("Authorization") auth: String,
        @Body body: CreateNutritionEntryRequest
    ): NutritionEntryDto

    @DELETE("api/v1/nutrition/entries/{id}")
    suspend fun deleteNutritionEntry(
        @Header("Authorization") auth: String,
        @Path("id") id: String
    )

    @POST("api/v1/programs/generate")
    suspend fun generateProgram(
        @Header("Authorization") auth: String,
        @Body body: GenerateProgramRequest
    ): TrainingProgramDto

    @GET("api/v1/programs/active")
    suspend fun getActiveProgram(@Header("Authorization") auth: String): ActiveProgramResponse

    @GET("api/v1/calendar")
    suspend fun getCalendar(
        @Header("Authorization") auth: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): CalendarResponse

    @POST("api/v1/workouts/sessions/start")
    suspend fun startWorkoutSession(
        @Header("Authorization") auth: String,
        @Body body: StartWorkoutSessionRequest
    ): WorkoutSessionResponse

    @GET("api/v1/workouts/sessions/{id}")
    suspend fun getWorkoutSessionDetail(
        @Header("Authorization") auth: String,
        @Path("id") sessionId: String
    ): WorkoutSessionDetailResponse

    @POST("api/v1/workouts/sessions/{id}/sets")
    suspend fun addWorkoutSets(
        @Header("Authorization") auth: String,
        @Path("id") sessionId: String,
        @Body body: AddWorkoutSetsRequest
    )

    @POST("api/v1/workouts/sessions/{id}/finish")
    suspend fun finishWorkoutSession(
        @Header("Authorization") auth: String,
        @Path("id") sessionId: String,
        @Body body: FinishWorkoutWithRatingRequest
    )

    @GET("api/v1/workouts/history")
    suspend fun getWorkoutHistory(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int? = null
    ): WorkoutHistoryResponse

    @GET("api/v1/achievements")
    suspend fun getAchievements(@Header("Authorization") auth: String): List<AchievementDto>

    @POST("api/v1/achievements")
    suspend fun createAchievement(
        @Header("Authorization") auth: String,
        @Body body: CreateAchievementRequest
    ): AchievementDto

    @DELETE("api/v1/achievements/{id}")
    suspend fun deleteAchievement(
        @Header("Authorization") auth: String,
        @Path("id") id: String
    )
}
