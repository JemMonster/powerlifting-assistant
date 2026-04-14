package com.powerlifting.server.service

import com.powerlifting.server.dto.GenerateProgramRequest
import com.powerlifting.server.dto.TrainingProgramDto
import com.powerlifting.server.repository.ProfileRepository
import com.powerlifting.server.repository.ProgramRepository
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class ProgramService(
    private val profileRepository: ProfileRepository,
    private val programRepository: ProgramRepository,
) {

    /**
     * Training program structure based on classic powerlifting periodization (derived from Excel).
     * 3 training days per week:
     *   Day A: Squat (heavy pyramid) + Bench (heavy pyramid) + Accessories
     *   Day B: Deadlift (heavy pyramid) + Bench (volume) + Accessories
     *   Day C: Squat (medium) + Bench (medium) + Accessories
     *
     * Each main lift uses pyramid sets at different %1RM values that progress weekly.
     */
    suspend fun generateDefaultProgram(userId: UUID, req: GenerateProgramRequest): TrainingProgramDto {
        val (profile, _) = profileRepository.getProfile(userId)
        val bench = profile.bench1rm
        val squat = profile.squat1rm
        val deadlift = profile.deadlift1rm

        require(bench != null && bench > 0) { "bench1rm must be set in profile" }
        require(squat != null && squat > 0) { "squat1rm must be set in profile" }
        require(deadlift != null && deadlift > 0) { "deadlift1rm must be set in profile" }

        val startDate = req.startDate?.let { LocalDate.parse(it) } ?: LocalDate.now(ZoneOffset.UTC)
        val weeks = req.weeks?.coerceIn(1, 12) ?: 4

        programRepository.deactivatePrograms(userId)

        val programId = programRepository.createProgram(
            userId = userId,
            name = "Программа пауэрлифтера ($weeks нед.)",
            templateCode = "PL_3D_${weeks}W",
            startDate = startDate,
            weeks = weeks
        )

        // Weekly progression percentages (based on Excel training program analysis)
        // Each week increases intensity slightly
        val weeklySquatPyramid = listOf(
            // week 1: lighter
            listOf(SetGroup(0.55, 4, 1), SetGroup(0.65, 3, 3), SetGroup(0.75, 2, 3)),
            // week 2
            listOf(SetGroup(0.57, 4, 1), SetGroup(0.67, 3, 3), SetGroup(0.77, 2, 3)),
            // week 3
            listOf(SetGroup(0.60, 3, 1), SetGroup(0.70, 3, 3), SetGroup(0.80, 2, 2)),
            // week 4: peak
            listOf(SetGroup(0.60, 3, 1), SetGroup(0.72, 3, 2), SetGroup(0.82, 1, 3)),
        )

        val weeklyBenchPyramid = listOf(
            listOf(SetGroup(0.55, 4, 1), SetGroup(0.65, 3, 3), SetGroup(0.75, 2, 2)),
            listOf(SetGroup(0.57, 4, 1), SetGroup(0.67, 3, 3), SetGroup(0.77, 2, 2)),
            listOf(SetGroup(0.60, 3, 1), SetGroup(0.70, 3, 2), SetGroup(0.80, 1, 3)),
            listOf(SetGroup(0.60, 3, 1), SetGroup(0.72, 2, 2), SetGroup(0.85, 1, 3)),
        )

        val weeklyDeadliftPyramid = listOf(
            listOf(SetGroup(0.55, 3, 1), SetGroup(0.65, 3, 2), SetGroup(0.75, 2, 2)),
            listOf(SetGroup(0.57, 3, 1), SetGroup(0.67, 3, 2), SetGroup(0.77, 2, 2)),
            listOf(SetGroup(0.60, 3, 1), SetGroup(0.70, 2, 2), SetGroup(0.80, 1, 3)),
            listOf(SetGroup(0.60, 3, 1), SetGroup(0.72, 2, 2), SetGroup(0.85, 1, 2)),
        )

        // Medium day percentages (lower than heavy day)
        val weeklySquatMedium = listOf(
            listOf(SetGroup(0.50, 5, 1), SetGroup(0.60, 4, 4)),
            listOf(SetGroup(0.52, 5, 1), SetGroup(0.62, 4, 4)),
            listOf(SetGroup(0.55, 4, 1), SetGroup(0.65, 4, 3)),
            listOf(SetGroup(0.55, 4, 1), SetGroup(0.67, 3, 3)),
        )

        val weeklyBenchMedium = listOf(
            listOf(SetGroup(0.50, 5, 1), SetGroup(0.60, 4, 4)),
            listOf(SetGroup(0.52, 5, 1), SetGroup(0.62, 4, 4)),
            listOf(SetGroup(0.55, 4, 1), SetGroup(0.65, 3, 4)),
            listOf(SetGroup(0.55, 4, 1), SetGroup(0.67, 3, 3)),
        )

        var date = startDate
        // Spacing: Mon, Wed, Fri pattern (2, 2, 3 days)
        val daySpacing = listOf(2L, 2L, 3L)

        for (week in 0 until weeks) {
            val wi = week.coerceAtMost(3) // reuse week 4 values for weeks > 4

            // Day A: Heavy Squat + Heavy Bench
            val dayAId = programRepository.createProgramWorkout(
                programId = programId,
                date = date,
                title = "День A: Присед + Жим (тяжёлый)",
                status = "planned"
            )
            createPyramidExercise(dayAId, "Присед", "squat", weeklySquatPyramid[wi], startOrder = 1)
            createPyramidExercise(dayAId, "Жим лёжа", "bench", weeklyBenchPyramid[wi], startOrder = 10)
            createAccessory(dayAId, 20, listOf(
                AccessoryEx("Тяга штанги в наклоне", 4, "8-10"),
                AccessoryEx("Пресс", 3, "12-15"),
            ))

            date = date.plusDays(daySpacing[0])

            // Day B: Heavy Deadlift + Bench Volume
            val dayBId = programRepository.createProgramWorkout(
                programId = programId,
                date = date,
                title = "День B: Тяга + Жим (объём)",
                status = "planned"
            )
            createPyramidExercise(dayBId, "Становая тяга", "deadlift", weeklyDeadliftPyramid[wi], startOrder = 1)
            createPyramidExercise(dayBId, "Жим лёжа", "bench", weeklyBenchMedium[wi], startOrder = 10)
            createAccessory(dayBId, 20, listOf(
                AccessoryEx("Наклоны со штангой", 4, "8-10"),
                AccessoryEx("Жим стоя", 3, "8-10"),
            ))

            date = date.plusDays(daySpacing[1])

            // Day C: Medium Squat + Medium Bench
            val dayCId = programRepository.createProgramWorkout(
                programId = programId,
                date = date,
                title = "День C: Присед + Жим (средний)",
                status = "planned"
            )
            createPyramidExercise(dayCId, "Присед", "squat", weeklySquatMedium[wi], startOrder = 1)
            createPyramidExercise(dayCId, "Жим лёжа", "bench", weeklyBenchMedium[wi], startOrder = 10)
            createAccessory(dayCId, 20, listOf(
                AccessoryEx("Жим гантелей", 3, "10-12"),
                AccessoryEx("Французский жим", 3, "10-12"),
                AccessoryEx("Подъём на бицепс", 3, "10-12"),
            ))

            date = date.plusDays(daySpacing[2])
        }

        return TrainingProgramDto(
            id = programId.toString(),
            name = "Программа пауэрлифтера ($weeks нед.)",
            templateCode = "PL_3D_${weeks}W",
            startDate = startDate.toString(),
            weeks = weeks,
            isActive = true
        )
    }

    private data class SetGroup(val percent: Double, val reps: Int, val sets: Int)
    private data class AccessoryEx(val name: String, val sets: Int, val reps: String)

    private suspend fun createPyramidExercise(
        workoutId: UUID,
        exerciseName: String,
        liftType: String,
        pyramid: List<SetGroup>,
        startOrder: Int,
    ) {
        pyramid.forEachIndexed { idx, sg ->
            programRepository.createExercise(
                programWorkoutId = workoutId,
                exerciseName = exerciseName,
                orderIndex = startOrder + idx,
                sets = sg.sets,
                reps = sg.reps.toString(),
                percent1rm = sg.percent,
                liftType = liftType
            )
        }
    }

    private suspend fun createAccessory(workoutId: UUID, startOrder: Int, accessories: List<AccessoryEx>) {
        accessories.forEachIndexed { idx, acc ->
            programRepository.createExercise(
                programWorkoutId = workoutId,
                exerciseName = acc.name,
                orderIndex = startOrder + idx,
                sets = acc.sets,
                reps = acc.reps,
                percent1rm = null,
                liftType = "other"
            )
        }
    }
}
