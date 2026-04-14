package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()

    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRegister) "Регистрация" else "Вход",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    error = null
                    loading = true
                    if (isRegister) {
                        auth.createUserWithEmailAndPassword(email.trim(), password)
                            .addOnSuccessListener { onAuthSuccess() }
                            .addOnFailureListener { e -> error = e.message }
                            .addOnCompleteListener { loading = false }
                    } else {
                        auth.signInWithEmailAndPassword(email.trim(), password)
                            .addOnSuccessListener { onAuthSuccess() }
                            .addOnFailureListener { e -> error = e.message }
                            .addOnCompleteListener { loading = false }
                    }
                },
                enabled = !loading && email.isNotBlank() && password.length >= 6,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                }
                Text(if (isRegister) "Создать аккаунт" else "Войти")
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = { isRegister = !isRegister }) {
                Text(if (isRegister) "Уже есть аккаунт? Войти" else "Нет аккаунта? Регистрация")
            }
        }
    }
}
