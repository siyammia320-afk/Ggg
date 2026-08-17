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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    onStart: (
        range: String,
        manualNumbersList: List<String>,
        useManualMode: Boolean,
        count: Int,
        threads: Int,
        method: CreationMethod,
        isFindAccountEnabled: Boolean,
        password: String
    ) -> Unit,
    onStop: () -> Unit,
    isRunning: Boolean,
    logs: List<String>,
    proxyStatus: String,
    initialPassword: String = "arafat@@##",
    isTerminalEnabledByAdmin: Boolean = true,
    isManualNumbersEnabledByAdmin: Boolean = true,
    terminalDisabledNotice: String = "Terminal is currently disabled by admin.",
    successCount: Int = 0,
    noAccountCount: Int = 0,
    existCount: Int = 0,
    failedCount: Int = 0,
    availableRanges: List<String> = emptyList(),
    onRefreshRanges: () -> Unit = {}
) {
    var selectedMethod by remember { mutableStateOf(CreationMethod.NM_OFFICIAL) }
    var isFindAccountOn by remember { mutableStateOf(false) }
    var isManualMode by remember { mutableStateOf(false) }
    
    var rangeInput by remember { mutableStateOf(if (availableRanges.isNotEmpty()) availableRanges.first() else "") }
    var manualNumbersText by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf(if (initialPassword.isNotBlank()) initialPassword else "arafat@@##") }
    var accountCount by remember { mutableStateOf("5") }
    var threadCount by remember { mutableStateOf("2") }
    var isRefreshingRangesState by remember { mutableStateOf(false) }

    // If manual mode is disabled by admin, automatically revert to Range mode
    LaunchedEffect(isManualNumbersEnabledByAdmin) {
        if (!isManualNumbersEnabledByAdmin && isManualMode) {
            isManualMode = false
        }
    }

    // Auto-update range if empty and new ranges arrive
    LaunchedEffect(availableRanges) {
        if (rangeInput.isEmpty() && availableRanges.isNotEmpty()) {
            rangeInput = availableRanges.first()
        }
    }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }

    // Parse manual numbers line-by-line
    val manualNumbersList = remember(manualNumbersText) {
        manualNumbersText.lines()
            .map { it.trim().replace("+", "") }
            .filter { it.isNotBlank() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0F1D)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
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
                                if (!isTerminalEnabledByAdmin) Color(0xFFEF4444) else if (isRunning) Color(0xFF22C55E) else Color(0xFF94A3B8),
                                shape = RoundedCornerShape(5.dp)
                            )
                    )
                    Text(
                        text = "Terminal",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (!isTerminalEnabledByAdmin) {
                        Text(
                            text = "(DISABLED BY ADMIN)",
                            color = Color(0xFFF87171),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (isRunning) onStop()
                        onClose()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            
            // Proxy & Method Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🌐 Proxy: $proxyStatus",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Method: ${selectedMethod.title}",
                    color = Color(0xFFA5B4FC),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // If Admin disabled the entire Terminal
            if (!isTerminalEnabledByAdmin) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                        Text(
                            text = terminalDisabledNotice,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Top Real-time Counters Grid (Success, No Account, Exist, Failed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatChip(
                    title = "Success",
                    count = successCount,
                    accentColor = Color(0xFF22C55E),
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    title = "No Account",
                    count = noAccountCount,
                    accentColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    title = "Exist",
                    count = existCount,
                    accentColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    title = "Failed",
                    count = failedCount,
                    accentColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }

            // Controls Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Method Selector + Find Account ON/OFF Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MethodSelectorButton(
                            title = "⚡ NM OFFICAL",
                            isSelected = selectedMethod == CreationMethod.NM_OFFICIAL,
                            enabled = !isRunning && isTerminalEnabledByAdmin,
                            onClick = { selectedMethod = CreationMethod.NM_OFFICIAL },
                            modifier = Modifier.weight(1f)
                        )
                        MethodSelectorButton(
                            title = "🚀 NM LIMIT",
                            isSelected = selectedMethod == CreationMethod.NM_LIMIT,
                            enabled = !isRunning && isTerminalEnabledByAdmin,
                            onClick = { selectedMethod = CreationMethod.NM_LIMIT },
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(32.dp)
                                .background(
                                    if (isFindAccountOn) Color(0xFF15803D) else Color(0xFF334155),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isFindAccountOn) Color(0xFF4ADE80) else Color(0xFF64748B),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable(enabled = !isRunning && isTerminalEnabledByAdmin) { isFindAccountOn = !isFindAccountOn },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFindAccountOn) Icons.Default.Check else Icons.Default.Search,
                                    contentDescription = null,
                                    tint = if (isFindAccountOn) Color.White else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isFindAccountOn) "Find Acc: ON" else "Find Acc: OFF",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Mode Toggle: [ Range Mode (Auto) ] vs [ Manual Number Mode (Line by line) ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Range Mode button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .background(
                                    if (!isManualMode) Color(0xFF2563EB) else Color(0xFF334155),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable(enabled = !isRunning && isTerminalEnabledByAdmin) { isManualMode = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎯 Range Mode",
                                color = if (!isManualMode) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Manual Number Mode button (Admin enabled / disabled check)
                        val isManualAllowed = isManualNumbersEnabledByAdmin
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(28.dp)
                                .background(
                                    if (!isManualAllowed) Color(0xFF475569).copy(alpha = 0.5f)
                                    else if (isManualMode) Color(0xFFD97706)
                                    else Color(0xFF334155),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isManualMode && isManualAllowed) Color(0xFFFBBF24) else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable(enabled = !isRunning && isTerminalEnabledByAdmin && isManualAllowed) {
                                    isManualMode = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isManualAllowed) Icons.Default.Edit else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (!isManualAllowed) Color(0xFF94A3B8) else Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (!isManualAllowed) "Manual (Admin OFF)" else "📋 Manual Lines",
                                    color = if (!isManualAllowed) Color(0xFF94A3B8) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Password Box
                    val isOfficial = selectedMethod == CreationMethod.NM_OFFICIAL
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isOfficial) "🔑 Password (Fixed for NM OFFICIAL)" else "🔑 Password (Custom for NM LIMIT)",
                            fontSize = 10.sp,
                            color = if (isOfficial) Color(0xFF94A3B8) else Color(0xFF38BDF8),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .background(if (isOfficial) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                .border(1.dp, if (isOfficial) Color(0xFF334155) else Color(0xFF38BDF8), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isOfficial) Color(0xFF64748B) else Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isOfficial) {
                                    Text(
                                        text = "arafat@@## (Default Official Locked)",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
                                    )
                                } else {
                                    BasicTextField(
                                        value = passwordInput,
                                        onValueChange = { passwordInput = it },
                                        singleLine = true,
                                        enabled = !isRunning && isTerminalEnabledByAdmin,
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF38BDF8)),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (passwordInput.isEmpty()) {
                                                Text(
                                                    text = "Enter Password...",
                                                    color = Color(0xFF64748B),
                                                    fontSize = 12.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Dynamic Section: Either Range Input (with Refresh button & chips) or Manual Numbers Text Box
                    if (!isManualMode) {
                        // Range Input with Refresh Button
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📞 Phone Range (Auto Generation)",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )

                                // Range Refresh Button
                                Row(
                                    modifier = Modifier
                                        .clickable(enabled = !isRunning && isTerminalEnabledByAdmin) {
                                            isRefreshingRangesState = true
                                            onRefreshRanges()
                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(1000)
                                                isRefreshingRangesState = false
                                            }
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Ranges",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (isRefreshingRangesState) "Refreshing..." else "Refresh Ranges",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = rangeInput,
                                    onValueChange = { rangeInput = it },
                                    singleLine = true,
                                    enabled = !isRunning && isTerminalEnabledByAdmin,
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    cursorBrush = SolidColor(Color(0xFF38BDF8)),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (rangeInput.isEmpty()) {
                                            Text(
                                                text = "Enter Range (e.g. 26134XXX)",
                                                color = Color(0xFF64748B),
                                                fontSize = 12.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }

                        // Quick select range chips
                        if (availableRanges.isNotEmpty() && !isRunning) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                availableRanges.take(12).forEach { r ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (rangeInput == r) Color(0xFF2563EB) else Color(0xFF334155),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable { rangeInput = r }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
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
                    } else {
                        // Manual Numbers Multi-line Text Box
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📋 Manual Numbers (Line by Line):",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (manualNumbersList.isNotEmpty()) Color(0xFF065F46) else Color(0xFF334155),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "⚡ Load: ${manualNumbersList.size} Ta",
                                        fontSize = 10.sp,
                                        color = if (manualNumbersList.isNotEmpty()) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(76.dp)
                                    .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.TopStart
                            ) {
                                BasicTextField(
                                    value = manualNumbersText,
                                    onValueChange = { manualNumbersText = it },
                                    enabled = !isRunning && isTerminalEnabledByAdmin,
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(Color(0xFFFBBF24)),
                                    modifier = Modifier.fillMaxSize(),
                                    decorationBox = { innerTextField ->
                                        if (manualNumbersText.isEmpty()) {
                                            Text(
                                                text = "Paste numbers here (one per line):\n88017282828\n88018272626\n0192827262",
                                                color = Color(0xFF64748B),
                                                fontSize = 11.sp,
                                                lineHeight = 16.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                    }
                    
                    // Account count, Threads & START Button in compact rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Accounts Input Box (Disabled or Auto in Manual Mode)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isManualMode) "Total Num" else "Accounts",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(bottom = 1.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .background(if (isManualMode) Color(0xFF1E293B) else Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isManualMode) Color(0xFF64748B) else Color(0xFF38BDF8), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (isManualMode) {
                                    Text(
                                        text = "${manualNumbersList.size}",
                                        color = Color(0xFF34D399),
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    BasicTextField(
                                        value = accountCount,
                                        onValueChange = { accountCount = it.filter { ch -> ch.isDigit() } },
                                        singleLine = true,
                                        enabled = !isRunning && isTerminalEnabledByAdmin,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF38BDF8)),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (accountCount.isEmpty()) {
                                                Text("Qty", color = Color(0xFF64748B), fontSize = 11.sp)
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Threads Input Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Threads",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(bottom = 1.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = threadCount,
                                    onValueChange = { threadCount = it.filter { ch -> ch.isDigit() } },
                                    singleLine = true,
                                    enabled = !isRunning && isTerminalEnabledByAdmin,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    cursorBrush = SolidColor(Color(0xFF38BDF8)),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (threadCount.isEmpty()) {
                                            Text("Th", color = Color(0xFF64748B), fontSize = 11.sp)
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }

                        // START / STOP Action Button
                        val canStart = isTerminalEnabledByAdmin && if (isManualMode) manualNumbersList.isNotEmpty() else rangeInput.isNotBlank()
                        if (!isRunning) {
                            Button(
                                onClick = {
                                    val count = if (isManualMode) manualNumbersList.size else (accountCount.toIntOrNull() ?: 1)
                                    val threads = threadCount.toIntOrNull() ?: 1
                                    val finalPwd = if (isOfficial) "arafat@@##" else passwordInput.trim().ifEmpty { "arafat@@##" }
                                    onStart(
                                        rangeInput.trim(),
                                        manualNumbersList,
                                        isManualMode,
                                        count,
                                        threads,
                                        selectedMethod,
                                        isFindAccountOn,
                                        finalPwd
                                    )
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .padding(top = 12.dp)
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                enabled = canStart
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("START", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = onStop,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .padding(top = 12.dp)
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("STOP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ">_ Terminal Ready.\n• Method: ${selectedMethod.title}\n• Mode: ${if (isManualMode) "Manual Lines (${manualNumbersList.size})" else "Range ($rangeInput)"}\n• Find Account: ${if (isFindAccountOn) "ON (Fresh Only)" else "OFF (All Numbers)"}\n• Click START to run automated creation.",
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

@Composable
private fun StatChip(
    title: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(accentColor.copy(alpha = 0.5f)))
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
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = "$count",
                fontSize = 13.sp,
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
            .height(32.dp)
            .background(
                if (isSelected) Color(0xFF2563EB) else Color(0xFF334155),
                RoundedCornerShape(6.dp)
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) Color(0xFF60A5FA) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
