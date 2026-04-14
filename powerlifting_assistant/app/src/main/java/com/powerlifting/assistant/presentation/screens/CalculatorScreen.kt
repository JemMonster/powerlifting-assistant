package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CalculatorScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Калькулятор веса штанги", style = MaterialTheme.typography.headlineSmall)
            Text("Рассчитайте нужные блины для штанги", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(Modifier.height(16.dp))

            var barWeight by remember { mutableStateOf(20) }
            var targetText by remember { mutableStateOf("") }

            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Вес грифа (кг)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(20, 15, 10).forEach { w ->
                            FilterChip(
                                selected = barWeight == w,
                                onClick = { barWeight = w },
                                label = { Text("$w кг") }
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it.replace(',', '.').filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Желаемый вес штанги (кг)") },
                        placeholder = { Text("Например: 180") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(14.dp))

                    val target = targetText.toDoubleOrNull()
                    val result = remember(barWeight, target) {
                        if (target == null) null else plateSolution(barWeight.toDouble(), target)
                    }

                    Button(
                        onClick = { /* recalculated via state */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = target != null,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Рассчитать")
                    }

                    Spacer(Modifier.height(14.dp))

                    Text("Доступные блины:")
                    Text("25, 20, 15, 10, 5, 2.5, 1.25 (кг)", style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(12.dp))

                    when {
                        target == null -> Text("Введите желаемый вес.")
                        target > MAX_TARGET_WEIGHT -> Text("Максимальный вес — ${"%.0f".format(MAX_TARGET_WEIGHT)} кг.", color = MaterialTheme.colorScheme.error)
                        result == null -> Text("")
                        result.possible -> {
                            Text("На каждую сторону:", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(result.plates.joinToString(" + ") { "${it}кг" })
                            Spacer(Modifier.height(6.dp))
                            Text("Итого на сторону: ${String.format("%.2f", result.perSide)} кг")
                        }
                        else -> {
                            Text("Невозможно собрать точно этот вес с указанными блинами.", color = MaterialTheme.colorScheme.error)
                            result.leftover?.let { lo ->
                                Text("Остаток на сторону: ${String.format("%.2f", lo)} кг", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PlateResult(
    val possible: Boolean,
    val plates: List<Double> = emptyList(),
    val perSide: Double = 0.0,
    val leftover: Double? = null
)

private const val MAX_TARGET_WEIGHT = 600.0

private fun plateSolution(bar: Double, target: Double): PlateResult {
    if (target < bar) return PlateResult(false, leftover = (bar - target) / 2.0)
    if (target > MAX_TARGET_WEIGHT) return PlateResult(false, leftover = null)

    val needTotal = target - bar
    val perSide = needTotal / 2.0

    val available = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

    var remaining = perSide
    val plates = mutableListOf<Double>()

    for (p in available) {
        val count = ((remaining + 1e-9) / p).toInt()
        repeat(count) {
            plates.add(p)
        }
        remaining -= p * count
    }

    val possible = kotlin.math.abs(remaining) < 1e-6

    return if (possible) {
        PlateResult(true, plates = plates, perSide = perSide)
    } else {
        PlateResult(false, plates = plates, perSide = perSide, leftover = remaining)
    }
}
