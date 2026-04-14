package com.powerlifting.server

import com.powerlifting.server.auth.FirebaseTokenVerifier
import com.powerlifting.server.auth.FirebaseUserPrincipal
import com.powerlifting.server.config.AppConfig
import com.powerlifting.server.config.ConfigLoader
import com.powerlifting.server.db.DatabaseFactory

import com.powerlifting.server.repository.*
import com.powerlifting.server.routes.registerAchievementRoutes
import com.powerlifting.server.routes.registerMeRoutes
import com.powerlifting.server.routes.registerNutritionRoutes
import com.powerlifting.server.routes.registerProfileRoutes
import com.powerlifting.server.routes.registerProgramRoutes
import com.powerlifting.server.routes.registerWorkoutRoutes
import com.powerlifting.server.service.ProgramService
import com.powerlifting.server.service.RecoveryService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.AttributeKey
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.Route
import io.ktor.server.application.ApplicationCallPipeline
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

private val PrincipalKey = AttributeKey<FirebaseUserPrincipal>("firebasePrincipal")
private val UserRowKey = AttributeKey<UserRow>("userRow")

fun Application.module(config: AppConfig = ConfigLoader.loadFromEnv()) {
    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        )
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "bad_request", "details" to (cause.message ?: "")))
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "internal_error", "details" to (cause.message ?: "")))
        }
    }

    if (config.corsAllowAll) {
        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            allowHeader("X-DEV-UID")
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
        }
    }

    // Init DB
    DatabaseFactory.init(config.db)

    // Init Firebase Admin (unless dev bypass)
    val tokenVerifier: FirebaseTokenVerifier? = if (config.devBypassAuth) {
        this.log.warn("DEV_BYPASS_AUTH enabled: Firebase verification is skipped")
        null
    } else {
        FirebaseTokenVerifier.init(config.firebase)
        FirebaseTokenVerifier.createVerifier()
    }

    val userRepository = UserRepository()
    val profileRepository = ProfileRepository()
    val nutritionRepository = NutritionRepository()
    val programRepository = ProgramRepository()
    val workoutRepository = WorkoutRepository()
    val achievementsRepository = AchievementsRepository()

    val recoveryService = RecoveryService()
    val programService = ProgramService(profileRepository, programRepository)

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        route("/api/v1") {
            routeAuth(tokenVerifier, config, userRepository) {
                registerMeRoutes()
                registerProfileRoutes(profileRepository)
                registerNutritionRoutes(nutritionRepository, profileRepository)
                registerProgramRoutes(programRepository, programService)
                registerWorkoutRoutes(workoutRepository, programRepository, recoveryService)
                registerAchievementRoutes(achievementsRepository)
            }
        }
    }
}

private fun ApplicationCall.getPrincipal(): FirebaseUserPrincipal = attributes[PrincipalKey]
private fun ApplicationCall.getUserRow(): UserRow = attributes[UserRowKey]

// Auth helper
private fun ApplicationCall.authenticate(
    tokenVerifier: FirebaseTokenVerifier?,
    config: AppConfig
): FirebaseUserPrincipal {
    if (config.devBypassAuth) {
        val uid = request.headers["X-DEV-UID"]?.takeIf { it.isNotBlank() } ?: "dev-user"
        return FirebaseUserPrincipal(uid = uid, email = "dev@example.com", name = "Dev")
    }

    val header = request.headers[HttpHeaders.Authorization]
        ?: throw IllegalArgumentException("Missing Authorization header")

    val parts = header.trim().split(" ", limit = 2)
    if (parts.size != 2 || !parts[0].equals("Bearer", ignoreCase = true)) {
        throw IllegalArgumentException("Invalid Authorization header (expected: Bearer <token>)")
    }

    val token = parts[1]
    return requireNotNull(tokenVerifier).verify(token)
}

// Expose helpers to routes
internal fun ApplicationCall.principal(): FirebaseUserPrincipal = getPrincipal()
internal fun ApplicationCall.userRow(): UserRow = getUserRow()

// Route wrapper with auth
private fun Route.routeAuth(
    tokenVerifier: FirebaseTokenVerifier?,
    config: AppConfig,
    userRepository: UserRepository,
    build: Route.() -> Unit
) {
    route("/") {
        intercept(ApplicationCallPipeline.Plugins) {
            val principal = call.authenticate(tokenVerifier, config)
            call.attributes.put(PrincipalKey, principal)

            val user = userRepository.getOrCreate(
                firebaseUid = principal.uid,
                email = principal.email,
                displayName = principal.name
            )
            call.attributes.put(UserRowKey, user)
        }
        build()
    }
}
