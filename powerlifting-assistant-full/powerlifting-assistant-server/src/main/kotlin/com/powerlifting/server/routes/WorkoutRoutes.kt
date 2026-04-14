package com.powerlifting.server.routes

import com.powerlifting.server.dto.*
import com.powerlifting.server.repository.ProgramRepository
import com.powerlifting.server.repository.WorkoutRepository
import com.powerlifting.server.service.RecoveryService
import com.powerlifting.server.userRow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.registerWorkoutRoutes(
    workoutRepository: WorkoutRepository,
    programRepository: ProgramRepository,
    recoveryService: RecoveryService
) {
    route("/workouts") {
        route("/sessions") {
            post("/start") {
                val u = call.userRow()
                val req = call.receive<StartWorkoutSessionRequest>()

                val recommendation = recoveryService.makeRecommendation(
                    sleepHours = req.sleepHours,
                    wellbeing = req.wellbeing,
                    fatigue = req.fatigue,
                    soreness = req.soreness
                )

                val programWorkoutId = req.programWorkoutId?.let { UUID.fromString(it) }

                val sessionId = workoutRepository.startSession(
                    userId = u.id,
                    programWorkoutId = programWorkoutId,
                    sleepHours = req.sleepHours,
                    wellbeing = req.wellbeing,
                    fatigue = req.fatigue,
                    soreness = req.soreness,
                    recommendation = recommendation
                )

                call.respond(
                    HttpStatusCode.Created,
                    WorkoutSessionResponse(sessionId = sessionId.toString(), recommendation = recommendation)
                )
            }

            post("/{id}/sets") {
                val u = call.userRow()
                val idStr = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
                val sessionId = UUID.fromString(idStr)
                val req = call.receive<AddWorkoutSetsRequest>()

                workoutRepository.addSets(u.id, sessionId, req)
                call.respond(HttpStatusCode.NoContent)
            }

            post("/{id}/finish") {
                val u = call.userRow()
                val idStr = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
                val sessionId = UUID.fromString(idStr)
                val req = call.receive<FinishWorkoutWithRatingRequest>()

                val programWorkoutId = workoutRepository.finishSession(
                    u.id, sessionId, req.workoutDurationSec, req.wellbeingRating
                )

                if (programWorkoutId != null) {
                    programRepository.markWorkoutCompleted(programWorkoutId)
                }

                call.respond(HttpStatusCode.NoContent)
            }

            get("/{id}") {
                val u = call.userRow()
                val idStr = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
                val sessionId = UUID.fromString(idStr)

                val detail = workoutRepository.getSessionDetail(u.id, sessionId)
                    ?: throw IllegalArgumentException("Session not found")
                call.respond(detail)
            }
        }

        get("/history") {
            val u = call.userRow()
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 30
            val history = workoutRepository.getHistory(u.id, limit)
            call.respond(history)
        }
    }
}
