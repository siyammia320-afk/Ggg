package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isLoginMode by remember { mutableStateOf(true) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var telegramUsername by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)), // Warm slate off-white canvas
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFDBEAFE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "ARAFAT FB CREATOR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isLoginMode) "অ্যাপ ব্যবহার করতে আপনার অ্যাকাউন্টে লগইন করুন" else "নতুন অ্যাকাউন্ট তৈরি করতে তথ্যগুলো পূরণ করুন",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Auth Error/Success banner
            if (uiState.authError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626))
                        Text(
                            text = uiState.authError ?: "",
                            color = Color(0xFF991B1B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (uiState.authSuccess != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                        Text(
                            text = uiState.authSuccess ?: "",
                            color = Color(0xFF166534),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Input Fields Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (!isLoginMode) {
                        // First Name
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name (প্রথম নাম)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        // Last Name
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name (শেষ নাম)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        // Telegram Username
                        OutlinedTextField(
                            value = telegramUsername,
                            onValueChange = { telegramUsername = it },
                            label = { Text("Telegram Username") },
                            placeholder = { Text("e.g. username") },
                            leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF64748B)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                    }

                    // Email Address
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (ইমেইল)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64748B)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (পাসওয়ার্ড)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }

            // Action Button
            Button(
                onClick = {
                    viewModel.clearAuthMessages()
                    if (isLoginMode) {
                        viewModel.logInUser(email, password) { _, _ -> }
                    } else {
                        viewModel.signUpUser(firstName, lastName, telegramUsername, email, password) { success, _ ->
                            if (success) {
                                isLoginMode = true
                            }
                        }
                    }
                },
                enabled = !uiState.isAuthLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.isAuthLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isLoginMode) "Log In (লগইন করুন)" else "Sign Up (নিবন্ধন করুন)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Mode Toggle Text
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isLoginMode) "নতুন অ্যাকাউন্ট তৈরি করতে চান?" else "ইতিমধ্যে অ্যাকাউন্ট তৈরি করা আছে?",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
                TextButton(
                    onClick = {
                        isLoginMode = !isLoginMode
                        viewModel.clearAuthMessages()
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isLoginMode) "Sign Up (নিবন্ধন)" else "Log In (লগইন)",
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
