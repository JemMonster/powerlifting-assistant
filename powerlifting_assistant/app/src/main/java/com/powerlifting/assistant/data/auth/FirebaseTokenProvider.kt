package com.powerlifting.assistant.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseTokenProvider {
    suspend fun bearerToken(forceRefresh: Boolean = false): String {
        val user = FirebaseAuth.getInstance().currentUser
            ?: error("User is not authenticated")

        val token = user.getIdToken(forceRefresh).await().token
            ?: error("Firebase token is null")

        return "Bearer $token"
    }
}
