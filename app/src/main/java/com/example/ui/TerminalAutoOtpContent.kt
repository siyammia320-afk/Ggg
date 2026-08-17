package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalAutoOtpScreen(
    onClose: () -> Unit,
    onStart: (String, Int, Int) -> Unit,
    onStop: () -> Unit,
    isRunning: Boolean,
    logs: List<String>,
    proxyStatus: String,
    availableRanges: List<String> = emptyList()
) {
    var rangeInput by remember { mutableStateOf(if (availableRanges.isNotEmpty()) availableRanges.first() else "") }
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
        color = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (isRunning) Color(0xFF22C55E) else Color(0xFF94A3B8),
                                shape = RoundedCornerShape(5.dp)
                            )
                    )
                    Text(
                        text = "Terminal Auto OTP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                IconButton(
                    onClick = {
                        if (isRunning) onStop()
                        onClose()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
            
            // Proxy Status bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF182234))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🌐 Proxy: $proxyStatus",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Method: NM OFFICIAL (GraphQL)",
                    color = Color(0xFFA5B4FC),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Controls Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Range selector or input
                    OutlinedTextField(
                        value = rangeInput,
                        onValueChange = { rangeInput = it },
                        label = { Text("Range (e.g. 2250689XXXX)", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        placeholder = { Text("Enter range code", color = Color(0xFF64748B), fontSize = 11.sp) },
                        singleLine = true,
                        enabled = !isRunning,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.LightGray,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )

                    // Quick select range chips if available
                    if (availableRanges.isNotEmpty() && !isRunning) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableRanges.take(8).forEach { r ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (rangeInput == r) Color(0xFF2563EB) else Color(0xFF334155),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { rangeInput = r }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = r,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                    
                    // Account count and Threads
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = accountCount,
                            onValueChange = { accountCount = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Total Accounts", color = Color(0xFF94A3B8), fontSize = 10.sp) },
                            singleLine = true,
                            enabled = !isRunning,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledTextColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155)
                            )
                        )
                        
                        OutlinedTextField(
                            value = threadCount,
                            onValueChange = { threadCount = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Threads (Concurrency)", color = Color(0xFF94A3B8), fontSize = 10.sp) },
                            singleLine = true,
                            enabled = !isRunning,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledTextColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155)
                            )
                        )
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isRunning) {
                            Button(
                                onClick = {
                                    val count = accountCount.toIntOrNull() ?: 1
                                    val threads = threadCount.toIntOrNull() ?: 1
                                    onStart(rangeInput.trim(), count, threads)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(6.dp),
                                enabled = rangeInput.isNotBlank()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⚡ START CREATE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = onStop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🛑 STOP CREATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Real-time Terminal Log Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Terminal Ready. Set Range, Accounts & Threads, then click START CREATE.",
                            color = Color(0xFF6B7280),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
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
                                log.contains("[THREAD") -> Color(0xFFFBBF24)
                                log.contains("[SYSTEM]") || log.contains("[CONFIG]") -> Color(0xFFA78BFA)
                                else -> Color(0xFFE2E8F0)
                            }
                            Text(
                                text = log,
                                color = color,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
