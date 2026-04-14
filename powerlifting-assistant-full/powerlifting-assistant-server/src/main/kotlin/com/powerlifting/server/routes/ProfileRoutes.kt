package com.powerlifting.server.routes

import com.powerlifting.server.dto.ProfileResponse
import com.powerlifting.server.dto.UpdateProfileRequest
import com.powerlifting.server.repository.ProfileRepository
import com.powerlifting.server.userRow
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.registerProfileRoutes(profileRepository: ProfileRepository) {
    route("/profile") {
        get {
            val u = call.userRow()
            val (profile, goals) = profileRepository.getProfile(u.id)
            val stats = profileRepository.getStats(u.id)

            call.respond(
                ProfileResponse(
                    me = com.powerlifting.server.dto.MeResponse(
                        userId = u.id.toString(),
                        firebaseUid = u.firebaseUid,
                        email = u.email,
                        displayName = u.displayName
                    ),
                    profile = profile,
                    nutritionGoals = goals,
                    stats = stats
                )
            )
        }

        put {
            val u = call.userRow()
            val req = call.receive<UpdateProfileRequest>()
            val updated = profileRepository.updateProfile(u.id, req)
            call.respond(updated)
        }
    }
}
