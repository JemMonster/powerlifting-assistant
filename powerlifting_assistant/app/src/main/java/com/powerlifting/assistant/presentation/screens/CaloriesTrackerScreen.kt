package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerlifting.assistant.data.api.NutritionEntryDto
import com.powerlifting.assistant.presentation.viewmodel.CaloriesViewModel

@Composable
fun CaloriesTrackerScreen(vm: CaloriesViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    val data = state.data

    var showAddDialog by remember { mutableStateOf(false) }
    var showGoalsDialog by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Счётчик калорий", style = MaterialTheme.typography.headlineSmall)
            Text("Отслеживайте калории и белок", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(Modifier.height(16.dp))

            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val caloriesGoal = data?.goals?.caloriesGoal ?: 2500
                    val proteinGoal = data?.goals?.proteinGoalG ?: 150
                    val calories = data?.totals?.calories ?: 0
                    val protein = data?.totals?.proteinG ?: 0

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Цели")
                        TextButton(onClick = { showGoalsDialog = true }) { Text("Изменить") }
                    }

                    Text("Калории: $caloriesGoal ккал")
                    Text("Белок: $proteinGoal г")

                    Spacer(Modifier.height(12.dp))

                    Text("Калории")
                    LinearProgressIndicator(
                        progress = (calories.toFloat() / caloriesGoal.toFloat()).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("$calories / $caloriesGoal ккал", style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(12.dp))

                    Text("Белок")
                    LinearProgressIndicator(
                        progress = (protein.toFloat() / proteinGoal.toFloat()).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("$protein / $proteinGoal г", style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить приём пищи")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Сегодня съедено:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (data == null) {
                if (state.loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(data.entries) { entry ->
                        MealItem(entry = entry, onDelete = { vm.deleteEntry(entry) })
                    }
                }
            }

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }

        if (showAddDialog) {
            AddMealDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, calories, protein ->
                    vm.addEntry(title, calories, protein)
                    showAddDialog = false
                }
            )
        }

        if (showGoalsDialog) {
            val currentC = data?.goals?.caloriesGoal ?: 2500
            val currentP = data?.goals?.proteinGoalG ?: 150
            UpdateGoalsDialog(
                initialCalories = currentC,
                initialProtein = currentP,
                onDismiss = { showGoalsDialog = false },
                onSave = { c, p ->
                    vm.updateGoals(c, p)
                    showGoalsDialog = false
                }
            )
        }
    }
}

@Composable
private fun MealItem(entry: NutritionEntryDto, onDelete: () -> Unit) {
    ElevatedCard(shape = MaterialTheme.shapes.large) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("${entry.calories} ккал • ${entry.proteinG} г белка", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}

@Composable
private fun AddMealDialog(onDismiss: () -> Unit, onAdd: (String, Int, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val c = calories.toIntOrNull() ?: 0
                    val p = protein.toIntOrNull() ?: 0
                    onAdd(title.trim(), c, p)
                },
                enabled = title.isNotBlank()
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("Новый приём пищи") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название") }, singleLine = true)
                OutlinedTextField(value = calories, onValueChange = { calories = it.filter { ch -> ch.isDigit() } }, label = { Text("Калории") }, singleLine = true)
                OutlinedTextField(value = protein, onValueChange = { protein = it.filter { ch -> ch.isDigit() } }, label = { Text("Белок (г)") }, singleLine = true)
            }
        }
    )
}

@Composable
private fun UpdateGoalsDialog(
    initialCalories: Int,
    initialProtein: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var calories by remember { mutableStateOf(initialCalories.toString()) }
    var protein by remember { mutableStateOf(initialProtein.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val c = calories.toIntOrNull()?.coerceIn(0, 10000) ?: initialCalories
                    val p = protein.toIntOrNull()?.coerceIn(0, 500) ?: initialProtein
                    onSave(c, p)
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("Цели на день") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = calories, onValueChange = { calories = it.filter { ch -> ch.isDigit() } }, label = { Text("Калории") }, singleLine = true)
                OutlinedTextField(value = protein, onValueChange = { protein = it.filter { ch -> ch.isDigit() } }, label = { Text("Белок (г)") }, singleLine = true)
            }
        }
    )
}
