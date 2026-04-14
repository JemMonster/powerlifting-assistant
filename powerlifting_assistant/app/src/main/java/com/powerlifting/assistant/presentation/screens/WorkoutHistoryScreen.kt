package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerlifting.assistant.data.api.WorkoutHistoryItemDto
import com.powerlifting.assistant.presentation.viewmodel.WorkoutHistoryViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutHistoryScreen(vm: WorkoutHistoryViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("История тренировок", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.height(4.dp))
            Text("Ваши завершённые тренировки", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(Modifier.height(16.dp))

            when {
                state.loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                state.error != null -> {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { vm.load() }) {
                        Text("Повторить")
                    }
                }
                state.sessions.isEmpty() -> {
                    Spacer(Modifier.height(32.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Пока нет завершённых тренировок",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.sessions) { session ->
                            HistoryCard(session)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(session: WorkoutHistoryItemDto) {
    val dateStr = try {
        val instant = Instant.parse(session.date)
        val local = instant.atZone(ZoneId.systemDefault())
        local.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    } catch (_: Exception) {
        session.date
    }

    val durationStr = session.durationSec?.let { sec ->
        val h = sec / 3600
        val m = (sec % 3600) / 60
        if (h > 0) "${h}ч ${m}мин" else "${m} мин"
    } ?: "—"

    val wellbeingEmoji = when (session.wellbeingRating) {
        1 -> "😫"
        2 -> "😟"
        3 -> "😐"
        4 -> "😊"
        5 -> "💪"
        else -> null
    }

    ElevatedCard(shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    session.workoutTitle ?: "Тренировка",
                    style = MaterialTheme.typography.titleMedium
                )
                wellbeingEmoji?.let { Text(it) }
            }

            Spacer(Modifier.height(4.dp))
            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Длительность", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(durationStr, style = MaterialTheme.typography.bodyMedium)
                }
                Column {
                    Text("Подходы", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("${session.setsCount}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
