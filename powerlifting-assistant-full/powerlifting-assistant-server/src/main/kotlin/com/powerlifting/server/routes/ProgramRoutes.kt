package com.powerlifting.server.routes

import com.powerlifting.server.dto.ActiveProgramResponse
import com.powerlifting.server.dto.CalendarResponse
import com.powerlifting.server.dto.GenerateProgramRequest
import com.powerlifting.server.repository.ProgramRepository
import com.powerlifting.server.service.ProgramService
import com.powerlifting.server.userRow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

fun Route.registerProgramRoutes(
    programRepository: ProgramRepository,
    programService: ProgramService
) {
    route("/programs") {
        post("/generate") {
            val u = call.userRow()
            val req = call.receive<GenerateProgramRequest>()
            val program = programService.generateDefaultProgram(u.id, req)
            call.respond(HttpStatusCode.Created, program)
        }

        get("/active") {
            val u = call.userRow()
            val active = programRepository.getActiveProgram(u.id)
            if (active == null) {
                call.respond(ActiveProgramResponse(program = null, upcomingWorkouts = emptyList()))
                return@get
            }

            val programId = UUID.fromString(active.id)
            val today = LocalDate.now(ZoneOffset.UTC)
            val upcoming = programRepository.getUpcomingWorkouts(programId, from = today, limit = 10)
            call.respond(ActiveProgramResponse(program = active, upcomingWorkouts = upcoming))
        }
    }

    get("/calendar") {
        val u = call.userRow()
        val active = programRepository.getActiveProgram(u.id)
            ?: run {
                call.respond(CalendarResponse(from = "", to = "", days = emptyList()))
                return@get
            }

        val programId = UUID.fromString(active.id)

        val from = call.request.queryParameters["from"]?.let { LocalDate.parse(it) }
            ?: LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1)
        val to = call.request.queryParameters["to"]?.let { LocalDate.parse(it) }
            ?: from.plusMonths(1).minusDays(1)

        val days = programRepository.getCalendar(programId, from, to)
        call.respond(CalendarResponse(from = from.toString(), to = to.toString(), days = days))
    }
}
