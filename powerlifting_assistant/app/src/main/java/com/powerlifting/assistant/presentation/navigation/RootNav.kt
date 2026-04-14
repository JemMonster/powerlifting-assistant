package com.powerlifting.assistant.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.powerlifting.assistant.presentation.screens.AuthScreen

@Composable
fun RootNav() {
    val navController = rememberNavController()

    var startDestination by remember { mutableStateOf("auth") }

    LaunchedEffect(Unit) {
        startDestination = if (FirebaseAuth.getInstance().currentUser != null) "main" else "auth"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScaffold(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}
