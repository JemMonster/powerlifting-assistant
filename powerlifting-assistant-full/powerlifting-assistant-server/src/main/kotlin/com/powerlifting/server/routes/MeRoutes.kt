package com.powerlifting.server.routes

import com.powerlifting.server.dto.MeResponse
import com.powerlifting.server.userRow
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.registerMeRoutes() {
    get("/me") {
        val u = call.userRow()
        call.respond(
            MeResponse(
                userId = u.id.toString(),
                firebaseUid = u.firebaseUid,
                email = u.email,
                displayName = u.displayName
            )
        )
    }
}
