package com.powerlifting.server.repository

import com.powerlifting.server.db.dbQuery
import com.powerlifting.server.db.tables.AchievementsTable
import com.powerlifting.server.dto.AchievementDto
import com.powerlifting.server.dto.CreateAchievementRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID

class AchievementsRepository {

    suspend fun list(userId: UUID, limit: Int = 100): List<AchievementDto> = dbQuery {
        AchievementsTable
            .select { AchievementsTable.userId eq userId }
            .orderBy(AchievementsTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .map {
                AchievementDto(
                    id = it[AchievementsTable.id].value.toString(),
                    createdAtIso = it[AchievementsTable.createdAt].toString(),
                    note = it[AchievementsTable.note],
                    photoUrl = it[AchievementsTable.photoUrl]
                )
            }
    }

    suspend fun create(userId: UUID, req: CreateAchievementRequest): AchievementDto = dbQuery {
        require(req.note.isNotBlank()) { "note must not be blank" }

        val now = Instant.now()
        val id = AchievementsTable.insertAndGetId {
            it[AchievementsTable.userId] = userId
            it[AchievementsTable.createdAt] = now
            it[AchievementsTable.note] = req.note
            it[AchievementsTable.photoUrl] = req.photoUrl
        }.value

        AchievementDto(
            id = id.toString(),
            createdAtIso = now.toString(),
            note = req.note,
            photoUrl = req.photoUrl
        )
    }

    suspend fun delete(userId: UUID, achievementId: UUID): Boolean = dbQuery {
        AchievementsTable.deleteWhere { (AchievementsTable.id eq achievementId) and (AchievementsTable.userId eq userId) } > 0
    }
}
