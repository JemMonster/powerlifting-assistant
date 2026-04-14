package com.powerlifting.server.repository

import com.powerlifting.server.db.dbQuery
import com.powerlifting.server.db.tables.ProgramExercisesTable
import com.powerlifting.server.db.tables.ProgramWorkoutsTable
import com.powerlifting.server.db.tables.WorkoutSessionsTable
import com.powerlifting.server.db.tables.WorkoutSetsTable
import com.powerlifting.server.dto.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID

class WorkoutRepository {

    suspend fun startSession(
        userId: UUID,
        programWorkoutId: UUID?,
        sleepHours: Double?,
        wellbeing: Int?,
        fatigue: Int?,
        soreness: Int?,
        recommendation: String?
    ): UUID = dbQuery {
        WorkoutSessionsTable.insertAndGetId {
            it[WorkoutSessionsTable.userId] = userId
            it[WorkoutSessionsTable.programWorkoutId] = programWorkoutId
            it[WorkoutSessionsTable.startedAt] = Instant.now()
            it[WorkoutSessionsTable.finishedAt] = null
            it[WorkoutSessionsTable.workoutDurationSec] = null

            it[WorkoutSessionsTable.sleepHours] = sleepHours?.toBigDecimal()
            it[WorkoutSessionsTable.wellbeing] = wellbeing
            it[WorkoutSessionsTable.fatigue] = fatigue
            it[WorkoutSessionsTable.soreness] = soreness
            it[WorkoutSessionsTable.recommendation] = recommendation
        }.value
    }

    suspend fun addSets(userId: UUID, sessionId: UUID, req: AddWorkoutSetsRequest) = dbQuery {
        val sessionRow = WorkoutSessionsTable.select { (WorkoutSessionsTable.id eq sessionId) and (WorkoutSessionsTable.userId eq userId) }
            .limit(1)
            .singleOrNull() ?: error("Session not found")

        WorkoutSetsTable.deleteWhere { WorkoutSetsTable.sessionId eq sessionId }

        req.sets.forEach { s ->
            validateSet(s)
            WorkoutSetsTable.insert {
                it[WorkoutSetsTable.sessionId] = sessionId
                it[WorkoutSetsTable.exerciseName] = s.exerciseName
                it[WorkoutSetsTable.setNumber] = s.setNumber
                it[WorkoutSetsTable.weightKg] = s.weightKg.toBigDecimal()
                it[WorkoutSetsTable.reps] = s.reps
                it[WorkoutSetsTable.rpe] = s.rpe?.toBigDecimal()
            }
        }

        sessionRow
    }

    suspend fun finishSession(userId: UUID, sessionId: UUID, durationSec: Int, wellbeingRating: Int? = null): UUID? = dbQuery {
        require(durationSec >= 0) { "durationSec must be >= 0" }
        wellbeingRating?.let { require(it in 1..5) { "wellbeingRating must be 1..5" } }

        val row = WorkoutSessionsTable
            .select { (WorkoutSessionsTable.id eq sessionId) and (WorkoutSessionsTable.userId eq userId) }
            .limit(1)
            .singleOrNull() ?: error("Session not found")

        WorkoutSessionsTable.update({ WorkoutSessionsTable.id eq sessionId }) {
            it[finishedAt] = Instant.now()
            it[workoutDurationSec] = durationSec
            if (wellbeingRating != null) {
                it[WorkoutSessionsTable.wellbeingRating] = wellbeingRating
            }
        }

        row[WorkoutSessionsTable.programWorkoutId]
    }

    suspend fun getSessionDetail(userId: UUID, sessionId: UUID): WorkoutSessionDetailResponse? = dbQuery {
        val row = WorkoutSessionsTable
            .select { (WorkoutSessionsTable.id eq sessionId) and (WorkoutSessionsTable.userId eq userId) }
            .limit(1)
            .singleOrNull() ?: return@dbQuery null

        val programWorkoutId = row[WorkoutSessionsTable.programWorkoutId]

        val exercises = if (programWorkoutId != null) {
            ProgramExercisesTable
                .select { ProgramExercisesTable.programWorkoutId eq programWorkoutId }
                .orderBy(ProgramExercisesTable.orderIndex, SortOrder.ASC)
                .map {
                    ProgramExerciseDto(
                        id = it[ProgramExercisesTable.id].value.toString(),
                        exerciseName = it[ProgramExercisesTable.exerciseName],
                        orderIndex = it[ProgramExercisesTable.orderIndex],
                        sets = it[ProgramExercisesTable.sets],
                        reps = it[ProgramExercisesTable.reps],
                        percent1rm = it[ProgramExercisesTable.percent1rm]?.toDouble(),
                        liftType = it[ProgramExercisesTable.liftType]
                    )
                }
        } else emptyList()

        val loggedSets = WorkoutSetsTable
            .select { WorkoutSetsTable.sessionId eq sessionId }
            .orderBy(WorkoutSetsTable.setNumber, SortOrder.ASC)
            .map {
                WorkoutSetDto(
                    exerciseName = it[WorkoutSetsTable.exerciseName],
                    setNumber = it[WorkoutSetsTable.setNumber],
                    weightKg = it[WorkoutSetsTable.weightKg].toDouble(),
                    reps = it[WorkoutSetsTable.reps],
                    rpe = it[WorkoutSetsTable.rpe]?.toDouble()
                )
            }

        WorkoutSessionDetailResponse(
            sessionId = sessionId.toString(),
            programWorkoutId = programWorkoutId?.toString(),
            recommendation = row[WorkoutSessionsTable.recommendation],
            exercises = exercises,
            loggedSets = loggedSets
        )
    }

    suspend fun getHistory(userId: UUID, limit: Int = 30): WorkoutHistoryResponse = dbQuery {
        val sessions = WorkoutSessionsTable
            .select { (WorkoutSessionsTable.userId eq userId) and (WorkoutSessionsTable.finishedAt.isNotNull()) }
            .orderBy(WorkoutSessionsTable.startedAt, SortOrder.DESC)
            .limit(limit)
            .map { row ->
                val sid = row[WorkoutSessionsTable.id].value
                val programWorkoutId = row[WorkoutSessionsTable.programWorkoutId]

                val workoutTitle = if (programWorkoutId != null) {
                    ProgramWorkoutsTable
                        .select { ProgramWorkoutsTable.id eq programWorkoutId }
                        .limit(1)
                        .singleOrNull()
                        ?.get(ProgramWorkoutsTable.title)
                } else null

                val setsCount = WorkoutSetsTable
                    .select { WorkoutSetsTable.sessionId eq sid }
                    .count().toInt()

                WorkoutHistoryItemDto(
                    sessionId = sid.toString(),
                    date = row[WorkoutSessionsTable.startedAt].toString(),
                    durationSec = row[WorkoutSessionsTable.workoutDurationSec],
                    workoutTitle = workoutTitle,
                    wellbeingRating = row[WorkoutSessionsTable.wellbeingRating],
                    setsCount = setsCount
                )
            }

        WorkoutHistoryResponse(sessions = sessions)
    }

    private fun validateSet(s: WorkoutSetDto) {
        require(s.exerciseName.isNotBlank()) { "exerciseName must not be blank" }
        require(s.setNumber >= 1) { "setNumber must be >= 1" }
        require(s.weightKg >= 0) { "weightKg must be >= 0" }
        require(s.reps >= 0) { "reps must be >= 0" }
        s.rpe?.let { require(it in 0.0..10.0) { "rpe must be 0..10" } }
    }
}
