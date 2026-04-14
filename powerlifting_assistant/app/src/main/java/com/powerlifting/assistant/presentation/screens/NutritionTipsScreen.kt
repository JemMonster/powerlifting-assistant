package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NutritionTipsScreen(onOpenCaloriesTracker: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Питание и рацион", style = MaterialTheme.typography.headlineSmall)
            Text("Советы по спортивному питанию и рациону", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(Modifier.height(16.dp))

            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Креатин", style = MaterialTheme.typography.titleMedium)
                    Text("Увеличивает силу и мышечную массу", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    Text("Дозировка: 5 г в день")
                    Text("Время приёма: в любое время дня")
                }
            }

            Spacer(Modifier.height(12.dp))

            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Протеин", style = MaterialTheme.typography.titleMedium)
                    Text("Восстановление мышц и рост мышечной массы", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    Text("Дозировка: 1–2 порции в день")
                    Text("Время приёма: после тренировки или между приёмами пищи")
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Как проще закрыть белок", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("• Куриная грудка, тунец, творог, яйца")
            Text("• Греческий йогурт и молочные продукты")
            Text("• Протеиновый коктейль как «добивка»")

            Spacer(Modifier.height(20.dp))

            Button(onClick = onOpenCaloriesTracker, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Text("Открыть дневник питания")
            }
        }
    }
}
