package com.powerlifting.server.repository

import com.powerlifting.server.db.dbQuery
import com.powerlifting.server.db.tables.NutritionGoalsTable
import com.powerlifting.server.db.tables.UserProfileTable
import com.powerlifting.server.db.tables.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID

data class UserRow(
    val id: UUID,
    val firebaseUid: String,
    val email: String?,
    val displayName: String?,
)

class UserRepository {

    suspend fun getOrCreate(firebaseUid: String, email: String?, displayName: String?): UserRow = dbQuery {
        val existing = UsersTable.select { UsersTable.firebaseUid eq firebaseUid }
            .limit(1)
            .singleOrNull()

        if (existing != null) {
            // Update email/name if changed
            UsersTable.update({ UsersTable.id eq existing[UsersTable.id].value }) {
                it[UsersTable.email] = email
                it[UsersTable.displayName] = displayName
            }
            return@dbQuery UserRow(
                id = existing[UsersTable.id].value,
                firebaseUid = existing[UsersTable.firebaseUid],
                email = email ?: existing[UsersTable.email],
                displayName = displayName ?: existing[UsersTable.displayName]
            )
        }

        val now = Instant.now()
        val newId = UsersTable.insertAndGetId {
            it[UsersTable.firebaseUid] = firebaseUid
            it[UsersTable.email] = email
            it[UsersTable.displayName] = displayName
            it[UsersTable.createdAt] = now
        }.value

        // Create default profile row (empty) and default goals
        UserProfileTable.insertIgnore {
            it[id] = newId
            it[heightCm] = null
            it[weightKg] = null
            it[bench1rm] = null
            it[squat1rm] = null
            it[deadlift1rm] = null
            it[updatedAt] = now
        }

        NutritionGoalsTable.insertIgnore {
            it[id] = newId
            it[caloriesGoal] = 2500
            it[proteinGoalG] = 150
        }

        return@dbQuery UserRow(
            id = newId,
            firebaseUid = firebaseUid,
            email = email,
            displayName = displayName
        )
    }
}
