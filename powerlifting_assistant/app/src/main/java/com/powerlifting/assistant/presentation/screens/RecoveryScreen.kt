package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerlifting.assistant.presentation.viewmodel.RecoveryViewModel

@Composable
fun RecoveryScreen(
    onProceedToWorkout: (sessionId: String) -> Unit,
    vm: RecoveryViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    var sleep by remember { mutableStateOf(8f) }
    var wellbeing by remember { mutableStateOf(7f) }
    var fatigue by remember { mutableStateOf(5f) }
    var soreness by remember { mutableStateOf(5f) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Восстановление", style = MaterialTheme.typography.headlineSmall)
            Text("Оцените самочувствие перед тренировкой", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(Modifier.height(16.dp))

            state.plannedWorkout?.let { w ->
                ElevatedCard(shape = MaterialTheme.shapes.large) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("План: ${w.title}", style = MaterialTheme.typography.titleMedium)
                        Text("Дата: ${w.date}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    MetricSlider(
                        title = "Сон (часы)",
                        value = sleep,
                        valueLabel = String.format("%.1f ч", sleep),
                        range = 0f..12f,
                        steps = 23,
                        onChange = { sleep = it }
                    )
                    MetricSlider(
                        title = "Самочувствие (1–10)",
                        value = wellbeing,
                        valueLabel = wellbeing.toInt().toString(),
                        range = 1f..10f,
                        steps = 8,
                        onChange = { wellbeing = it }
                    )
                    MetricSlider(
                        title = "Усталость (1–10)",
                        value = fatigue,
                        valueLabel = fatigue.toInt().toString(),
                        range = 1f..10f,
                        steps = 8,
                        onChange = { fatigue = it }
                    )
                    MetricSlider(
                        title = "Боль в мышцах (1–10)",
                        value = soreness,
                        valueLabel = soreness.toInt().toString(),
                        range = 1f..10f,
                        steps = 8,
                        onChange = { soreness = it }
                    )

                    Button(
                        onClick = {
                            vm.startSession(
                                sleepHours = sleep.toDouble(),
                                wellbeing = wellbeing.toInt(),
                                fatigue = fatigue.toInt(),
                                soreness = soreness.toInt()
                            )
                        },
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Получить рекомендацию")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            state.recommendation?.let {
                ElevatedCard(shape = MaterialTheme.shapes.large) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Рекомендация", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(it)

                        state.startedSessionId?.let { sid ->
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { onProceedToWorkout(sid) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Text("Перейти к тренировке")
                            }
                        }
                    }
                }
            }

            if (state.loading) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun MetricSlider(
    title: String,
    value: Float,
    valueLabel: String,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title)
            Text(valueLabel, style = MaterialTheme.typography.labelMedium)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            steps = steps
        )
    }
}
