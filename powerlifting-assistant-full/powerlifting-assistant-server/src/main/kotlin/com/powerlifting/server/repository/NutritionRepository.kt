package com.powerlifting.server.repository

import com.powerlifting.server.db.dbQuery
import com.powerlifting.server.db.tables.NutritionEntriesTable
import com.powerlifting.server.dto.CreateNutritionEntryRequest
import com.powerlifting.server.dto.NutritionEntryDto
import com.powerlifting.server.dto.NutritionTotalsDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class NutritionRepository {

    suspend fun getEntriesForDate(userId: UUID, date: LocalDate): Pair<NutritionTotalsDto, List<NutritionEntryDto>> = dbQuery {
        val start = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        val rows = NutritionEntriesTable
            .select {
                (NutritionEntriesTable.userId eq userId) and
                    (NutritionEntriesTable.eatenAt greaterEq start) and
                    (NutritionEntriesTable.eatenAt less end)
            }
            .orderBy(NutritionEntriesTable.eatenAt, SortOrder.ASC)
            .toList()

        val entries = rows.map {
            NutritionEntryDto(
                id = it[NutritionEntriesTable.id].value.toString(),
                title = it[NutritionEntriesTable.title],
                eatenAtIso = it[NutritionEntriesTable.eatenAt].toString(),
                calories = it[NutritionEntriesTable.calories],
                proteinG = it[NutritionEntriesTable.proteinG]
            )
        }

        val totals = NutritionTotalsDto(
            calories = entries.sumOf { it.calories },
            proteinG = entries.sumOf { it.proteinG }
        )

        totals to entries
    }

    suspend fun createEntry(userId: UUID, req: CreateNutritionEntryRequest): NutritionEntryDto = dbQuery {
        require(req.title.isNotBlank()) { "title must not be blank" }
        require(req.calories >= 0) { "calories must be >= 0" }
        require(req.proteinG >= 0) { "proteinG must be >= 0" }

        val eatenAt = req.eatenAtIso?.let { Instant.parse(it) } ?: Instant.now()

        val id = NutritionEntriesTable.insertAndGetId {
            it[NutritionEntriesTable.userId] = userId
            it[NutritionEntriesTable.eatenAt] = eatenAt
            it[NutritionEntriesTable.title] = req.title
            it[NutritionEntriesTable.calories] = req.calories
            it[NutritionEntriesTable.proteinG] = req.proteinG
        }.value

        NutritionEntryDto(
            id = id.toString(),
            title = req.title,
            eatenAtIso = eatenAt.toString(),
            calories = req.calories,
            proteinG = req.proteinG
        )
    }

    suspend fun deleteEntry(userId: UUID, entryId: UUID): Boolean = dbQuery {
        val deleted = NutritionEntriesTable.deleteWhere {
            (NutritionEntriesTable.id eq entryId) and (NutritionEntriesTable.userId eq userId)
        }
        deleted > 0
    }
}
