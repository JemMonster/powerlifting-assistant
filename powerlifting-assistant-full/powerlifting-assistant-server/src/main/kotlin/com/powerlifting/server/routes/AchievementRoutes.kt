package com.powerlifting.server.routes

import com.powerlifting.server.dto.CreateAchievementRequest
import com.powerlifting.server.repository.AchievementsRepository
import com.powerlifting.server.userRow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.registerAchievementRoutes(repo: AchievementsRepository) {
    route("/achievements") {
        get {
            val u = call.userRow()
            val list = repo.list(u.id)
            call.respond(list)
        }

        post {
            val u = call.userRow()
            val req = call.receive<CreateAchievementRequest>()
            val created = repo.create(u.id, req)
            call.respond(HttpStatusCode.Created, created)
        }

        delete("/{id}") {
            val u = call.userRow()
            val idStr = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
            val id = UUID.fromString(idStr)
            val ok = repo.delete(u.id, id)
            if (ok) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
        }
    }
}
