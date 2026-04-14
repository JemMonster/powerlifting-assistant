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
import com.powerlifting.assistant.data.api.AchievementDto
import com.powerlifting.assistant.presentation.viewmodel.AchievementsViewModel

@Composable
fun AchievementsScreen(vm: AchievementsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    var showDialog by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Достижения", style = MaterialTheme.typography.headlineSmall)
                    Text("Записывайте личные рекорды", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(state.items) { a ->
                    AchievementCard(a, onDelete = { vm.delete(a.id) })
                }
            }

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "MVP: фото можно хранить в Firebase Storage и сохранять URL в достижение.\n" +
                    "В проекте уже подключён firebase-storage-ktx — можно добавить выбор изображения и загрузку.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        if (showDialog) {
            AddAchievementDialog(
                onDismiss = { showDialog = false },
                onAdd = { note ->
                    vm.add(note)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
private fun AchievementCard(a: AchievementDto, onDelete: () -> Unit) {
    ElevatedCard(shape = MaterialTheme.shapes.large) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(a.note, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(a.createdAtIso, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}

@Composable
private fun AddAchievementDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onAdd(note.trim()) }, enabled = note.isNotBlank()) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("Новое достижение") },
        text = {
            Column {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Что получилось?") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
