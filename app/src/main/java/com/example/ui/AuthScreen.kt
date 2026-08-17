package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0 = Login, 1 = Signup

    // Login Form State
    var loginEmail by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var loginPasswordVisible by rememberSaveable { mutableStateOf(false) }

    // Signup Form State
    var signupFirstName by rememberSaveable { mutableStateOf("") }
    var signupLastName by rememberSaveable { mutableStateOf("") }
    var signupTelegram by rememberSaveable { mutableStateOf("") }
    var signupEmail by rememberSaveable { mutableStateOf("") }
    var signupPassword by rememberSaveable { mutableStateOf("") }
    var signupPasswordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFDCE9F0))
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Card (Design specs from user prompt)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0x331877F2)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FC)),
                border = BorderStroke(1.dp, Color(0xFFBED3E0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with Circular Facebook Logo & FB TOOLS Title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFBED3E0), CircleShape)
                                .shadow(2.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "f",
                                color = Color(0xFF1877F2),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "FB ",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A3F52)
                            )
                            Text(
                                text = "TOOLS",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1877F2)
                            )
                        }

                        Text(
                            text = "Facebook Automation & Account Suite",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4A6B82),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Rounded Pill Tab Switcher
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(Color(0xFFD4E3ED))
                            .padding(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Login Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (selectedTab == 0) Color.White else Color.Transparent)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        viewModel.clearAuthMessages()
                                        selectedTab = 0
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (selectedTab == 0) Color(0xFF1877F2) else Color(0xFF6E8E9E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Login",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (selectedTab == 0) Color(0xFF1A3F52) else Color(0xFF6E8E9E)
                                    )
                                }
                            }

                            // Sign Up Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (selectedTab == 1) Color.White else Color.Transparent)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        viewModel.clearAuthMessages()
                                        selectedTab = 1
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = if (selectedTab == 1) Color(0xFF1877F2) else Color(0xFF6E8E9E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Sign Up",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (selectedTab == 1) Color(0xFF1A3F52) else Color(0xFF6E8E9E)
                                    )
                                }
                            }
                        }
                    }

                    // Alert Messages (Error / Success)
                    AnimatedVisibility(
                        visible = uiState.authError != null || uiState.authSuccess != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (uiState.authError != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .border(1.dp, Color(0xFFF87171), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("⚠️", fontSize = 14.sp)
                                    Text(
                                        text = uiState.authError ?: "",
                                        color = Color(0xFF991B1B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearAuthMessages() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF991B1B), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        } else if (uiState.authSuccess != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .border(1.dp, Color(0xFF4ADE80), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("✅", fontSize = 14.sp)
                                    Text(
                                        text = uiState.authSuccess ?: "",
                                        color = Color(0xFF166534),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearAuthMessages() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF166534), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Forms
                    if (selectedTab == 0) {
                        // ----------------- LOGIN FORM -----------------
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Email Field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "Email Address",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2C4A5E)
                                    )
                                }
                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = { loginEmail = it },
                                    placeholder = { Text("you@example.com", color = Color(0xFF8FA9BA), fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(24.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xFF1877F2),
                                        unfocusedBorderColor = Color(0xFFBED3E0),
                                        focusedTextColor = Color(0xFF1A3F52),
                                        unfocusedTextColor = Color(0xFF1A3F52)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Password Field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "Password",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2C4A5E)
                                    )
                                }
                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it },
                                    placeholder = { Text("••••••••", color = Color(0xFF8FA9BA), fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(24.dp),
                                    visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            if (loginEmail.isNotBlank() && loginPassword.isNotBlank()) {
                                                viewModel.logInUser(loginEmail.trim(), loginPassword) { _, _ -> }
                                            }
                                        }
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                            Icon(
                                                imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password",
                                                tint = Color(0xFF6E8E9E),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xFF1877F2),
                                        unfocusedBorderColor = Color(0xFFBED3E0),
                                        focusedTextColor = Color(0xFF1A3F52),
                                        unfocusedTextColor = Color(0xFF1A3F52)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Submit Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.clearAuthMessages()
                                    viewModel.logInUser(loginEmail.trim(), loginPassword) { _, _ -> }
                                },
                                enabled = !uiState.isAuthLoading && loginEmail.isNotBlank() && loginPassword.isNotBlank(),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (uiState.isAuthLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Text("Log In", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                    }
                                }
                            }

                            // Footer Switch Link
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "New user? ",
                                    fontSize = 13.sp,
                                    color = Color(0xFF4A6B82)
                                )
                                Text(
                                    text = "Sign up",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1877F2),
                                    modifier = Modifier.clickable {
                                        viewModel.clearAuthMessages()
                                        selectedTab = 1
                                    }
                                )
                            }
                        }
                    } else {
                        // ----------------- SIGN UP FORM -----------------
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // First & Last Name
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(13.dp))
                                        Text("First Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C4A5E))
                                    }
                                    OutlinedTextField(
                                        value = signupFirstName,
                                        onValueChange = { signupFirstName = it },
                                        placeholder = { Text("John", color = Color(0xFF8FA9BA), fontSize = 12.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(20.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = Color(0xFF1877F2),
                                            unfocusedBorderColor = Color(0xFFBED3E0),
                                            focusedTextColor = Color(0xFF1A3F52),
                                            unfocusedTextColor = Color(0xFF1A3F52)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(13.dp))
                                        Text("Last Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C4A5E))
                                    }
                                    OutlinedTextField(
                                        value = signupLastName,
                                        onValueChange = { signupLastName = it },
                                        placeholder = { Text("Doe", color = Color(0xFF8FA9BA), fontSize = 12.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(20.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = Color(0xFF1877F2),
                                            unfocusedBorderColor = Color(0xFFBED3E0),
                                            focusedTextColor = Color(0xFF1A3F52),
                                            unfocusedTextColor = Color(0xFF1A3F52)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Telegram Field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(13.dp))
                                    Text("Telegram Username", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C4A5E))
                                }
                                OutlinedTextField(
                                    value = signupTelegram,
                                    onValueChange = { signupTelegram = it },
                                    placeholder = { Text("@username", color = Color(0xFF8FA9BA), fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xFF1877F2),
                                        unfocusedBorderColor = Color(0xFFBED3E0),
                                        focusedTextColor = Color(0xFF1A3F52),
                                        unfocusedTextColor = Color(0xFF1A3F52)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Email Field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(13.dp))
                                    Text("Email Address", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C4A5E))
                                }
                                OutlinedTextField(
                                    value = signupEmail,
                                    onValueChange = { signupEmail = it },
                                    placeholder = { Text("you@example.com", color = Color(0xFF8FA9BA), fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xFF1877F2),
                                        unfocusedBorderColor = Color(0xFFBED3E0),
                                        focusedTextColor = Color(0xFF1A3F52),
                                        unfocusedTextColor = Color(0xFF1A3F52)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Password Field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(13.dp))
                                    Text("Password", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C4A5E))
                                }
                                OutlinedTextField(
                                    value = signupPassword,
                                    onValueChange = { signupPassword = it },
                                    placeholder = { Text("••••••••", color = Color(0xFF8FA9BA), fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                    visualTransformation = if (signupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            if (signupEmail.isNotBlank() && signupPassword.isNotBlank()) {
                                                viewModel.signUpUser(signupFirstName.trim(), signupLastName.trim(), signupTelegram.trim(), signupEmail.trim(), signupPassword) { success, _ ->
                                                    if (success) {
                                                        loginEmail = signupEmail
                                                        loginPassword = signupPassword
                                                        selectedTab = 0
                                                    }
                                                }
                                            }
                                        }
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { signupPasswordVisible = !signupPasswordVisible }) {
                                            Icon(
                                                imageVector = if (signupPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password",
                                                tint = Color(0xFF6E8E9E),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xFF1877F2),
                                        unfocusedBorderColor = Color(0xFFBED3E0),
                                        focusedTextColor = Color(0xFF1A3F52),
                                        unfocusedTextColor = Color(0xFF1A3F52)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Submit Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.clearAuthMessages()
                                    viewModel.signUpUser(
                                        signupFirstName.trim(),
                                        signupLastName.trim(),
                                        signupTelegram.trim(),
                                        signupEmail.trim(),
                                        signupPassword
                                    ) { success, _ ->
                                        if (success) {
                                            loginEmail = signupEmail
                                            loginPassword = signupPassword
                                            selectedTab = 0
                                        }
                                    }
                                },
                                enabled = !uiState.isAuthLoading && signupEmail.isNotBlank() && signupPassword.isNotBlank(),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (uiState.isAuthLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                    }
                                }
                            }

                            // Footer Switch Link
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Already have an account? ",
                                    fontSize = 13.sp,
                                    color = Color(0xFF4A6B82)
                                )
                                Text(
                                    text = "Log in",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1877F2),
                                    modifier = Modifier.clickable {
                                        viewModel.clearAuthMessages()
                                        selectedTab = 0
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
