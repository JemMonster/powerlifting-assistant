package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerlifting.assistant.data.api.UpdateProfileRequest
import com.powerlifting.assistant.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onOpenProgram: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenCaloriesTracker: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    val p = state.profile?.profile

    var height by remember(p?.heightCm) { mutableStateOf(p?.heightCm?.toString() ?: "") }
    var weight by remember(p?.weightKg) { mutableStateOf(p?.weightKg?.toString() ?: "") }
    var bench by remember(p?.bench1rm) { mutableStateOf(p?.bench1rm?.toString() ?: "") }
    var squat by remember(p?.squat1rm) { mutableStateOf(p?.squat1rm?.toString() ?: "") }
    var deadlift by remember(p?.deadlift1rm) { mutableStateOf(p?.deadlift1rm?.toString() ?: "") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Профиль", style = MaterialTheme.typography.headlineSmall)
            Text("Данные для расчётов и программы", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(Modifier.height(16.dp))

            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Рост (см)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it.replace(',', '.').filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Вес (кг)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider()

                    Text("1ПМ (кг)", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = bench,
                        onValueChange = { bench = it.replace(',', '.').filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Жим лёжа") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = squat,
                        onValueChange = { squat = it.replace(',', '.').filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Присед") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deadlift,
                        onValueChange = { deadlift = it.replace(',', '.').filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Становая тяга") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            vm.save(
                                UpdateProfileRequest(
                                    heightCm = height.toIntOrNull(),
                                    weightKg = weight.toDoubleOrNull(),
                                    bench1rm = bench.toDoubleOrNull(),
                                    squat1rm = squat.toDoubleOrNull(),
                                    deadlift1rm = deadlift.toDoubleOrNull()
                                )
                            )
                        },
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Сохранить")
                    }

                    if (state.saved) {
                        Text("Сохранено", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Быстрые действия", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onOpenProgram, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                        Text("Открыть программу")
                    }
                    Button(onClick = onOpenCaloriesTracker, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                        Text("Открыть дневник питания")
                    }
                    Button(onClick = onOpenAchievements, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                        Text("Открыть достижения")
                    }

                    Divider()

                    OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                        Text("Выйти")
                    }
                }
            }

            if (state.loading) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
