package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerlifting.assistant.presentation.viewmodel.HomeViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun HomeScreen(
    onOpenCalories: () -> Unit,
    onOpenBmi: () -> Unit,
    onOpenProgram: () -> Unit,
    onOpenRecovery: () -> Unit,
    onOpenAchievements: () -> Unit,
    onStartWorkout: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    val profile = state.profile
    val goals = profile?.nutritionGoals
    val stats = profile?.stats

    val height = profile?.profile?.heightCm
    val weight = profile?.profile?.weightKg
    val bmi = if (height != null && weight != null && height > 0) {
        val hM = height.toDouble() / 100.0
        weight / (hM * hM)
    } else null

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            Text("Привет, Спортсмен!", style = MaterialTheme.typography.headlineMedium)
            Text("Готов к новым достижениям?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Калории",
                    value = if (stats != null && goals != null) "${stats.caloriesToday}/${goals.caloriesGoal}" else "—",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenCalories
                )
                StatCard(
                    title = "ИМТ",
                    value = bmi?.let { String.format("%.1f", it) } ?: "—",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenBmi
                )
                StatCard(
                    title = "Достижения",
                    value = stats?.achievementsCount?.toString() ?: "—",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAchievements
                )
            }

            Spacer(Modifier.height(16.dp))

            CalendarCard(
                calendarDays = state.calendar?.days.orEmpty(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onStartWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Начать тренировку", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SmallActionCard(title = "Программа", subtitle = "Тренировочный план", modifier = Modifier.weight(1f), onClick = onOpenProgram)
                SmallActionCard(title = "Восстановление", subtitle = "Советы и опросник", modifier = Modifier.weight(1f), onClick = onOpenRecovery)
            }

            if (state.loading) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.error?.let {
                Spacer(Modifier.height(12.dp))
                ElevatedCard(shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { vm.refresh() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SmallActionCard(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedCard(modifier = modifier, onClick = onClick, shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun CalendarCard(calendarDays: List<com.powerlifting.assistant.data.api.CalendarDayDto>, modifier: Modifier = Modifier) {
    val today = LocalDate.now(ZoneId.systemDefault())
    val month = YearMonth.of(today.year, today.month)

    val map = calendarDays.associateBy { LocalDate.parse(it.date) }

    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Календарь тренировок", style = MaterialTheme.typography.titleMedium)
                Text("${today.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${today.year}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(10.dp))

            val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekDays.forEach {
                    Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(8.dp))

            val cells = buildMonthCells(month)

            // Fixed-height grid (6 rows) so it works inside scrollable Column
            val rows = cells.chunked(7)
            rows.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    week.forEach { date ->
                        Box(modifier = Modifier.weight(1f)) {
                            if (date == null) {
                                Spacer(modifier = Modifier.aspectRatio(1f).fillMaxWidth())
                            } else {
                                val info = map[date]
                                val isToday = date == today
                                val isTraining = info != null

                                val bg = when {
                                    isToday -> MaterialTheme.colorScheme.primary
                                    isTraining -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                                    else -> Color.Transparent
                                }

                                val borderColor = when {
                                    isTraining -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                }

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bg)
                                        .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    // Pad remaining cells if week has fewer than 7
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendDot(color = MaterialTheme.colorScheme.tertiary)
                Text("Тренировка", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(12.dp))
                LegendDot(color = MaterialTheme.colorScheme.primary)
                Text("Сегодня", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
    )
}

private fun buildMonthCells(month: YearMonth): List<LocalDate?> {
    val first = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()

    // Convert to Monday-first index: Monday=0 ... Sunday=6
    val firstDow = first.dayOfWeek
    val offset = when (firstDow) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }

    val totalCells = 42 // 6 weeks grid
    val result = MutableList<LocalDate?>(totalCells) { null }

    var day = 1
    for (i in offset until offset + daysInMonth) {
        result[i] = month.atDay(day)
        day++
    }
    return result
}
