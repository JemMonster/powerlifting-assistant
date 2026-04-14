package com.powerlifting.server.repository

import com.powerlifting.server.db.dbQuery
import com.powerlifting.server.db.tables.AchievementsTable
import com.powerlifting.server.db.tables.NutritionEntriesTable
import com.powerlifting.server.db.tables.NutritionGoalsTable
import com.powerlifting.server.db.tables.UserProfileTable
import com.powerlifting.server.dto.NutritionGoalsDto
import com.powerlifting.server.dto.StatsDto
import com.powerlifting.server.dto.UpdateNutritionGoalsRequest
import com.powerlifting.server.dto.UpdateProfileRequest
import com.powerlifting.server.dto.UserProfileDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class ProfileRepository {

    suspend fun getProfile(userId: UUID): Pair<UserProfileDto, NutritionGoalsDto> = dbQuery {
        val profileRow = UserProfileTable.select { UserProfileTable.id eq userId }
            .limit(1)
            .singleOrNull()

        val profile = profileRow?.let {
            UserProfileDto(
                heightCm = it[UserProfileTable.heightCm],
                weightKg = it[UserProfileTable.weightKg]?.toDouble(),
                bench1rm = it[UserProfileTable.bench1rm]?.toDouble(),
                squat1rm = it[UserProfileTable.squat1rm]?.toDouble(),
                deadlift1rm = it[UserProfileTable.deadlift1rm]?.toDouble(),
            )
        } ?: UserProfileDto()

        val goalsRow = NutritionGoalsTable.select { NutritionGoalsTable.id eq userId }
            .limit(1)
            .singleOrNull()

        val goals = goalsRow?.let {
            NutritionGoalsDto(
                caloriesGoal = it[NutritionGoalsTable.caloriesGoal],
                proteinGoalG = it[NutritionGoalsTable.proteinGoalG]
            )
        } ?: NutritionGoalsDto(caloriesGoal = 2500, proteinGoalG = 150)

        profile to goals
    }

    suspend fun updateProfile(userId: UUID, req: UpdateProfileRequest): UserProfileDto = dbQuery {
        val now = Instant.now()

        val exists = UserProfileTable.select { UserProfileTable.id eq userId }.limit(1).count() > 0
        if (!exists) {
            UserProfileTable.insert {
                it[id] = userId
                it[heightCm] = req.heightCm
                it[weightKg] = req.weightKg?.toBigDecimal()
                it[bench1rm] = req.bench1rm?.toBigDecimal()
                it[squat1rm] = req.squat1rm?.toBigDecimal()
                it[deadlift1rm] = req.deadlift1rm?.toBigDecimal()
                it[updatedAt] = now
            }
        } else {
            UserProfileTable.update({ UserProfileTable.id eq userId }) {
                req.heightCm?.let { v -> it[heightCm] = v }
                req.weightKg?.let { v -> it[weightKg] = v.toBigDecimal() }
                req.bench1rm?.let { v -> it[bench1rm] = v.toBigDecimal() }
                req.squat1rm?.let { v -> it[squat1rm] = v.toBigDecimal() }
                req.deadlift1rm?.let { v -> it[deadlift1rm] = v.toBigDecimal() }
                it[updatedAt] = now
            }
        }

        val row = UserProfileTable.select { UserProfileTable.id eq userId }.single()
        UserProfileDto(
            heightCm = row[UserProfileTable.heightCm],
            weightKg = row[UserProfileTable.weightKg]?.toDouble(),
            bench1rm = row[UserProfileTable.bench1rm]?.toDouble(),
            squat1rm = row[UserProfileTable.squat1rm]?.toDouble(),
            deadlift1rm = row[UserProfileTable.deadlift1rm]?.toDouble(),
        )
    }

    suspend fun updateNutritionGoals(userId: UUID, req: UpdateNutritionGoalsRequest): NutritionGoalsDto = dbQuery {
        val exists = NutritionGoalsTable.select { NutritionGoalsTable.id eq userId }.limit(1).count() > 0
        if (!exists) {
            NutritionGoalsTable.insert {
                it[id] = userId
                it[caloriesGoal] = req.caloriesGoal
                it[proteinGoalG] = req.proteinGoalG
            }
        } else {
            NutritionGoalsTable.update({ NutritionGoalsTable.id eq userId }) {
                it[caloriesGoal] = req.caloriesGoal
                it[proteinGoalG] = req.proteinGoalG
            }
        }

        val row = NutritionGoalsTable.select { NutritionGoalsTable.id eq userId }.single()
        NutritionGoalsDto(
            caloriesGoal = row[NutritionGoalsTable.caloriesGoal],
            proteinGoalG = row[NutritionGoalsTable.proteinGoalG]
        )
    }

    suspend fun getStats(userId: UUID, date: LocalDate = LocalDate.now(ZoneOffset.UTC)): StatsDto = dbQuery {
        val achievementsCount = AchievementsTable.select { AchievementsTable.userId eq userId }.count().toInt()

        val start = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        val caloriesSum = NutritionEntriesTable.calories.sum()
        val proteinSum = NutritionEntriesTable.proteinG.sum()

        val sums = NutritionEntriesTable
            .slice(caloriesSum, proteinSum)
            .select {
                (NutritionEntriesTable.userId eq userId) and
                    (NutritionEntriesTable.eatenAt greaterEq start) and
                    (NutritionEntriesTable.eatenAt less end)
            }
            .single()

        val caloriesToday = sums[caloriesSum] ?: 0
        val proteinToday = sums[proteinSum] ?: 0

        StatsDto(
            achievementsCount = achievementsCount,
            caloriesToday = caloriesToday,
            proteinToday = proteinToday
        )
    }
}
