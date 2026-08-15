package com.example

import com.example.ui.AuthScreen
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import android.provider.Settings
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.text.style.TextAlign
import com.example.service.FloatingWidgetService
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.data.Country
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AccountEntity
import com.example.ui.MainViewModel
import com.example.ui.SplashScreen
import com.example.ui.VoltxActiveNumber
import com.example.ui.theme.FBTheme

fun getCountryFlagForRange(rangeCode: String): String {
    val clean = rangeCode.filter { it.isDigit() }
    return when {
        clean.startsWith("880") -> "🇧🇩"
        clean.startsWith("1") -> "🇺🇸"
        clean.startsWith("33") -> "🇫🇷"
        clean.startsWith("86") -> "🇨🇳"
        clean.startsWith("966") -> "🇸🇦"
        clean.startsWith("91") -> "🇮🇳"
        clean.startsWith("261") -> "🇲🇬"
        clean.startsWith("44") -> "🇬🇧"
        clean.startsWith("49") -> "🇩🇪"
        clean.startsWith("92") -> "🇵🇰"
        clean.startsWith("62") -> "🇮🇩"
        clean.startsWith("60") -> "🇲🇾"
        clean.startsWith("63") -> "🇵🇭"
        clean.startsWith("20") -> "🇪🇬"
        clean.startsWith("90") -> "🇹🇷"
        clean.startsWith("7") -> "🇷🇺"
        clean.startsWith("39") -> "🇮🇹"
        clean.startsWith("34") -> "🇪🇸"
        clean.startsWith("55") -> "🇧🇷"
        clean.startsWith("234") -> "🇳🇬"
        else -> "📱"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            FBTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: com.example.ui.MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var showSplashScreen by rememberSaveable { mutableStateOf(true) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (showSplashScreen) {
        SplashScreen(onFinished = { showSplashScreen = false })
        return
    }

    if (!uiState.isLoggedIn) {
        AuthScreen(viewModel = viewModel, onAuthSuccess = {})
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "FB TOOL 💣💥",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "ONLINE • FLOATING CONTROLLER",
                                fontSize = 10.sp,
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    val context = LocalContext.current
                    IconButton(
                        onClick = { viewModel.checkForUpdates(manual = true, context = context) },
                        enabled = !uiState.isCheckingUpdate
                    ) {
                        if (uiState.isCheckingUpdate) {
                            CircularProgressIndicator(
                                color = Color(0xFF38BDF8),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Check Update",
                                tint = if (uiState.updateInfo?.hasUpdate == true) Color(0xFFFBBF24) else Color(0xFF94A3B8)
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.logOutUser() }) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Logout",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF020617))
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Prominent update notification banner
            if (uiState.updateInfo?.hasUpdate == true) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { viewModel.checkForUpdates(manual = false) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "নতুন আপডেট এসেছে! (v${uiState.updateInfo?.latestVersionName})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "সরাসরি অ্যাপের ভেতর থেকে আপডেট করুন",
                                    fontSize = 10.sp,
                                    color = Color(0xFF93C5FD)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.startDownloadingAndInstallUpdate(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("UPDATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            FloatingWidgetControlCard(viewModel = viewModel)
        }

        if (uiState.showUpdateDialog) {
            AppUpdateDialog(uiState = uiState, viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullFeatureAppContent(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val accountsHistory by viewModel.accountHistory.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    if (!uiState.isLoggedIn) {
        AuthScreen(viewModel = viewModel, onAuthSuccess = {})
        return
    }

    if (!uiState.isAppOn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "APP IS CURRENTLY OFF 🚫",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = uiState.appStatusMessage,
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { viewModel.checkAppStatusManually() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (uiState.isCheckingAppStatus) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                Text("RECHECK APP STATUS", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            kotlinx.coroutines.delay(1500)
            viewModel.dismissMessage()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            kotlinx.coroutines.delay(1500)
            viewModel.dismissMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = viewModel::openProxyDialog,
                        modifier = Modifier.testTag("settings_proxy_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Proxy Settings",
                            tint = Color(0xFF1E293B)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White)
            ) {
                // Non-blocking compact message notification banner
                if (uiState.errorMessage != null || uiState.successMessage != null) {
                    val isErr = uiState.errorMessage != null
                    val message = uiState.errorMessage ?: uiState.successMessage ?: ""
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isErr) Color(0xFFFEE2E2) else Color(0xFFE2E8F0))
                            .clickable { viewModel.dismissMessage() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = message,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isErr) Color(0xFF991B1B) else Color(0xFF1E293B),
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = if (isErr) Color(0xFF991B1B) else Color(0xFF1E293B),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = Color(0xFF1E293B),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF2563EB),
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        modifier = Modifier.testTag("tab_get_number")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = if (selectedTabIndex == 0) Color(0xFF2563EB) else Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "NUMBER",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (selectedTabIndex == 0) Color(0xFF1E293B) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }
                    }
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        modifier = Modifier.testTag("tab_create")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = if (selectedTabIndex == 1) Color(0xFF2563EB) else Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "NM CREATE",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (selectedTabIndex == 1) Color(0xFF1E293B) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }
                    }
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        modifier = Modifier.testTag("tab_email_create")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = if (selectedTabIndex == 2) Color(0xFF2563EB) else Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "EMAIL CREATE",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (selectedTabIndex == 2) Color(0xFF1E293B) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }
                    }
                    Tab(
                        selected = selectedTabIndex == 3,
                        onClick = { selectedTabIndex = 3 },
                        modifier = Modifier.testTag("tab_inbox")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Sms,
                                contentDescription = null,
                                tint = if (selectedTabIndex == 3) Color(0xFF2563EB) else Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "INBOX (${uiState.activeNumbers.size})",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (selectedTabIndex == 3) Color(0xFF1E293B) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }
                    }
                    Tab(
                        selected = selectedTabIndex == 4,
                        onClick = { selectedTabIndex = 4 },
                        modifier = Modifier.testTag("tab_history")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = if (selectedTabIndex == 4) Color(0xFF2563EB) else Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "SAVED (${accountsHistory.size})",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (selectedTabIndex == 4) Color(0xFF1E293B) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }
                    }
                }

                when (selectedTabIndex) {
                    0 -> GetNumberTabContent(
                        uiState = uiState,
                        onRangeClicked = viewModel::onRangeClicked,
                        onCustomRangeChange = viewModel::onCustomRangeChanged,
                        onFetchCustomRange = viewModel::fetchNumberFromCustomRange,
                        onRefreshRanges = viewModel::refreshFacebookRanges,
                        onCopyDeviceId = { devId -> viewModel.copyToClipboard(context, devId, "DEVICE ID") },
                        onCheckActivation = viewModel::checkDeviceActivationManually,
                        onCopyNumber = { num -> viewModel.copyToClipboard(context, num, "PHONE NUMBER") },
                        onGoToCreate = { selectedTabIndex = 1 },
                        onOpenApiKeyDialog = viewModel::openApiKeyDialog
                    )
                    1 -> CreateAccountTabContent(
                        uiState = uiState,
                        onPhoneChange = viewModel::onPhoneChanged,
                        onPasswordChange = viewModel::onPasswordChanged,
                        onCountrySelected = viewModel::onCountrySelected,
                        onCreateAccount = viewModel::createAccount,
                        onFindAccount = viewModel::findAccount,
                        onCopyUid = { uid -> viewModel.copyToClipboard(context, uid, "UID") },
                        onCopyNumber = { num -> viewModel.copyToClipboard(context, num, "NUMBER") },
                        onCopyCookies = { cookie -> viewModel.copyToClipboard(context, cookie, "COOKIES") },
                        onGoToGetNumber = { selectedTabIndex = 0 },
                        onOpenProxySettings = viewModel::openProxyDialog,
                        onCheckLive = viewModel::checkLiveStatusForSingleAccount
                    )
                    2 -> EmailCreateTabContent(
                        uiState = uiState,
                        onEmailChange = viewModel::onEmailChanged,
                        onPasswordChange = viewModel::onPasswordChanged,
                        onCountrySelected = viewModel::onCountrySelected,
                        onGenerateRandomEmail = viewModel::generateRandomEmail,
                        onCreateAccountWithEmail = { viewModel.createAccountWithEmail(context) },
                        onCopyEmail = { email -> viewModel.copyToClipboard(context, email, "EMAIL") },
                        onCopyUid = { uid -> viewModel.copyToClipboard(context, uid, "UID") },
                        onCopyCookies = { cookie -> viewModel.copyToClipboard(context, cookie, "COOKIES") },
                        onOpenProxySettings = viewModel::openProxyDialog,
                        onCheckLive = viewModel::checkLiveStatusForSingleAccount
                    )
                    3 -> InboxTabContent(
                        activeNumbers = uiState.activeNumbers,
                        onCopyOtp = { otp -> viewModel.copyToClipboard(context, otp, "OTP CODE") },
                        onCopyPhone = { phone -> viewModel.copyToClipboard(context, phone, "PHONE NUMBER") },
                        onCopyUid = { uid -> viewModel.copyToClipboard(context, uid, "UID") },
                        onClearInbox = viewModel::clearInbox,
                        onReloadInbox = viewModel::manualRefreshOtps
                    )
                    4 -> AccountHistoryTabContent(
                        accounts = accountsHistory,
                        onClearAll = viewModel::clearAllAccounts,
                        onDeleteOne = viewModel::deleteAccount,
                        onCopyUid = { uid -> viewModel.copyToClipboard(context, uid, "UID") },
                        onCopyNumber = { num -> viewModel.copyToClipboard(context, num, "NUMBER") },
                        onCopyCookies = { cookie -> viewModel.copyToClipboard(context, cookie, "COOKIES") },
                        liveStatuses = uiState.liveStatuses,
                        isCheckingLive = uiState.isCheckingLive,
                        onCheckLive = viewModel::checkLiveStatusForSavedAccounts
                    )
                }
            }
        }

        // First-Time Telegram Join Dialog Overlay
        if (uiState.showTelegramDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(
                        onClick = { viewModel.dismissTelegramDialog() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                        .clickable(
                            onClick = { },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8)
                            )
                            Text(text = "Join Official Telegram", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        }

                        Text(
                            text = "Please join our official Telegram channel to stay updated with new features and announcements!",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.dismissTelegramDialog() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                            ) {
                                Text("Close", color = Color.LightGray)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/INCOME_FREE_BD")).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) { }
                                    viewModel.dismissTelegramDialog()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text("JOIN CHANNEL", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showProxyDialog) {
            ProxySettingsDialog(
                uiState = uiState,
                onSave = viewModel::saveProxySettings,
                onDismiss = viewModel::closeProxyDialog
            )
        }

        if (uiState.showApiKeyDialog) {
            ApiKeySettingsDialog(
                uiState = uiState,
                onSave = viewModel::saveApiKey,
                onDismiss = viewModel::closeApiKeyDialog
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GetNumberTabContent(
    uiState: com.example.ui.AccountCreatorUiState,
    onRangeClicked: (String) -> Unit,
    onCustomRangeChange: (String) -> Unit,
    onFetchCustomRange: () -> Unit,
    onRefreshRanges: () -> Unit,
    onCopyDeviceId: (String) -> Unit,
    onCheckActivation: () -> Unit,
    onCopyNumber: (String) -> Unit,
    onGoToCreate: () -> Unit,
    onOpenApiKeyDialog: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ✍️ Compact Custom Range Input & GET Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "✍️ Custom Range / নিজের রেঞ্জ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "(যেমন: 2250689XXXX)",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.customRangeInput,
                            onValueChange = onCustomRangeChange,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("custom_range_input"),
                            placeholder = {
                                Text("2250689XXXX", color = Color(0xFF64748B), fontSize = 12.sp)
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = {
                                if (uiState.customRangeInput.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onCustomRangeChange("") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        )

                        // Compact GET Button
                        Button(
                            onClick = onFetchCustomRange,
                            enabled = !uiState.isFetchingNumber && !uiState.isCreating,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("get_custom_range_button")
                        ) {
                            if (uiState.isFetchingNumber && uiState.selectedRangeCode == uiState.customRangeInput) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text("GET", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Facebook Live Ranges Selection Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📘 Facebook Ranges",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Tap a range to fetch auto number",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onRefreshRanges,
                                enabled = !uiState.isLoadingRanges,
                                modifier = Modifier.size(32.dp)
                            ) {
                                if (uiState.isLoadingRanges) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color(0xFF2563EB),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Refresh Ranges",
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (uiState.facebookRanges.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.isLoadingRanges) "Loading live ranges..." else "No Facebook ranges available. Tap Refresh above.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            uiState.facebookRanges.forEach { rangeCode ->
                                val flag = remember(rangeCode) { getCountryFlagForRange(rangeCode) }
                                val isSelected = uiState.selectedRangeCode == rangeCode

                                Button(
                                    onClick = { onRangeClicked(rangeCode) },
                                    enabled = !uiState.isFetchingNumber && !uiState.isCreating,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF4B5563) else Color(0xFF2D2D2D)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = flag, fontSize = 15.sp)
                                        Text(
                                            text = rangeCode,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fetched Number Display & Quick Navigate to Create
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📱 FETCHED PHONE NUMBER",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (uiState.isFetchingNumber) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.LightGray,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2A2A2A))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (uiState.phoneInput.isNotEmpty()) uiState.phoneInput else "Tap any range above to get a number",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.phoneInput.isNotEmpty()) Color.White else Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                            if (uiState.selectedRangeCode != null) {
                                Text(
                                    text = "Range: ${uiState.selectedRangeCode}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    if (uiState.phoneInput.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onCopyNumber(uiState.phoneInput) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("COPY NUMBER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = onGoToCreate,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(42.dp)
                                    .testTag("go_to_create_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("CREATE NOW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateAccountTabContent(
    uiState: com.example.ui.AccountCreatorUiState,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCountrySelected: (Country) -> Unit,
    onCreateAccount: () -> Unit,
    onFindAccount: () -> Unit = {},
    onCopyUid: (String) -> Unit,
    onCopyNumber: (String) -> Unit,
    onCopyCookies: (String) -> Unit,
    onGoToGetNumber: () -> Unit,
    onOpenProxySettings: () -> Unit = {},
    onCheckLive: (String) -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Proxy Status Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🌐 Proxy: ${uiState.proxyStatus}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onOpenProxySettings,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Main Create Account Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚙️ Create Account (Number)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Phone Input (Read-only)
                OutlinedTextField(
                    value = uiState.phoneInput,
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    placeholder = { Text("Select number from NUMBER tab", color = Color.Gray, fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color(0xFF2D2D2D),
                        disabledTextColor = Color.White,
                        disabledPlaceholderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input")
                )

                // Country Selector Dropdown
                CountryDropdownSelector(
                    selectedCountry = uiState.selectedCountry,
                    enabled = true,
                    onCountrySelected = onCountrySelected
                )

                // Password Input
                OutlinedTextField(
                    value = uiState.passwordInput,
                    onValueChange = onPasswordChange,
                    enabled = true,
                    placeholder = { Text("Password", color = Color.Gray, fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color(0xFF2D2D2D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input")
                )

                // Action Buttons Row (CREATE NOW & Find Account)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // CREATE NOW Button
                    Button(
                        onClick = onCreateAccount,
                        enabled = !uiState.isCreating && !uiState.isFindingAccount,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF334155),
                            disabledContainerColor = Color(0xFF2D2D2D)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("create_now_button")
                    ) {
                        if (uiState.isCreating) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("CREATING...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("CREATE NOW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Find Account Button
                    Button(
                        onClick = onFindAccount,
                        enabled = !uiState.isCreating && !uiState.isFindingAccount,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            disabledContainerColor = Color(0xFF2D2D2D)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("find_account_button")
                    ) {
                        if (uiState.isFindingAccount) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("SEARCHING...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("Find Account", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Find Account Result Banner
                uiState.findAccountResult?.let { result ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.contains("Yess")) Color(0xFF065F46) else Color(0xFF7F1D1D)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account Status:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = result,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Result Card (Compact layout for 1 screen)
        uiState.lastCreatedAccount?.let { account ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("✅ Account Created!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    
                    val liveStatus = uiState.liveStatuses[account.uid]
                    val statusText = when (liveStatus) {
                        true -> " Live ✅"
                        false -> " Dad 🚫"
                        null -> ""
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("UID: ${account.uid}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        if (statusText.isNotEmpty()) {
                            Text(statusText, fontSize = 11.sp, color = if (liveStatus == true) Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onCopyUid(account.uid) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .testTag("copy_uid_button")
                        ) {
                            Text("COPY UID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { onCopyNumber(account.phone) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .testTag("copy_number_button")
                        ) {
                            Text("COPY PHONE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { onCopyCookies(account.cookies) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .testTag("copy_cookies_button")
                        ) {
                            Text("COPY COOKIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { onCheckLive(account.uid) },
                        enabled = !uiState.isCheckingLive,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        if (uiState.isCheckingLive) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 1.5.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text("Live Chake ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InboxTabContent(
    activeNumbers: List<VoltxActiveNumber>,
    onCopyOtp: (String) -> Unit,
    onCopyPhone: (String) -> Unit,
    onCopyUid: (String) -> Unit,
    onClearInbox: () -> Unit = {},
    onReloadInbox: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📥 OTP Inbox (${activeNumbers.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Auto check every 3s • Saved offline • Auto-copied",
                    fontSize = 11.sp,
                    color = Color(0xFF10B981)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onReloadInbox) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reload Inbox",
                        tint = Color(0xFF38BDF8)
                    )
                }

                if (activeNumbers.isNotEmpty()) {
                    IconButton(onClick = onClearInbox) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Clear Inbox",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        if (activeNumbers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "No active requested numbers yet",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Go to 'GET NUMBER' tab and tap any Range to request a number automatically!",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeNumbers) { active ->
                    ActiveNumberInboxCard(
                        active = active,
                        onCopyOtp = onCopyOtp,
                        onCopyPhone = onCopyPhone,
                        onCopyUid = onCopyUid
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveNumberInboxCard(
    active: VoltxActiveNumber,
    onCopyOtp: (String) -> Unit,
    onCopyPhone: (String) -> Unit,
    onCopyUid: (String) -> Unit
) {
    val flag = getCountryFlagForRange(active.rangeCode)
    val hasOtp = !active.otp.isNullOrEmpty() && active.otp != "N/A"

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasOtp) Color(0xFF062C1E) else Color(0xFF0F172A)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (hasOtp) Color(0xFF059669) else Color(0xFF1E293B),
                RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = flag, fontSize = 20.sp)
                    Column {
                        Text(
                            text = active.phone,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Range: ${active.rangeCode} • Time: ${active.timestamp}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(onClick = { onCopyPhone(active.phone) }) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy phone",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!active.accountUid.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UID: ${active.accountUid}",
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Tap to copy",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.clickable { onCopyUid(active.accountUid) }
                    )
                }
            }

            HorizontalDivider(color = if (hasOtp) Color(0xFF065F46) else Color(0xFF1E293B))

            // OTP Section
            if (hasOtp) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Sms,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "OTP RECEIVED",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF047857))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "✅ AUTO-COPIED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = active.otp ?: "",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )

                            Button(
                                onClick = { onCopyOtp(active.otp ?: "") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("COPY OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }

                        if (!active.rawMessage.isNullOrEmpty()) {
                            Text(
                                text = "Message: ${active.rawMessage}",
                                fontSize = 11.sp,
                                color = Color(0xFFA7F3D0)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFFF59E0B),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "⏳ Waiting for OTP... (Checking every 3 seconds)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFBBF24)
                    )
                }
            }
        }
    }
}

@Composable
fun AccountHistoryTabContent(
    accounts: List<AccountEntity>,
    onClearAll: () -> Unit,
    onDeleteOne: (Long) -> Unit,
    onCopyUid: (String) -> Unit,
    onCopyNumber: (String) -> Unit,
    onCopyCookies: (String) -> Unit,
    liveStatuses: Map<String, Boolean> = emptyMap(),
    isCheckingLive: Boolean = false,
    onCheckLive: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Saved Accounts (${accounts.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Stored locally in app database",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        if (accounts.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCheckLive,
                    enabled = !isCheckingLive,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    if (isCheckingLive) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live Chake", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                OutlinedButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp).testTag("clear_all_button")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(64.dp)
                    )
                    Text("No accounts saved yet", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(accounts, key = { it.id }) { acc ->
                    AccountItemCard(
                        account = acc,
                        onDelete = { onDeleteOne(acc.id) },
                        onCopyUid = { onCopyUid(acc.uid) },
                        onCopyNumber = { onCopyNumber(acc.phone) },
                        onCopyCookies = { onCopyCookies(acc.cookies) },
                        liveStatus = liveStatuses[acc.uid]
                    )
                }
            }
        }
    }
}

@Composable
fun AccountItemCard(
    account: AccountEntity,
    onDelete: () -> Unit,
    onCopyUid: (String) -> Unit,
    onCopyNumber: (String) -> Unit,
    onCopyCookies: (String) -> Unit,
    liveStatus: Boolean? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "UID: ${account.uid}",
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    when (liveStatus) {
                        true -> Text("Live ✅", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        false -> Text("DAD 🚫", fontSize = 12.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        null -> {}
                    }
                }
                Text("Phone: ${account.phone}", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }

            // 3 Separate Copy Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { onCopyUid(account.uid) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                ) {
                    Text("UID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Button(
                    onClick = { onCopyNumber(account.phone) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                ) {
                    Text("NUMBER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { onCopyCookies(account.cookies) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                ) {
                    Text("COOKIES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CountryDropdownSelector(
    selectedCountry: Country,
    enabled: Boolean,
    onCountrySelected: (Country) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val countries = remember { Country.values() }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Select Country Name Pool",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF38BDF8)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (enabled) Color(0xFF1E293B) else Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { expanded = !expanded }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = selectedCountry.flagEmoji, fontSize = 20.sp)
                        Text(
                            text = selectedCountry.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (enabled) Color.White else Color.Gray
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Select country",
                        tint = if (enabled) Color(0xFF38BDF8) else Color.Gray
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF0F172A))
            ) {
                countries.forEach { country ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = country.flagEmoji, fontSize = 18.sp)
                                Text(
                                    text = country.displayName,
                                    fontSize = 14.sp,
                                    color = if (country == selectedCountry) Color(0xFF38BDF8) else Color.White,
                                    fontWeight = if (country == selectedCountry) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        onClick = {
                            onCountrySelected(country)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProxySettingsDialog(
    uiState: com.example.ui.AccountCreatorUiState,
    onSave: (server: String, port: String, user: String, pass: String, isProxyEnabled: Boolean, isCustomUaEnabled: Boolean, customUa: String) -> Unit,
    onDismiss: () -> Unit
) {
    var isProxyOn by remember { mutableStateOf(uiState.isProxyEnabled) }
    var server by remember { mutableStateOf(uiState.proxyServer) }
    var port by remember { mutableStateOf(uiState.proxyPort) }
    var username by remember { mutableStateOf(uiState.proxyUsername) }
    var password by remember { mutableStateOf(uiState.proxyPassword) }

    var isCustomUaOn by remember { mutableStateOf(uiState.isCustomUserAgentEnabled) }
    var customUa by remember { mutableStateOf(uiState.customUserAgent) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .padding(12.dp)
                .clickable(
                    onClick = { },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF38BDF8))
                    Text("App & Network Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // Scrollable content
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    item {
                        // --- PROXY SETTINGS SECTION ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                        Text("Proxy System", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Switch(
                                        checked = isProxyOn,
                                        onCheckedChange = { isProxyOn = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF10B981),
                                            uncheckedThumbColor = Color.Gray,
                                            uncheckedTrackColor = Color(0xFF334155)
                                        )
                                    )
                                }
                                Text(
                                    text = if (isProxyOn) "Proxy ON (Account created via Proxy)" else "Proxy OFF (Account created via Direct Phone IP)",
                                    fontSize = 11.sp,
                                    color = if (isProxyOn) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )

                                if (isProxyOn) {
                                    OutlinedTextField(
                                        value = server,
                                        onValueChange = { server = it },
                                        label = { Text("Proxy Server / IP", color = Color(0xFF38BDF8)) },
                                        placeholder = { Text("e.g. 192.168.1.1", color = Color.Gray) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF38BDF8),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = port,
                                        onValueChange = { port = it },
                                        label = { Text("Proxy Port", color = Color(0xFF38BDF8)) },
                                        placeholder = { Text("e.g. 8080", color = Color.Gray) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF38BDF8),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = username,
                                        onValueChange = { username = it },
                                        label = { Text("Username (Optional)", color = Color(0xFF38BDF8)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF38BDF8),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = { Text("Password (Optional)", color = Color(0xFF38BDF8)) },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF38BDF8),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // --- USER AGENT SETTINGS SECTION ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Smartphone, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                        Text("User-Agent System", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Switch(
                                        checked = isCustomUaOn,
                                        onCheckedChange = { isCustomUaOn = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF10B981),
                                            uncheckedThumbColor = Color.Gray,
                                            uncheckedTrackColor = Color(0xFF334155)
                                        )
                                    )
                                }
                                Text(
                                    text = if (isCustomUaOn) "Custom User-Agent ON (Uses saved User-Agent)" else "Custom User-Agent OFF (Uses phone original User-Agent)",
                                    fontSize = 11.sp,
                                    color = if (isCustomUaOn) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )

                                if (isCustomUaOn) {
                                    OutlinedTextField(
                                        value = customUa,
                                        onValueChange = { customUa = it },
                                        label = { Text("Custom User-Agent", color = Color(0xFF38BDF8)) },
                                        placeholder = { Text("Mozilla/5.0 (Linux; Android...)", color = Color.Gray) },
                                        minLines = 2,
                                        maxLines = 4,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF38BDF8),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Cancel", color = Color.LightGray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onSave(server, port, username, password, isProxyOn, isCustomUaOn, customUa) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("SAVE SETTINGS", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ApiKeySettingsDialog(
    uiState: com.example.ui.AccountCreatorUiState,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var masterKeyInput by remember { mutableStateOf("") }
    var apiKeyInput by remember { mutableStateOf(uiState.voltxApiKey) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                onClick = { if (!uiState.isSavingApiKey) onDismiss() },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .padding(16.dp)
                .clickable(
                    onClick = { },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "🔐 API Key Set",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = "API কী পরিবর্তন করতে অবশ্যই একটি অব্যবহৃত মাস্টার কী লাগবে। একটি মাস্টার কী দিয়ে শুধুমাত্র একটি এপিআই সেট করা যাবে।",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                // Master Key Field
                OutlinedTextField(
                    value = masterKeyInput,
                    onValueChange = { if (!uiState.isSavingApiKey) masterKeyInput = it },
                    label = { Text("মাস্টার কী (Master Key)", color = Color(0xFF38BDF8)) },
                    placeholder = { Text("মাস্টার কী প্রবেশ করুন", color = Color.Gray) },
                    singleLine = true,
                    enabled = !uiState.isSavingApiKey,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledBorderColor = Color(0xFF1E293B),
                        disabledTextColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // API Key Field
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { if (!uiState.isSavingApiKey) apiKeyInput = it },
                    label = { Text("এপিআই কী (API Key)", color = Color(0xFF38BDF8)) },
                    placeholder = { Text("নতুন এপিআই কী প্রবেশ করুন", color = Color.Gray) },
                    singleLine = true,
                    enabled = !uiState.isSavingApiKey,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledBorderColor = Color(0xFF1E293B),
                        disabledTextColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error Message from State if any
                uiState.errorMessage?.let { error ->
                    Text(
                        text = "⚠️ $error",
                        color = Color(0xFFF87171),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !uiState.isSavingApiKey,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Cancel", color = Color.LightGray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onSave(masterKeyInput, apiKeyInput) },
                        enabled = !uiState.isSavingApiKey && masterKeyInput.isNotEmpty() && apiKeyInput.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        if (uiState.isSavingApiKey) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("SAVE API", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingWidgetControlCard(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isFloatingActive by FloatingWidgetService.isRunning.collectAsStateWithLifecycle()
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            hasOverlayPermission = Settings.canDrawOverlays(context)
            kotlinx.coroutines.delay(1000)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(
                1.dp,
                if (isFloatingActive) Color(0xFF10B981) else Color(0xFF0284C7),
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isFloatingActive) Color(0xFF065F46) else Color(0xFF1E3A8A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (isFloatingActive) Color(0xFF34D399) else Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "FLOATING BUTTON CONTROLLER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isFloatingActive) "FLOATING WIDGET ACTIVE ⚡" else "FLOATING WIDGET OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFloatingActive) Color(0xFF34D399) else Color(0xFF94A3B8)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isFloatingActive) Color(0xFF065F46) else Color(0xFF334155))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFloatingActive) "ON 🟢" else "OFF 🔴",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFloatingActive) Color(0xFF34D399) else Color(0xFFFCA5A5)
                    )
                }
            }

            Text(
                text = "স্টার্ট বাটনে চাপ দিলে একটি ভাসমান বাটন আসবে। এটি স্ক্রিনে যেকোনো জায়গায় সরানো যাবে। বাটনে চাপ দিলে ডায়ালগের মতো ফুল অ্যাপ আসবে। X চাপলে আবার বন্ধ হবে।",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 15.sp
            )

            if (!hasOverlayPermission) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("ALLOW DISPLAY OVER OTHER APPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            hasOverlayPermission = Settings.canDrawOverlays(context)
                            if (hasOverlayPermission) {
                                FloatingWidgetService.startService(context)
                            } else {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        },
                        enabled = !isFloatingActive,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            disabledContainerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("START FLOATING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Button(
                        onClick = {
                            FloatingWidgetService.stopService(context)
                        },
                        enabled = isFloatingActive,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            disabledContainerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("STOP FLOATING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

            // Balance section
            Text(
                text = "💰 আপনার ব্যালেন্স (Wallet Balance)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%.2f ৳", uiState.userBalance),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF34D399)
                )
                Text(
                    text = "প্রতি OTP মূল্য: ${uiState.otpPrice} ৳",
                    fontSize = 11.sp,
                    color = Color(0xFF60A5FA)
                )
            }

            // Withdraw Form
            HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

            Text(
                text = "💸 উইথড্রল রিকোয়েস্ট (Withdrawal Request)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            var selectedMethod by remember { mutableStateOf("Bkash") }
            var detailsInput by remember { mutableStateOf("") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bkash toggle
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedMethod == "Bkash") Color(0xFFE11D48) else Color(0xFF1E293B))
                        .clickable { selectedMethod = "Bkash"; detailsInput = "" }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bkash",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Binance toggle
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedMethod == "Binance") Color(0xFFF59E0B) else Color(0xFF1E293B))
                        .clickable { selectedMethod = "Binance"; detailsInput = "" }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Binance",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Input field based on choice
            OutlinedTextField(
                value = detailsInput,
                onValueChange = { detailsInput = it },
                label = {
                    Text(
                        text = if (selectedMethod == "Bkash") "বিকাশ নাম্বার দিন (Bkash Number)" else "বাইনান্স ইউআইডি দিন (Binance UID)",
                        color = Color(0xFF94A3B8)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (selectedMethod == "Bkash") Color(0xFFE11D48) else Color(0xFFF59E0B),
                    unfocusedBorderColor = Color(0xFF475569)
                )
            )

            // Status message
            if (uiState.withdrawalError != null) {
                Text(
                    text = uiState.withdrawalError ?: "",
                    color = Color(0xFFEF4444),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (uiState.withdrawalSuccess != null) {
                Text(
                    text = uiState.withdrawalSuccess ?: "",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Withdraw action button
            Button(
                onClick = {
                    viewModel.clearWithdrawalMessages()
                    viewModel.submitWithdrawal(selectedMethod, detailsInput)
                },
                enabled = detailsInput.isNotBlank() && uiState.userBalance >= 20.0 && !uiState.isWithdrawalLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    disabledContainerColor = Color(0xFF334155)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                if (uiState.isWithdrawalLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (uiState.userBalance < 20.0) "মিনিমাম উইথড্র ২০ টাকা (Min 20 TK)" else "উইথড্র করুন (Withdraw Full)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Withdrawals History
            if (uiState.withdrawalsList.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)
                Text(
                    text = "📋 উইথড্রল রেকর্ড (Withdrawal History):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.withdrawalsList.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${item.method} (${item.value})",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format("%.2f ৳", item.amount),
                                    color = Color(0xFF34D399),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            // Status badge
                            val badgeColor = when (item.status.lowercase()) {
                                "approved", "complete" -> Color(0xFF10B981)
                                "rejected" -> Color(0xFFEF4444)
                                else -> Color(0xFFF59E0B) // pending
                            }

                            val badgeText = when (item.status.lowercase()) {
                                "approved", "complete" -> "Complete ✅"
                                "rejected" -> "Rejected ❌"
                                else -> "Pending ⏳"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppUpdateDialog(
    uiState: com.example.ui.AccountCreatorUiState,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val updateInfo = uiState.updateInfo ?: return

    val currentVersionName = try {
        val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        pInfo.versionName ?: "1.0"
    } catch (e: Exception) {
        "1.0"
    }

    AlertDialog(
        onDismissRequest = {
            if (!uiState.isDownloadingUpdate) {
                viewModel.dismissUpdateDialog()
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "নতুন আপডেট পাওয়া গেছে! 🚀",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "বর্তমান ভার্সন (Current):",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "v$currentVersionName",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "নতুন ভার্সন (New):",
                            fontSize = 11.sp,
                            color = Color(0xFF2563EB)
                        )
                        Text(
                            text = "v${updateInfo.latestVersionName}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1D4ED8)
                        )
                    }
                }

                if (updateInfo.releaseNotes.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "আপডেটের বিবরণ (What's New):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = updateInfo.releaseNotes,
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                if (uiState.isDownloadingUpdate) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ডাউনলোড হচ্ছে: ${uiState.downloadProgress}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { uiState.downloadProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF2563EB),
                            trackColor = Color(0xFFDBEAFE)
                        )
                    }
                }

                if (uiState.updateDownloadError != null) {
                    Text(
                        text = "ত্রুটি: ${uiState.updateDownloadError}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.startDownloadingAndInstallUpdate(context)
                },
                enabled = !uiState.isDownloadingUpdate,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (uiState.isDownloadingUpdate) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ডাউনলোড হচ্ছে...", fontWeight = FontWeight.Bold)
                } else {
                    Text("UPDATE NOW 📲", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        dismissButton = {
            if (!uiState.isDownloadingUpdate) {
                OutlinedButton(
                    onClick = { viewModel.dismissUpdateDialog() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("পরে করব (Later)", color = Color(0xFF64748B))
                }
            }
        }
    )
}

@Composable
fun EmailCreateTabContent(
    uiState: com.example.ui.AccountCreatorUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCountrySelected: (Country) -> Unit,
    onGenerateRandomEmail: () -> Unit,
    onCreateAccountWithEmail: () -> Unit,
    onCopyEmail: (String) -> Unit,
    onCopyUid: (String) -> Unit,
    onCopyCookies: (String) -> Unit,
    onOpenProxySettings: () -> Unit = {},
    onCheckLive: (String) -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Proxy Status Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌐 Proxy: ${uiState.proxyStatus}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = onOpenProxySettings,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Main EMAIL CREATE Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📧 EMAIL CREATE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    OutlinedButton(
                        onClick = onGenerateRandomEmail,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Text("🎲 Random Email", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Email Input
                OutlinedTextField(
                    value = uiState.emailInput,
                    onValueChange = onEmailChange,
                    placeholder = { Text("e.g. siamahmed532228279714@hotmail.com", color = Color.Gray, fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (uiState.emailInput.isNotBlank()) {
                            IconButton(onClick = { onCopyEmail(uiState.emailInput) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Email", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF2D2D2D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Country Selector Dropdown
                CountryDropdownSelector(
                    selectedCountry = uiState.selectedCountry,
                    enabled = true,
                    onCountrySelected = onCountrySelected
                )

                // Password Input
                OutlinedTextField(
                    value = uiState.passwordInput,
                    onValueChange = onPasswordChange,
                    enabled = true,
                    placeholder = { Text("Password (default: Pass123456)", color = Color.Gray, fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color(0xFF2D2D2D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // EMAIL CREATE Button
                Button(
                    onClick = onCreateAccountWithEmail,
                    enabled = !uiState.isCreating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        disabledContainerColor = Color(0xFF2D2D2D)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    if (uiState.isCreating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Text("CREATING EMAIL ACC...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("⚡ EMAIL CREATE (Auto Copy)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Result Card
        uiState.lastCreatedAccount?.let { account ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("✅ Account Created!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                    Text("Email/Phone: ${account.phone}", fontSize = 11.sp, color = Color.White)
                    Text("UID: ${account.uid}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onCopyUid(account.uid) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            Text("COPY UID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { onCopyCookies(account.cookies) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            Text("COPY COOKIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { onCheckLive(account.uid) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(34.dp)
                    ) {
                        Text("Live Check ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}





