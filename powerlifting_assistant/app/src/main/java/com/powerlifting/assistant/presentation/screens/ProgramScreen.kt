package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerlifting.assistant.data.api.ProgramWorkoutDto
import com.powerlifting.assistant.presentation.viewmodel.ProgramViewModel

@Composable
fun ProgramScreen(vm: ProgramViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    val active = state.active

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Программа", style = MaterialTheme.typography.headlineSmall)
            Text("Тренировочный план от ваших ПМ", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(Modifier.height(16.dp))

            if (state.profileMissingMaxes) {
                ElevatedCard(shape = MaterialTheme.shapes.large) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Перед стартом нужно указать ПМ", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Заполните в профиле предельный максимум (1ПМ) в жиме, приседе и тяге.\n\n" +
                                "Как узнать 1ПМ:\n" +
                                "• Разминка\n" +
                                "• 3–5 подходов с ростом веса\n" +
                                "• Последний подход — 1 повтор с максимально возможным весом при чистой технике.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            when {
                state.loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                active == null -> {
                    Text("Не удалось загрузить данные.")
                }
                active.program == null -> {
                    ElevatedCard(shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("У вас нет активной программы.")
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { vm.generate() },
                                enabled = !state.profileMissingMaxes,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Text("Сгенерировать программу")
                            }
                            if (state.profileMissingMaxes) {
                                Spacer(Modifier.height(8.dp))
                                Text("Сначала заполните ПМ в профиле.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                else -> {
                    ElevatedCard(shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(active.program.name, style = MaterialTheme.typography.titleMedium)
                            Text("Старт: ${active.program.startDate} • ${active.program.weeks} нед.", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Если тренировок больше нет - показываем кнопку создания новой программы
                    if (active.upcomingWorkouts.isEmpty()) {
                        ElevatedCard(shape = MaterialTheme.shapes.large) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Программа завершена", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(6.dp))
                                Text("Все тренировки пройдены. Создайте новую программу!", style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = { vm.generate() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Text("Создать новую программу")
                                }
                            }
                        }
                    } else {
                        Text("Ближайшие тренировки", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                            items(active.upcomingWorkouts) { w ->
                                WorkoutCard(w)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { vm.generate() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text("Создать новую программу")
                        }
                    }
                }
            }

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun WorkoutCard(w: ProgramWorkoutDto) {
    ElevatedCard(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(w.title, style = MaterialTheme.typography.titleMedium)
            Text(w.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(10.dp))
            w.exercises.sortedBy { it.orderIndex }.forEach { ex ->
                val percent = ex.percent1rm?.let { " • ${String.format("%.0f", it * 100)}% 1ПМ" } ?: ""
                Text("• ${ex.exerciseName}: ${ex.sets}x${ex.reps}$percent", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
