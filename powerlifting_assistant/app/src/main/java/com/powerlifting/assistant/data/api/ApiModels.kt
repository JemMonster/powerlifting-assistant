package com.powerlifting.assistant.data.api

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val error: String,
    val details: String? = null
)

@Serializable
data class MeResponse(
    val userId: String,
    val firebaseUid: String,
    val email: String? = null,
    val displayName: String? = null
)

@Serializable
data class UserProfileDto(
    val heightCm: Int? = null,
    val weightKg: Double? = null,
    val bench1rm: Double? = null,
    val squat1rm: Double? = null,
    val deadlift1rm: Double? = null
)

@Serializable
data class NutritionGoalsDto(
    val caloriesGoal: Int,
    val proteinGoalG: Int
)

@Serializable
data class StatsDto(
    val achievementsCount: Int,
    val caloriesToday: Int,
    val proteinToday: Int
)

@Serializable
data class ProfileResponse(
    val me: MeResponse,
    val profile: UserProfileDto,
    val nutritionGoals: NutritionGoalsDto,
    val stats: StatsDto
)

@Serializable
data class UpdateProfileRequest(
    val heightCm: Int? = null,
    val weightKg: Double? = null,
    val bench1rm: Double? = null,
    val squat1rm: Double? = null,
    val deadlift1rm: Double? = null
)

@Serializable
data class UpdateNutritionGoalsRequest(
    val caloriesGoal: Int,
    val proteinGoalG: Int
)

@Serializable
data class NutritionEntryDto(
    val id: String,
    val title: String,
    val eatenAtIso: String,
    val calories: Int,
    val proteinG: Int
)

@Serializable
data class CreateNutritionEntryRequest(
    val title: String,
    val calories: Int,
    val proteinG: Int,
    val eatenAtIso: String? = null
)

@Serializable
data class NutritionTotalsDto(
    val calories: Int,
    val proteinG: Int
)

@Serializable
data class NutritionTodayResponse(
    val date: String,
    val totals: NutritionTotalsDto,
    val goals: NutritionGoalsDto,
    val entries: List<NutritionEntryDto>
)

@Serializable
data class GenerateProgramRequest(
    val startDate: String? = null,
    val weeks: Int? = null
)

@Serializable
data class TrainingProgramDto(
    val id: String,
    val name: String,
    val templateCode: String,
    val startDate: String,
    val weeks: Int,
    val isActive: Boolean
)

@Serializable
data class ProgramExerciseDto(
    val id: String,
    val exerciseName: String,
    val orderIndex: Int,
    val sets: Int,
    val reps: String,
    val percent1rm: Double? = null,
    val liftType: String
)

@Serializable
data class ProgramWorkoutDto(
    val id: String,
    val date: String,
    val title: String,
    val status: String,
    val exercises: List<ProgramExerciseDto>
)

@Serializable
data class ActiveProgramResponse(
    val program: TrainingProgramDto? = null,
    val upcomingWorkouts: List<ProgramWorkoutDto> = emptyList()
)

@Serializable
data class CalendarDayDto(
    val date: String,
    val title: String,
    val status: String
)

@Serializable
data class CalendarResponse(
    val from: String,
    val to: String,
    val days: List<CalendarDayDto>
)

@Serializable
data class StartWorkoutSessionRequest(
    val programWorkoutId: String? = null,
    val sleepHours: Double? = null,
    val wellbeing: Int? = null,
    val fatigue: Int? = null,
    val soreness: Int? = null
)

@Serializable
data class WorkoutSessionResponse(
    val sessionId: String,
    val recommendation: String? = null
)

@Serializable
data class FinishWorkoutSessionRequest(
    val workoutDurationSec: Int
)

@Serializable
data class AchievementDto(
    val id: String,
    val createdAtIso: String,
    val note: String,
    val photoUrl: String? = null
)

@Serializable
data class CreateAchievementRequest(
    val note: String,
    val photoUrl: String? = null
)

@Serializable
data class FinishWorkoutWithRatingRequest(
    val workoutDurationSec: Int,
    val wellbeingRating: Int? = null
)

@Serializable
data class WorkoutHistoryItemDto(
    val sessionId: String,
    val date: String,
    val durationSec: Int? = null,
    val workoutTitle: String? = null,
    val wellbeingRating: Int? = null,
    val setsCount: Int = 0
)

@Serializable
data class WorkoutHistoryResponse(
    val sessions: List<WorkoutHistoryItemDto>
)

@Serializable
data class WorkoutSessionDetailResponse(
    val sessionId: String,
    val programWorkoutId: String? = null,
    val recommendation: String? = null,
    val exercises: List<ProgramExerciseDto> = emptyList(),
    val loggedSets: List<WorkoutSetDto> = emptyList()
)

@Serializable
data class WorkoutSetDto(
    val exerciseName: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double? = null
)

@Serializable
data class AddWorkoutSetsRequest(
    val sets: List<WorkoutSetDto>
)
