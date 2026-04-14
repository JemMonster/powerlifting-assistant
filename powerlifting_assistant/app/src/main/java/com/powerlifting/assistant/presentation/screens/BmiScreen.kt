package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerlifting.assistant.presentation.viewmodel.BmiViewModel

@Composable
fun BmiScreen(vm: BmiViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    val bmi = remember(state.heightCm, state.weightKg) {
        val h = state.heightCm
        val w = state.weightKg
        if (h != null && w != null && h > 0) {
            val hm = h.toDouble() / 100.0
            w / (hm * hm)
        } else null
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Индекс массы тела (ИМТ)", style = MaterialTheme.typography.headlineSmall)
            Text("Рассчитывается по росту и весу из профиля.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(Modifier.height(16.dp))

            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val heightText = state.heightCm?.let { "$it см" } ?: "—"
                    val weightText = state.weightKg?.let { "${it} кг" } ?: "—"
                    Text("Рост: $heightText")
                    Text("Вес: $weightText")

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = bmi?.let { String.format("Ваш ИМТ: %.1f", it) } ?: "Заполните рост и вес в профиле",
                        style = MaterialTheme.typography.titleMedium
                    )

                    bmi?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("Категория: ${bmiCategory(it)}")
                    }
                }
            }

            if (state.loading) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun bmiCategory(bmi: Double): String = when {
    bmi < 18.5 -> "Недостаток массы"
    bmi < 25.0 -> "Норма"
    bmi < 30.0 -> "Избыточная масса"
    else -> "Ожирение"
}
