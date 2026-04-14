package com.powerlifting.assistant.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.powerlifting.assistant.presentation.screens.*

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

// Routes that should hide the bottom bar (full-screen experiences)
private val fullScreenRoutes = setOf("workout/{sessionId}", "recovery")

@Composable
fun MainScaffold(onLogout: () -> Unit) {
    val navController = rememberNavController()

    val items = listOf(
        BottomItem("calculator", "Калькул.", { Icon(Icons.Default.Calculate, contentDescription = null) }),
        BottomItem("history", "История", { Icon(Icons.Default.History, contentDescription = null) }),
        BottomItem("home", "Главная", { Icon(Icons.Default.Home, contentDescription = null) }),
        BottomItem("help", "Помощь", { Icon(Icons.Default.Help, contentDescription = null) }),
        BottomItem("profile", "Профиль", { Icon(Icons.Default.Person, contentDescription = null) }),
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute !in fullScreenRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                    items.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = item.icon,
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding: PaddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onOpenCalories = { navController.navigate("calories") },
                    onOpenBmi = { navController.navigate("bmi") },
                    onOpenProgram = { navController.navigate("program") },
                    onOpenRecovery = { navController.navigate("recovery") },
                    onOpenAchievements = { navController.navigate("achievements") },
                    onStartWorkout = { navController.navigate("recovery") }
                )
            }
            composable("calculator") { CalculatorScreen() }
            composable("history") { WorkoutHistoryScreen() }
            composable("help") { HelpScreen() }
            composable("profile") {
                ProfileScreen(
                    onLogout = onLogout,
                    onOpenProgram = { navController.navigate("program") },
                    onOpenAchievements = { navController.navigate("achievements") },
                    onOpenCaloriesTracker = { navController.navigate("calories") }
                )
            }

            composable("calories") { CaloriesTrackerScreen() }
            composable("bmi") { BmiScreen() }
            composable("program") { ProgramScreen() }
            composable("recovery") {
                RecoveryScreen(
                    onProceedToWorkout = { sessionId ->
                        navController.navigate("workout/$sessionId")
                    }
                )
            }
            composable("achievements") { AchievementsScreen() }
            composable("workout/{sessionId}") { backStack ->
                val sessionId = backStack.arguments?.getString("sessionId") ?: ""
                WorkoutScreen(
                    sessionId = sessionId,
                    title = "Тренировка",
                    onFinish = { navController.popBackStack("home", inclusive = false) }
                )
            }
        }
    }
}
