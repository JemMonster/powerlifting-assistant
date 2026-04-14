package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerlifting.assistant.presentation.viewmodel.*

@Composable
fun WorkoutScreen(
    sessionId: String,
    title: String,
    onFinish: () -> Unit,
    vm: WorkoutViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(sessionId) {
        vm.loadWorkout(sessionId)
    }

    LaunchedEffect(state.phase) {
        if (state.phase == WorkoutPhase.FINISHED) onFinish()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (state.phase) {
            WorkoutPhase.LOADING -> LoadingPhase(state)
            WorkoutPhase.WARMUP -> WarmupPhase(state, vm)
            WorkoutPhase.EXERCISE -> ExercisePhase(state, vm)
            WorkoutPhase.REST -> RestPhase(state, vm)
            WorkoutPhase.ACCESSORIES -> AccessoriesPhase(state, vm)
            WorkoutPhase.FINISH_RATING -> FinishRatingPhase(state, vm)
            WorkoutPhase.FINISHED -> {} // handled by LaunchedEffect
        }
    }
}

@Composable
private fun LoadingPhase(state: WorkoutUiState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.error != null) {
            Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Text(state.error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        } else {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("Загрузка тренировки...")
        }
    }
}

@Composable
private fun WarmupPhase(state: WorkoutUiState, vm: WorkoutViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Разминка", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        // Total timer
        TotalTimerBar(state.totalTimerSec)

        Spacer(Modifier.height(16.dp))

        state.recommendation?.let {
            ElevatedCard(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Рекомендация", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        ElevatedCard(shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Рекомендации по разминке", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                WarmupItem("5–10 минут лёгкого кардио (велотренажёр, ходьба)")
                WarmupItem("Динамическая растяжка: махи ногами, руками")
                WarmupItem("Разминочные подходы с пустым грифом (2–3 по 10–15)")
                WarmupItem("Постепенное повышение веса до рабочего")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Preview upcoming exercises
        if (state.mainExercises.isNotEmpty()) {
            Text("Сегодня в программе:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            state.mainExercises.forEach { ex ->
                val totalSets = ex.setGroups.sumOf { it.totalSets }
                Text("• ${ex.name} — $totalSets подходов", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { vm.warmupDone() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Разминка выполнена", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun WarmupItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text("•", modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ExercisePhase(state: WorkoutUiState, vm: WorkoutViewModel) {
    val exercise = state.currentExercise ?: return
    val setGroup = state.currentSetGroup ?: return

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        // Header with total timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(exercise.name, style = MaterialTheme.typography.headlineSmall)
            TotalTimerChip(state.totalTimerSec)
        }

        Spacer(Modifier.height(4.dp))
        ProgressBar(state.completedMainSets, state.totalMainSets)

        Spacer(Modifier.height(16.dp))

        // Exercise icon based on lift type
        ElevatedCard(shape = RoundedCornerShape(14.dp)) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val icon = when (exercise.liftType) {
                    "squat" -> Icons.Default.FitnessCenter
                    "bench" -> Icons.Default.FitnessCenter
                    "deadlift" -> Icons.Default.FitnessCenter
                    else -> Icons.Default.SportsGymnastics
                }
                Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)

                Spacer(Modifier.height(12.dp))

                // Weight display
                val weightText = setGroup.weightKg?.let { "${it.toCleanString()} кг" } ?: "Свободный вес"
                Text(weightText, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

                setGroup.percent1rm?.let {
                    Text("${(it * 100).toInt()}% от 1ПМ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }

                Spacer(Modifier.height(8.dp))
                Text("${setGroup.targetReps} повторений", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Set circles for current set group
        Text(
            "Подходы (${setGroup.completedSets}/${setGroup.totalSets})",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        SetCirclesRow(completed = setGroup.completedSets, total = setGroup.totalSets)

        // All set groups for this exercise
        if (exercise.setGroups.size > 1) {
            Spacer(Modifier.height(12.dp))
            Text("Все серии:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            exercise.setGroups.forEachIndexed { idx, sg ->
                val isCurrent = idx == state.currentSetGroupIndex
                val color = when {
                    isCurrent -> MaterialTheme.colorScheme.primary
                    sg.allCompleted -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
                val w = sg.weightKg?.let { "${it.toCleanString()} кг" } ?: ""
                Text(
                    "${sg.completedSets}/${sg.totalSets} × ${sg.targetReps} повт. $w",
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Exercise timer
        if (state.isExerciseTimerRunning) {
            ElevatedCard(shape = RoundedCornerShape(14.dp)) {
                Column(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Выполнение", style = MaterialTheme.typography.labelMedium)
                    Text(formatTime(state.exerciseTimerSec), style = MaterialTheme.typography.headlineMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Action buttons
        if (!state.isExerciseTimerRunning) {
            Button(
                onClick = { vm.startSet() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Начать подход", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Button(
                onClick = { vm.completeSet() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Подход выполнен", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun RestPhase(state: WorkoutUiState, vm: WorkoutViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Отдых", style = MaterialTheme.typography.headlineMedium)
            TotalTimerChip(state.totalTimerSec)
        }

        Spacer(Modifier.height(4.dp))
        ProgressBar(state.completedMainSets, state.totalMainSets)

        Spacer(Modifier.height(32.dp))

        // Rest timer display
        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(8.dp))
        Text(formatTime(state.restTimerSec), style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))

        // Recommended rest time
        val recommendedRest = if (state.currentSetGroup?.percent1rm != null && state.currentSetGroup!!.percent1rm!! > 0.75) {
            "3–5 минут (тяжёлые подходы)"
        } else {
            "2–3 минуты"
        }
        Text("Рекомендуется: $recommendedRest", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

        Spacer(Modifier.height(12.dp))

        // Show what's next
        state.currentExercise?.let { ex ->
            val sg = state.currentSetGroup
            if (sg != null && !sg.allCompleted) {
                val w = sg.weightKg?.let { "${it.toCleanString()} кг" } ?: ""
                Text("Следующий: ${ex.name} — ${sg.targetReps} повт. $w", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = { vm.skipRest() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.SkipNext, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Следующий подход", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun AccessoriesPhase(state: WorkoutUiState, vm: WorkoutViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Подсобные упражнения", style = MaterialTheme.typography.headlineSmall)
            TotalTimerChip(state.totalTimerSec)
        }

        Spacer(Modifier.height(4.dp))
        Text("Основные упражнения выполнены!", color = MaterialTheme.colorScheme.tertiary)

        Spacer(Modifier.height(16.dp))

        if (state.accessoryExercises.isNotEmpty()) {
            Text("Рекомендуемые подсобные:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.accessoryExercises) { ex ->
                    ElevatedCard(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(ex.name, style = MaterialTheme.typography.titleMedium)
                            ex.setGroups.forEach { sg ->
                                Text(
                                    "${sg.totalSets} × ${sg.targetReps} повт.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Text("Нет подсобных упражнений в программе.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { vm.proceedToFinish() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.DoneAll, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Закончить тренировку", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun FinishRatingPhase(state: WorkoutUiState, vm: WorkoutViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Тренировка завершена!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Общее время: ${formatTime(state.totalTimerSec)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Выполнено подходов: ${state.completedMainSets}/${state.totalMainSets}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        ElevatedCard(shape = RoundedCornerShape(14.dp)) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Оцените самочувствие", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val emojis = listOf("😫", "😟", "😐", "😊", "💪")
                    val labels = listOf("Ужасно", "Плохо", "Норм", "Хорошо", "Отлично")
                    emojis.forEachIndexed { idx, emoji ->
                        val rating = idx + 1
                        val selected = state.wellbeingRating == rating
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FilterChip(
                                selected = selected,
                                onClick = { vm.setWellbeingRating(rating) },
                                label = { Text(emoji, fontSize = 20.sp) }
                            )
                            Text(
                                labels[idx],
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { vm.finishWorkout() },
            enabled = !state.isFinishing,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isFinishing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
            }
            Text("Сохранить результат", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// ═══════════════ Utility Composables ═══════════════

@Composable
private fun TotalTimerBar(seconds: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Spacer(Modifier.width(6.dp))
        Text(formatTime(seconds), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun TotalTimerChip(seconds: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(formatTime(seconds), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ProgressBar(completed: Int, total: Int) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    Column {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "$completed/$total подходов",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SetCirclesRow(completed: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        for (i in 0 until total) {
            val isDone = i < completed
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDone) MaterialTheme.colorScheme.tertiary else Color.Transparent)
                    .border(2.dp, if (isDone) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("${i + 1}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private fun formatTime(totalSec: Int): String {
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun Double.toCleanString(): String {
    return if (this == Math.floor(this)) "%.0f".format(this)
    else "%.1f".format(this)
}
