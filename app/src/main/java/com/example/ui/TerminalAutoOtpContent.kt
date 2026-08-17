package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

enum class CreationMethod(val title: String) {
    NM_OFFICIAL("NM OFFICAL"),
    NM_LIMIT("NM LIMIT")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalAutoOtpScreen(
    onClose: () -> Unit,
    onStart: (range: String, count: Int, threads: Int, method: CreationMethod, isFindAccountEnabled: Boolean, password: String) -> Unit,
    onStop: () -> Unit,
    isRunning: Boolean,
    logs: List<String>,
    proxyStatus: String,
    initialPassword: String = "arafat@@##",
    successCount: Int = 0,
    noAccountCount: Int = 0,
    existCount: Int = 0,
    failedCount: Int = 0,
    availableRanges: List<String> = emptyList()
) {
    var selectedMethod by remember { mutableStateOf(CreationMethod.NM_OFFICIAL) }
    var isFindAccountOn by remember { mutableStateOf(false) }
    var rangeInput by remember { mutableStateOf(if (availableRanges.isNotEmpty()) availableRanges.first() else "") }
    var passwordInput by remember { mutableStateOf(if (initialPassword.isNotBlank()) initialPassword else "arafat@@##") }
    var accountCount by remember { mutableStateOf("5") }
    var threadCount by remember { mutableStateOf("2") }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0F1D)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Compact Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isRunning) Color(0xFF22C55E) else Color(0xFF94A3B8),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Text(
                        text = "Terminal",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = {
                        if (isRunning) onStop()
                        onClose()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            
            // Ultra-Compact Proxy Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🌐 Proxy: $proxyStatus",
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Method: ${selectedMethod.title}",
                    color = Color(0xFFA5B4FC),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Top Real-time Counters Grid (Success, No Account, Exist, Failed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Success Card
                StatChip(
                    title = "Success",
                    count = successCount,
                    accentColor = Color(0xFF22C55E),
                    modifier = Modifier.weight(1f)
                )
                // No Account (Fresh) Card
                StatChip(
                    title = "No Account",
                    count = noAccountCount,
                    accentColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                // Exist (Already has account) Card
                StatChip(
                    title = "Exist",
                    count = existCount,
                    accentColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                // Failed Card
                StatChip(
                    title = "Failed",
                    count = failedCount,
                    accentColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }

            // Compact Controls Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Method Selector + Find Account ON/OFF Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // NM OFFICIAL
                        MethodSelectorButton(
                            title = "⚡ NM OFFICAL",
                            isSelected = selectedMethod == CreationMethod.NM_OFFICIAL,
                            enabled = !isRunning,
                            onClick = { selectedMethod = CreationMethod.NM_OFFICIAL },
                            modifier = Modifier.weight(1f)
                        )
                        // NM LIMIT
                        MethodSelectorButton(
                            title = "🚀 NM LIMIT",
                            isSelected = selectedMethod == CreationMethod.NM_LIMIT,
                            enabled = !isRunning,
                            onClick = { selectedMethod = CreationMethod.NM_LIMIT },
                            modifier = Modifier.weight(1f)
                        )
                        // Find Account ON / OFF Toggle Button
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(28.dp)
                                .background(
                                    if (isFindAccountOn) Color(0xFF15803D) else Color(0xFF334155),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isFindAccountOn) Color(0xFF4ADE80) else Color(0xFF64748B),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable(enabled = !isRunning) { isFindAccountOn = !isFindAccountOn },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFindAccountOn) Icons.Default.Check else Icons.Default.Search,
                                    contentDescription = null,
                                    tint = if (isFindAccountOn) Color.White else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isFindAccountOn) "Find Acc: ON" else "Find Acc: OFF",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Password Field: Locked for NM OFFICIAL, Editable for NM LIMIT
                    val isOfficial = selectedMethod == CreationMethod.NM_OFFICIAL
                    OutlinedTextField(
                        value = if (isOfficial) "arafat@@## (Default Official)" else passwordInput,
                        onValueChange = { if (!isOfficial) passwordInput = it },
                        placeholder = { Text("Password", color = Color(0xFF64748B), fontSize = 10.sp) },
                        label = {
                            Text(
                                text = if (isOfficial) "Password (Fixed for NM OFFICIAL)" else "Password (Editable for NM LIMIT)",
                                fontSize = 9.sp,
                                color = if (isOfficial) Color(0xFF94A3B8) else Color(0xFF38BDF8)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isOfficial) Color(0xFF64748B) else Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        singleLine = true,
                        enabled = !isRunning && !isOfficial,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color(0xFF94A3B8),
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            disabledBorderColor = Color(0xFF242E42),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            disabledContainerColor = Color(0xFF0B1120)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )

                    // Range Input Field
                    OutlinedTextField(
                        value = rangeInput,
                        onValueChange = { rangeInput = it },
                        placeholder = { Text("Range (e.g. 2250689XXXX)", color = Color(0xFF64748B), fontSize = 10.sp) },
                        singleLine = true,
                        enabled = !isRunning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.LightGray,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )

                    // Quick select range chips if available
                    if (availableRanges.isNotEmpty() && !isRunning) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            availableRanges.take(8).forEach { r ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (rangeInput == r) Color(0xFF2563EB) else Color(0xFF334155),
                                            RoundedCornerShape(3.dp)
                                        )
                                        .clickable { rangeInput = r }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = r,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                    
                    // Account count and Threads & Button in compact rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = accountCount,
                            onValueChange = { accountCount = it.filter { ch -> ch.isDigit() } },
                            placeholder = { Text("Accounts", color = Color(0xFF64748B), fontSize = 10.sp) },
                            singleLine = true,
                            enabled = !isRunning,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledTextColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                        
                        OutlinedTextField(
                            value = threadCount,
                            onValueChange = { threadCount = it.filter { ch -> ch.isDigit() } },
                            placeholder = { Text("Threads", color = Color(0xFF64748B), fontSize = 10.sp) },
                            singleLine = true,
                            enabled = !isRunning,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledTextColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )

                        if (!isRunning) {
                            Button(
                                onClick = {
                                    val count = accountCount.toIntOrNull() ?: 1
                                    val threads = threadCount.toIntOrNull() ?: 1
                                    val finalPwd = if (isOfficial) "arafat@@##" else passwordInput.trim().ifEmpty { "arafat@@##" }
                                    onStart(rangeInput.trim(), count, threads, selectedMethod, isFindAccountOn, finalPwd)
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                enabled = rangeInput.isNotBlank()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("START", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = onStop,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("STOP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Real-time Terminal Log Screen (Maximised Viewport)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                    .padding(6.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ">_ Terminal Ready.\n• Method: ${selectedMethod.title}\n• Find Account: ${if (isFindAccountOn) "ON (Fresh Only)" else "OFF (All Numbers)"}\n• Select Range, Password & Threads, then click START.",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(logs) { log ->
                            val color = when {
                                log.contains("[SUCCESS]") || log.contains("Saved to Database") -> Color(0xFF4ADE80)
                                log.contains("[FAILED]") || log.contains("[ERROR]") -> Color(0xFFF87171)
                                log.contains("[PROXY]") -> Color(0xFF38BDF8)
                                log.contains("ALREADY EXISTS") -> Color(0xFFFBBF24)
                                log.contains("No Account Found") -> Color(0xFF34D399)
                                log.contains("[THREAD") -> Color(0xFFFCD34D)
                                log.contains("[SYSTEM]") || log.contains("[CONFIG]") -> Color(0xFFA78BFA)
                                else -> Color(0xFFE2E8F0)
                            }
                            Text(
                                text = log,
                                color = color,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 13.sp,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    title: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.5f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = "$count",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun MethodSelectorButton(
    title: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(28.dp)
            .background(
                if (isSelected) Color(0xFF2563EB) else Color(0xFF334155),
                RoundedCornerShape(4.dp)
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) Color(0xFF60A5FA) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
