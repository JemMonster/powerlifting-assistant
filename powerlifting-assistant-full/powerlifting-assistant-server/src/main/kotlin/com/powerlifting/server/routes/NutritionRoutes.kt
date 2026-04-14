package com.powerlifting.server.routes

import com.powerlifting.server.dto.CreateNutritionEntryRequest
import com.powerlifting.server.dto.NutritionTodayResponse
import com.powerlifting.server.dto.UpdateNutritionGoalsRequest
import com.powerlifting.server.repository.NutritionRepository
import com.powerlifting.server.repository.ProfileRepository
import com.powerlifting.server.userRow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

fun Route.registerNutritionRoutes(
    nutritionRepository: NutritionRepository,
    profileRepository: ProfileRepository
) {
    route("/nutrition") {
        put("/goals") {
            val u = call.userRow()
            val req = call.receive<UpdateNutritionGoalsRequest>()
            val updated = profileRepository.updateNutritionGoals(u.id, req)
            call.respond(updated)
        }

        get("/today") {
            val u = call.userRow()
            val date = call.request.queryParameters["date"]?.let { LocalDate.parse(it) }
                ?: LocalDate.now(ZoneOffset.UTC)

            val (_, goals) = profileRepository.getProfile(u.id)
            val (totals, entries) = nutritionRepository.getEntriesForDate(u.id, date)

            call.respond(
                NutritionTodayResponse(
                    date = date.toString(),
                    totals = totals,
                    goals = goals,
                    entries = entries
                )
            )
        }

        post("/entries") {
            val u = call.userRow()
            val req = call.receive<CreateNutritionEntryRequest>()
            val created = nutritionRepository.createEntry(u.id, req)
            call.respond(HttpStatusCode.Created, created)
        }

        delete("/entries/{id}") {
            val u = call.userRow()
            val idStr = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
            val id = UUID.fromString(idStr)

            val ok = nutritionRepository.deleteEntry(u.id, id)
            if (ok) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
        }
    }
}
