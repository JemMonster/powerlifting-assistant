package com.powerlifting.server.repository

import com.powerlifting.server.db.dbQuery
import com.powerlifting.server.db.tables.ProgramExercisesTable
import com.powerlifting.server.db.tables.ProgramWorkoutsTable
import com.powerlifting.server.db.tables.TrainingProgramsTable
import com.powerlifting.server.dto.ProgramExerciseDto
import com.powerlifting.server.dto.ProgramWorkoutDto
import com.powerlifting.server.dto.TrainingProgramDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class ProgramRepository {

    suspend fun deactivatePrograms(userId: UUID) = dbQuery {
        TrainingProgramsTable.update({ (TrainingProgramsTable.userId eq userId) and (TrainingProgramsTable.isActive eq true) }) {
            it[isActive] = false
        }
    }

    suspend fun createProgram(
        userId: UUID,
        name: String,
        templateCode: String,
        startDate: LocalDate,
        weeks: Int,
    ): UUID = dbQuery {
        TrainingProgramsTable.insertAndGetId {
            it[TrainingProgramsTable.userId] = userId
            it[TrainingProgramsTable.name] = name
            it[TrainingProgramsTable.templateCode] = templateCode
            it[TrainingProgramsTable.startDate] = startDate
            it[TrainingProgramsTable.weeks] = weeks
            it[TrainingProgramsTable.isActive] = true
            it[TrainingProgramsTable.createdAt] = Instant.now()
        }.value
    }

    suspend fun createProgramWorkout(
        programId: UUID,
        date: LocalDate,
        title: String,
        status: String = "planned"
    ): UUID = dbQuery {
        ProgramWorkoutsTable.insertAndGetId {
            it[ProgramWorkoutsTable.programId] = programId
            it[ProgramWorkoutsTable.workoutDate] = date
            it[ProgramWorkoutsTable.title] = title
            it[ProgramWorkoutsTable.status] = status
        }.value
    }

    suspend fun createExercise(
        programWorkoutId: UUID,
        exerciseName: String,
        orderIndex: Int,
        sets: Int,
        reps: String,
        percent1rm: Double?,
        liftType: String
    ): UUID = dbQuery {
        ProgramExercisesTable.insertAndGetId {
            it[ProgramExercisesTable.programWorkoutId] = programWorkoutId
            it[ProgramExercisesTable.exerciseName] = exerciseName
            it[ProgramExercisesTable.orderIndex] = orderIndex
            it[ProgramExercisesTable.sets] = sets
            it[ProgramExercisesTable.reps] = reps
            it[ProgramExercisesTable.percent1rm] = percent1rm?.toBigDecimal()
            it[ProgramExercisesTable.liftType] = liftType
        }.value
    }

    suspend fun getActiveProgram(userId: UUID): TrainingProgramDto? = dbQuery {
        val row = TrainingProgramsTable
            .select { (TrainingProgramsTable.userId eq userId) and (TrainingProgramsTable.isActive eq true) }
            .orderBy(TrainingProgramsTable.createdAt, SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?: return@dbQuery null

        TrainingProgramDto(
            id = row[TrainingProgramsTable.id].value.toString(),
            name = row[TrainingProgramsTable.name],
            templateCode = row[TrainingProgramsTable.templateCode],
            startDate = row[TrainingProgramsTable.startDate].toString(),
            weeks = row[TrainingProgramsTable.weeks],
            isActive = row[TrainingProgramsTable.isActive]
        )
    }

    suspend fun getUpcomingWorkouts(programId: UUID, from: LocalDate, limit: Int = 7): List<ProgramWorkoutDto> = dbQuery {
        val workouts = ProgramWorkoutsTable
            .select { (ProgramWorkoutsTable.programId eq programId) and (ProgramWorkoutsTable.workoutDate greaterEq from) }
            .orderBy(ProgramWorkoutsTable.workoutDate, SortOrder.ASC)
            .limit(limit)
            .map {
                it[ProgramWorkoutsTable.id].value to ProgramWorkoutDto(
                    id = it[ProgramWorkoutsTable.id].value.toString(),
                    date = it[ProgramWorkoutsTable.workoutDate].toString(),
                    title = it[ProgramWorkoutsTable.title],
                    status = it[ProgramWorkoutsTable.status],
                    exercises = emptyList()
                )
            }

        val workoutIds = workouts.map { it.first }
        if (workoutIds.isEmpty()) return@dbQuery emptyList()

        val exercisesByWorkout = ProgramExercisesTable
            .select { ProgramExercisesTable.programWorkoutId inList workoutIds }
            .orderBy(ProgramExercisesTable.programWorkoutId, SortOrder.ASC)
            .orderBy(ProgramExercisesTable.orderIndex, SortOrder.ASC)
            .groupBy { it[ProgramExercisesTable.programWorkoutId] }
            .mapValues { (_, rows) ->
                rows.map {
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
            }

        workouts.map { (id, w) ->
            w.copy(exercises = exercisesByWorkout[id].orEmpty())
        }
    }

    suspend fun getCalendar(programId: UUID, from: LocalDate, to: LocalDate): List<com.powerlifting.server.dto.CalendarDayDto> = dbQuery {
        ProgramWorkoutsTable
            .select {
                (ProgramWorkoutsTable.programId eq programId) and
                    (ProgramWorkoutsTable.workoutDate greaterEq from) and
                    (ProgramWorkoutsTable.workoutDate lessEq to)
            }
            .orderBy(ProgramWorkoutsTable.workoutDate, SortOrder.ASC)
            .map {
                com.powerlifting.server.dto.CalendarDayDto(
                    date = it[ProgramWorkoutsTable.workoutDate].toString(),
                    title = it[ProgramWorkoutsTable.title],
                    status = it[ProgramWorkoutsTable.status]
                )
            }
    }

    suspend fun findProgramWorkout(programId: UUID, workoutId: UUID): ProgramWorkoutDto? = dbQuery {
        val row = ProgramWorkoutsTable
            .select { (ProgramWorkoutsTable.programId eq programId) and (ProgramWorkoutsTable.id eq workoutId) }
            .limit(1)
            .singleOrNull() ?: return@dbQuery null

        val exercises = ProgramExercisesTable
            .select { ProgramExercisesTable.programWorkoutId eq workoutId }
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

        ProgramWorkoutDto(
            id = workoutId.toString(),
            date = row[ProgramWorkoutsTable.workoutDate].toString(),
            title = row[ProgramWorkoutsTable.title],
            status = row[ProgramWorkoutsTable.status],
            exercises = exercises
        )
    }

    suspend fun markWorkoutCompleted(programWorkoutId: UUID) = dbQuery {
        ProgramWorkoutsTable.update({ ProgramWorkoutsTable.id eq programWorkoutId }) {
            it[status] = "completed"
        }
    }
}
