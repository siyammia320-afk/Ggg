package com.example.ui

import android.app.Application
import android.os.Build
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccountDao
import com.example.data.AccountEntity
import com.example.data.AppDatabase
import com.example.data.PreferencesRepository
import com.example.network.FbAccountService
import com.example.network.FbIdentifyService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineScope

import com.example.network.AppConfigService
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

import com.example.data.Country

import com.example.network.VoltxApiService
import com.example.network.MasterKeyService
import com.example.network.VoltxOtpItem
import com.example.network.AppUpdateService
import com.example.network.UpdateInfo
import com.example.util.NotificationHelper
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import java.util.concurrent.atomic.AtomicInteger

data class VoltxActiveNumber(
    val phone: String,
    val rangeCode: String,
    val timestamp: String = "",
    val otp: String? = null,
    val rawMessage: String? = null,
    val isAutoCopied: Boolean = false,
    val accountUid: String? = null
)

data class AccountCreatorUiState(
    val phoneInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val selectedCountry: Country = Country.BANGLADESH,
    val isCreating: Boolean = false,
    val lastCreatedAccount: AccountEntity? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showTelegramDialog: Boolean = false,
    val deviceId: String = "",
    val isActivated: Boolean = true,
    val isAppOn: Boolean = true,
    val isCheckingAppStatus: Boolean = false,
    val appStatusMessage: String = "Checking app status...",
    val facebookRanges: List<String> = emptyList(),
    val customRangeInput: String = "",
    val isLoadingRanges: Boolean = false,
    val isFetchingNumber: Boolean = false,
    val selectedRangeCode: String? = null,
    val activeNumbers: List<VoltxActiveNumber> = emptyList(),
    val lastCopiedOtp: String? = null,
    val proxyServer: String = "",
    val proxyPort: String = "",
    val proxyUsername: String = "",
    val proxyPassword: String = "",
    val isProxyEnabled: Boolean = true,
    val isCustomUserAgentEnabled: Boolean = false,
    val customUserAgent: String = "",
    val proxyStatus: String = "DISCONNECTED",
    val showProxyDialog: Boolean = false,
    val sheetPasswordInput: String = "",
    val sheetUidInput: String = "",
    val sheetCookiesInput: String = "",
    val sheetSavedRecords: List<String> = emptyList(),
    val voltxApiKey: String = "MFSCNKJSFBI",
    val showApiKeyDialog: Boolean = false,
    val isSavingApiKey: Boolean = false,
    val liveStatuses: Map<String, Boolean> = emptyMap(),
    val isCheckingLive: Boolean = false,
    val isLoggedIn: Boolean = false,
    val loggedInEmail: String = "",
    val loggedInFirstName: String = "",
    val loggedInLastName: String = "",
    val loggedInTelegram: String = "",
    val isAuthLoading: Boolean = false,
    val authError: String? = null,
    val authSuccess: String? = null,
    val userBalance: Double = 0.0,
    val otpPrice: Double = 0.5,
    val withdrawalsList: List<com.example.network.WalletService.Withdrawal> = emptyList(),
    val isWithdrawalLoading: Boolean = false,
    val withdrawalError: String? = null,
    val withdrawalSuccess: String? = null,
    val isFindingAccount: Boolean = false,
    val findAccountResult: String? = null,
    val showUpdateDialog: Boolean = false,
    val updateInfo: UpdateInfo? = null,
    val isCheckingUpdate: Boolean = false,
    val isDownloadingUpdate: Boolean = false,
    val downloadProgress: Int = 0,
    val updateDownloadError: String? = null,
    val showTerminalAutoOtp: Boolean = false,
    val isTerminalRunning: Boolean = false,
    val terminalLogs: List<String> = emptyList(),
    val terminalSuccessCount: Int = 0,
    val terminalNoAccountCount: Int = 0,
    val terminalExistCount: Int = 0,
    val terminalFailedCount: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var lastFindAccountTimeMs: Long = 0L
    private var lastLiveCheckTimeMs: Long = 0L

    private val accountDao: AccountDao = AppDatabase.getDatabase(application).accountDao()
    private val prefsRepository = PreferencesRepository(application)

    private val _uiState = MutableStateFlow(AccountCreatorUiState())
    val uiState: StateFlow<AccountCreatorUiState> = _uiState.asStateFlow()

    val accountHistory: StateFlow<List<AccountEntity>> = accountDao.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Load persistent settings on launch
        val savedPassword = prefsRepository.savedPassword
        val savedCountryCode = prefsRepository.selectedCountryCode
        val savedCountry = Country.fromCode(savedCountryCode)
        val showTelegram = !prefsRepository.isTelegramJoined

        val pServer = prefsRepository.proxyServer
        val pPort = prefsRepository.proxyPort
        val pUser = prefsRepository.proxyUsername
        val pPass = prefsRepository.proxyPassword
        val isProxyOn = prefsRepository.isProxyEnabled
        val isCustomUaOn = prefsRepository.isCustomUserAgentEnabled
        val customUa = prefsRepository.customUserAgent

        val savedActiveNumbers = loadActiveNumbersFromPrefs()
        val sheetPass = prefsRepository.sheetPassword
        val sheetRecords = readSheetRecordsFromCsv()
        val savedApiKey = prefsRepository.voltxApiKey

        // Set the active api key in service
        VoltxApiService.currentApiKey = savedApiKey

        val isLoggedInSaved = prefsRepository.isLoggedIn
        val loggedInEmailSaved = prefsRepository.loggedInEmail
        val loggedInFirstNameSaved = prefsRepository.loggedInFirstName
        val loggedInLastNameSaved = prefsRepository.loggedInLastName
        val loggedInTelegramSaved = prefsRepository.loggedInTelegram

        _uiState.value = _uiState.value.copy(
            passwordInput = savedPassword,
            selectedCountry = savedCountry,
            showTelegramDialog = showTelegram,
            isActivated = true,
            isAppOn = true,
            isCheckingAppStatus = true,
            appStatusMessage = "Checking app status...",
            proxyServer = pServer,
            proxyPort = pPort,
            proxyUsername = pUser,
            proxyPassword = pPass,
            isProxyEnabled = isProxyOn,
            isCustomUserAgentEnabled = isCustomUaOn,
            customUserAgent = customUa,
            activeNumbers = savedActiveNumbers,
            sheetPasswordInput = sheetPass,
            sheetSavedRecords = sheetRecords,
            voltxApiKey = savedApiKey,
            isLoggedIn = isLoggedInSaved,
            loggedInEmail = loggedInEmailSaved,
            loggedInFirstName = loggedInFirstNameSaved,
            loggedInLastName = loggedInLastNameSaved,
            loggedInTelegram = loggedInTelegramSaved,
            userBalance = prefsRepository.userBalance.toDouble()
        )

        // Load wallet data on launch if logged in
        if (isLoggedInSaved) {
            refreshWalletData()
        }

        // Start periodic app status check (every 30 seconds)
        startPeriodicAppStatusCheck()

        // Fetch Facebook live ranges
        refreshFacebookRanges()

        // Start OTP polling every 3 seconds
        startOtpPolling()

        // Check for updates on startup
        checkForUpdates(manual = false)
    }

    private fun startPeriodicAppStatusCheck() {
        viewModelScope.launch {
            while (isActive) {
                checkAppStatusInternal()
                if (_uiState.value.isLoggedIn) {
                    refreshWalletData()
                }
                delay(5_000) // check every 5 seconds for real-time app status & wallet updates
            }
        }
    }

    private fun startOtpPolling() {
        viewModelScope.launch {
            while (isActive) {
                checkOtpsInternal()
                delay(3000) // check every 3 seconds as requested
            }
        }
    }

    private fun isPhoneNumberMatch(num1: String, num2: String): Boolean {
        val d1 = num1.replace("\\D".toRegex(), "")
        val d2 = num2.replace("\\D".toRegex(), "")
        if (d1.isEmpty() || d2.isEmpty()) return false
        if (d1 == d2) return true
        if (d1.endsWith(d2) || d2.endsWith(d1)) return true
        if (d1.length >= 8 && d2.length >= 8 && d1.takeLast(8) == d2.takeLast(8)) return true
        return d1.contains(d2) || d2.contains(d1)
    }

    private fun saveActiveNumbersToPrefs(numbers: List<VoltxActiveNumber>) {
        try {
            val array = JSONArray()
            for (item in numbers) {
                val obj = JSONObject()
                obj.put("phone", item.phone)
                obj.put("rangeCode", item.rangeCode)
                obj.put("timestamp", item.timestamp)
                obj.put("otp", item.otp ?: "")
                obj.put("rawMessage", item.rawMessage ?: "")
                obj.put("isAutoCopied", item.isAutoCopied)
                obj.put("accountUid", item.accountUid ?: "")
                array.put(obj)
            }
            prefsRepository.activeNumbersJson = array.toString()
        } catch (_: Exception) {}
    }

    private fun loadActiveNumbersFromPrefs(): List<VoltxActiveNumber> {
        val jsonStr = prefsRepository.activeNumbersJson
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<VoltxActiveNumber>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    VoltxActiveNumber(
                        phone = obj.optString("phone"),
                        rangeCode = obj.optString("rangeCode"),
                        timestamp = obj.optString("timestamp"),
                        otp = if (obj.optString("otp").isNotEmpty()) obj.optString("otp") else null,
                        rawMessage = if (obj.optString("rawMessage").isNotEmpty()) obj.optString("rawMessage") else null,
                        isAutoCopied = obj.optBoolean("isAutoCopied"),
                        accountUid = if (obj.optString("accountUid").isNotEmpty()) obj.optString("accountUid") else null
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private suspend fun checkOtpsInternal() {
        val currentActives = _uiState.value.activeNumbers
        if (currentActives.isEmpty()) return

        val otps = VoltxApiService.checkSuccessOtps()
        if (otps.isEmpty()) return

        val updatedActives = currentActives.map { active ->
            val match = otps.find { isPhoneNumberMatch(it.number, active.phone) }
            if (match != null && match.otp != "N/A") {
                if (!active.isAutoCopied || active.otp != match.otp) {
                    // Auto copy OTP to clipboard and show system notification
                    autoCopyOtpToClipboard(match.otp, active.phone)
                    // Award OTP Price to user
                    awardOtpIncome()
                    active.copy(
                        otp = match.otp,
                        rawMessage = match.message,
                        isAutoCopied = true
                    )
                } else {
                    active
                }
            } else {
                active
            }
        }

        _uiState.value = _uiState.value.copy(activeNumbers = updatedActives)
        saveActiveNumbersToPrefs(updatedActives)
    }

    private fun awardOtpIncome() {
        val email = _uiState.value.loggedInEmail
        if (email.isEmpty()) return
        val currentBalance = _uiState.value.userBalance
        val price = _uiState.value.otpPrice
        val newBalance = currentBalance + price

        viewModelScope.launch {
            val success = com.example.network.WalletService.updateUserBalance(email, newBalance)
            if (success) {
                _uiState.value = _uiState.value.copy(userBalance = newBalance)
                prefsRepository.userBalance = newBalance.toFloat()
            }
        }
    }

    private fun autoCopyOtpToClipboard(otp: String, phone: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("FB_OTP", otp)
        clipboard.setPrimaryClip(clip)

        // Trigger system notification
        NotificationHelper.showOtpNotification(getApplication(), otp, phone)

        _uiState.value = _uiState.value.copy(
            lastCopiedOtp = otp,
            successMessage = "🎉 OTP RECV 💥💣: $otp for $phone!"
        )
    }

    fun manualRefreshOtps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(successMessage = "Refreshing OTP Inbox...")
            checkOtpsInternal()
        }
    }

    fun refreshFacebookRanges() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRanges = true)
            val ranges = VoltxApiService.fetchLiveFacebookRanges()
            _uiState.value = _uiState.value.copy(
                facebookRanges = ranges,
                isLoadingRanges = false
            )
        }
    }

    fun onCustomRangeChanged(range: String) {
        _uiState.value = _uiState.value.copy(customRangeInput = range)
    }

    fun fetchNumberFromCustomRange() {
        val currentState = _uiState.value
        val rangeInput = currentState.customRangeInput.trim()
        if (rangeInput.isEmpty()) {
            _uiState.value = currentState.copy(errorMessage = "দয়া করে রেঞ্জ লিখুন (যেমন: 2250689XXXX)!")
            return
        }

        if (!currentState.isAppOn) {
            _uiState.value = currentState.copy(errorMessage = currentState.appStatusMessage)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isFetchingNumber = true,
                selectedRangeCode = rangeInput,
                errorMessage = null,
                successMessage = "Fetching number for custom range $rangeInput..."
            )

            // 1. Fetch real number from panel API
            val number = VoltxApiService.fetchPhoneNumber(rangeInput)

            if (number.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isFetchingNumber = false,
                    errorMessage = "প্যানেল থেকে এই রেঞ্জের ($rangeInput) কোনো নাম্বার পাওয়া যায়নি! অন্য রেঞ্জ দিয়ে চেষ্টা করুন।"
                )
            } else {
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val newActive = VoltxActiveNumber(
                    phone = number,
                    rangeCode = rangeInput,
                    timestamp = timeStr
                )
                val newActiveList = listOf(newActive) + _uiState.value.activeNumbers

                // Auto copy phone number to clipboard when fetched
                try {
                    val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("PHONE_NUMBER", number)
                    clipboard.setPrimaryClip(clip)
                } catch (_: Exception) {}

                _uiState.value = _uiState.value.copy(
                    isFetchingNumber = false,
                    phoneInput = number,
                    activeNumbers = newActiveList,
                    successMessage = "📋 Number $number received & Auto-Copied to Clipboard!"
                )
                saveActiveNumbersToPrefs(newActiveList)
            }
        }
    }

    fun onRangeClicked(rangeCode: String) {
        val currentState = _uiState.value
        if (!currentState.isAppOn) {
            _uiState.value = currentState.copy(errorMessage = currentState.appStatusMessage)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isFetchingNumber = true,
                selectedRangeCode = rangeCode,
                errorMessage = null,
                successMessage = "Fetching number for range $rangeCode..."
            )

            val number = VoltxApiService.fetchPhoneNumber(rangeCode)
            if (number.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isFetchingNumber = false,
                    errorMessage = "No number available for range $rangeCode. Please try another range!"
                )
            } else {
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val newActive = VoltxActiveNumber(
                    phone = number,
                    rangeCode = rangeCode,
                    timestamp = timeStr
                )
                val newActiveList = listOf(newActive) + _uiState.value.activeNumbers

                // Auto copy phone number to clipboard when fetched
                try {
                    val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("PHONE_NUMBER", number)
                    clipboard.setPrimaryClip(clip)
                } catch (_: Exception) {}

                _uiState.value = _uiState.value.copy(
                    isFetchingNumber = false,
                    phoneInput = number,
                    activeNumbers = newActiveList,
                    successMessage = "📋 Number $number received & Auto-Copied to Clipboard!"
                )
                saveActiveNumbersToPrefs(newActiveList)
            }
        }
    }

    fun clearInbox() {
        _uiState.value = _uiState.value.copy(
            activeNumbers = emptyList(),
            successMessage = "Inbox cleared!"
        )
        saveActiveNumbersToPrefs(emptyList())
    }

    fun checkAppStatusManually() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCheckingAppStatus = true,
                appStatusMessage = "Checking app status..."
            )
            checkAppStatusInternal()
        }
    }

    fun checkDeviceActivationManually() {
        checkAppStatusManually()
    }

    private suspend fun checkAppStatusInternal() {
        val res = AppConfigService.checkAppStatus()
        _uiState.value = _uiState.value.copy(
            isAppOn = res.isAppOn,
            isActivated = true,
            isCheckingAppStatus = false,
            appStatusMessage = res.message
        )
    }

    fun dismissTelegramDialog() {
        prefsRepository.isTelegramJoined = true
        _uiState.value = _uiState.value.copy(showTelegramDialog = false)
    }

    fun openProxyDialog() {
        _uiState.value = _uiState.value.copy(showProxyDialog = true)
    }

    fun closeProxyDialog() {
        _uiState.value = _uiState.value.copy(showProxyDialog = false)
    }

    fun saveProxySettings(
        server: String,
        port: String,
        user: String,
        pass: String,
        isProxyEnabled: Boolean = true,
        isCustomUserAgentEnabled: Boolean = false,
        customUserAgent: String = ""
    ) {
        prefsRepository.proxyServer = server.trim()
        prefsRepository.proxyPort = port.trim()
        prefsRepository.proxyUsername = user.trim()
        prefsRepository.proxyPassword = pass.trim()
        prefsRepository.isProxyEnabled = isProxyEnabled
        prefsRepository.isCustomUserAgentEnabled = isCustomUserAgentEnabled
        prefsRepository.customUserAgent = customUserAgent.trim()

        _uiState.value = _uiState.value.copy(
            proxyServer = server.trim(),
            proxyPort = port.trim(),
            proxyUsername = user.trim(),
            proxyPassword = pass.trim(),
            isProxyEnabled = isProxyEnabled,
            isCustomUserAgentEnabled = isCustomUserAgentEnabled,
            customUserAgent = customUserAgent.trim(),
            showProxyDialog = false,
            successMessage = "Settings Saved!"
        )
    }

    fun onPhoneChanged(newPhone: String) {
        // Read-only: phone number cannot be modified manually from input box
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.value = _uiState.value.copy(passwordInput = newPassword, errorMessage = null)
        prefsRepository.savedPassword = newPassword
    }

    fun onCountrySelected(country: Country) {
        prefsRepository.selectedCountryCode = country.code
        _uiState.value = _uiState.value.copy(selectedCountry = country)
    }

    private fun createAccountWithNumber(phone: String) {
        val currentState = _uiState.value
        val password = currentState.passwordInput.ifEmpty { "Pass123456" }

        val useProxy = currentState.isProxyEnabled && currentState.proxyServer.isNotBlank() && currentState.proxyPort.isNotBlank()

        val proxyServerToUse = if (useProxy) currentState.proxyServer else ""
        val proxyPortToUse = if (useProxy) currentState.proxyPort else ""
        val proxyUserToUse = if (useProxy) currentState.proxyUsername else ""
        val proxyPassToUse = if (useProxy) currentState.proxyPassword else ""

        _uiState.value = currentState.copy(
            isCreating = true,
            errorMessage = null,
            proxyStatus = if (useProxy) "CONNECTING PROXY (${currentState.proxyServer}:${currentState.proxyPort})..." else "CONNECTING PROXY..."
        )

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                // Step 1: Connect proxy
                if (useProxy) {
                    _uiState.value = _uiState.value.copy(proxyStatus = "CONNECTING PROXY (${currentState.proxyServer}:${currentState.proxyPort})...")
                    delay(500) // Connect proxy
                    _uiState.value = _uiState.value.copy(proxyStatus = "CONNECTED (PROXY ACTIVE)")
                } else {
                    _uiState.value = _uiState.value.copy(proxyStatus = "CONNECTING PROXY...")
                    delay(500)
                    _uiState.value = _uiState.value.copy(proxyStatus = "CONNECTED (PROXY ACTIVE)")
                }

                // Step 2: Wait 2 seconds after proxy connection before sending account create request
                delay(2000)

                val result = FbAccountService.createAccount(
                    phoneInput = phone,
                    passwordInput = password,
                    country = currentState.selectedCountry,
                    proxyServer = proxyServerToUse,
                    proxyPort = proxyPortToUse,
                    proxyUsername = proxyUserToUse,
                    proxyPassword = proxyPassToUse,
                    customUserAgent = currentState.customUserAgent,
                    isCustomUserAgentEnabled = currentState.isCustomUserAgentEnabled
                )

                if (result.success) {
                    val entity = AccountEntity(
                        phone = result.phone,
                        uid = result.uid,
                        name = result.name,
                        password = result.password,
                        cookies = result.cookies
                    )
                    val newId = accountDao.insertAccount(entity)
                    val savedEntity = entity.copy(id = newId)

                    // Auto-save created account to account.csv file in /sdcard/ACCOUNT FB/
                    appendRecordToCsv(result.uid, result.password, result.cookies)

                    // Update active number with UID
                    val updatedActives = _uiState.value.activeNumbers.map {
                        if (it.phone == phone) it.copy(accountUid = result.uid) else it
                    }

                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        lastCreatedAccount = savedEntity,
                        activeNumbers = updatedActives,
                        successMessage = "Account Created! UID: ${result.uid}. Auto-saved to account.csv"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = "Account Creation Failed: ${result.error}"
                    )
                }
            } finally {
                // Proxy automatically turns OFF after account creation completes
                _uiState.value = _uiState.value.copy(
                    proxyStatus = "DISCONNECTED (AUTO OFF)"
                )
            }
        }
    }

    fun createAccountOfficial(context: Context) {
        val currentState = _uiState.value
        val phone = currentState.phoneInput.trim()
        if (phone.isEmpty()) {
            _uiState.value = currentState.copy(errorMessage = "Please get a number first")
            return
        }

        val useProxy = currentState.isProxyEnabled && currentState.proxyServer.isNotBlank() && currentState.proxyPort.isNotBlank()
        val proxyServerToUse = if (useProxy) currentState.proxyServer else ""
        val proxyPortToUse = if (useProxy) currentState.proxyPort else ""
        val proxyUserToUse = if (useProxy) currentState.proxyUsername else ""
        val proxyPassToUse = if (useProxy) currentState.proxyPassword else ""

        _uiState.value = currentState.copy(
            isCreating = true,
            errorMessage = null,
            proxyStatus = if (useProxy) "CONNECTING PROXY (${currentState.proxyServer}:${currentState.proxyPort})..." else "CONNECTING PROXY..."
        )

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                if (useProxy) {
                    delay(500)
                    _uiState.value = _uiState.value.copy(proxyStatus = "CONNECTED (PROXY ACTIVE)")
                } else {
                    delay(500)
                    _uiState.value = _uiState.value.copy(proxyStatus = "CONNECTED (PROXY ACTIVE)")
                }

                delay(1000)

                val result = FbAccountService.createAccountOfficial(
                    phoneInput = phone,
                    country = currentState.selectedCountry,
                    proxyServer = proxyServerToUse,
                    proxyPort = proxyPortToUse,
                    proxyUsername = proxyUserToUse,
                    proxyPassword = proxyPassToUse,
                    customUserAgent = currentState.customUserAgent,
                    isCustomUserAgentEnabled = currentState.isCustomUserAgentEnabled
                )

                if (result.success) {
                    val entity = AccountEntity(
                        phone = result.phone,
                        uid = result.uid,
                        name = result.name,
                        password = result.password,
                        cookies = result.cookies
                    )
                    val newId = accountDao.insertAccount(entity)
                    val savedEntity = entity.copy(id = newId)

                    // Auto-save created account to account.csv file in /sdcard/ACCOUNT FB/
                    appendRecordToCsv(result.uid, result.password, result.cookies)

                    // Update active number with UID
                    val updatedActives = _uiState.value.activeNumbers.map {
                        if (it.phone == phone) it.copy(accountUid = result.uid) else it
                    }

                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        lastCreatedAccount = savedEntity,
                        activeNumbers = updatedActives,
                        successMessage = "Account Created! UID: ${result.uid}. Auto-saved to account.csv"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = "Account Creation Failed: ${result.error}"
                    )
                }
            } finally {
                _uiState.value = _uiState.value.copy(
                    proxyStatus = "DISCONNECTED (AUTO OFF)"
                )
            }
        }
    }

    private var terminalJob: Job? = null

    fun openTerminalAutoOtp() {
        _uiState.value = _uiState.value.copy(showTerminalAutoOtp = true)
    }

    fun closeTerminalAutoOtp() {
        _uiState.value = _uiState.value.copy(showTerminalAutoOtp = false)
    }

    private fun appendTerminalLog(log: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val formatted = "[$time] $log"
        _uiState.value = _uiState.value.copy(
            terminalLogs = _uiState.value.terminalLogs + formatted
        )
    }

    fun stopTerminalAutoOtp() {
        terminalJob?.cancel()
        terminalJob = null
        appendTerminalLog("[SYSTEM] 🛑 Stopped by user. Active threads cancelling...")
        _uiState.value = _uiState.value.copy(
            isTerminalRunning = false,
            proxyStatus = "DISCONNECTED (AUTO OFF)"
        )
    }

    fun startTerminalAutoOtp(
        range: String,
        totalAccounts: Int,
        threadCount: Int,
        method: com.example.ui.CreationMethod,
        isFindAccountEnabled: Boolean,
        customPassword: String = "arafat@@##",
        context: Context
    ) {
        if (range.isBlank() || totalAccounts <= 0) return
        val threads = threadCount.coerceIn(1, 20)
        val accountsToCreate = totalAccounts.coerceAtLeast(1)

        terminalJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isTerminalRunning = true,
            terminalLogs = emptyList(),
            terminalSuccessCount = 0,
            terminalNoAccountCount = 0,
            terminalExistCount = 0,
            terminalFailedCount = 0
        )

        val currentState = _uiState.value
        val useProxy = currentState.isProxyEnabled && currentState.proxyServer.isNotBlank() && currentState.proxyPort.isNotBlank()
        val proxyServerToUse = if (useProxy) currentState.proxyServer else ""
        val proxyPortToUse = if (useProxy) currentState.proxyPort else ""
        val proxyUserToUse = if (useProxy) currentState.proxyUsername else ""
        val proxyPassToUse = if (useProxy) currentState.proxyPassword else ""
        val country = currentState.selectedCountry
        val targetPassword = if (customPassword.isNotBlank()) customPassword.trim() else "arafat@@##"

        terminalJob = viewModelScope.launch(Dispatchers.IO) {
            appendTerminalLog("[SYSTEM] 🚀 Terminal Engine Started")
            appendTerminalLog("[CONFIG] Range: $range | Total: $accountsToCreate | Threads: $threads | Method: ${method.title} | Find Account: ${if (isFindAccountEnabled) "ON (Fresh Only)" else "OFF"}")
            if (method == com.example.ui.CreationMethod.NM_LIMIT) {
                appendTerminalLog("[CONFIG] Custom Password: $targetPassword")
            } else {
                appendTerminalLog("[CONFIG] Official Password: arafat@@## (Locked)")
            }
            
            if (useProxy) {
                appendTerminalLog("[PROXY] 🌐 Connecting proxy $proxyServerToUse:$proxyPortToUse...")
                delay(400)
                _uiState.value = _uiState.value.copy(proxyStatus = "CONNECTED ($proxyServerToUse:$proxyPortToUse)")
                appendTerminalLog("[PROXY] 🟢 Connected! Proxy active for all worker threads.")
            } else {
                appendTerminalLog("[PROXY] ⚠️ Direct connection (No proxy configured)")
                _uiState.value = _uiState.value.copy(proxyStatus = "CONNECTED (DIRECT)")
            }

            val completedCount = AtomicInteger(0)
            val successCount = AtomicInteger(0)
            val noAccountCount = AtomicInteger(0)
            val existCount = AtomicInteger(0)
            val failedCount = AtomicInteger(0)

            val queue = Channel<Int>(Channel.UNLIMITED)
            for (i in 1..accountsToCreate) {
                queue.send(i)
            }
            queue.close()

            val workers = (1..threads).map { threadId ->
                launch {
                    for (accountIndex in queue) {
                        if (!isActive) break
                        appendTerminalLog("[THREAD #$threadId] [Account $accountIndex/$accountsToCreate] ⏳ Requesting phone number for range $range...")
                        
                        var phoneNumber: String? = null
                        try {
                            phoneNumber = VoltxApiService.fetchPhoneNumber(range)
                        } catch (e: Exception) {
                            appendTerminalLog("[THREAD #$threadId] [Account $accountIndex] [ERROR] ${e.message}")
                        }

                        if (phoneNumber.isNullOrBlank()) {
                            appendTerminalLog("[THREAD #$threadId] [Account $accountIndex] ⚠️ No number returned. Retrying after 2s...")
                            delay(2000)
                            try {
                                phoneNumber = VoltxApiService.fetchPhoneNumber(range)
                            } catch (e: Exception) {
                                // ignore
                            }
                            if (phoneNumber.isNullOrBlank()) {
                                appendTerminalLog("[THREAD #$threadId] [Account $accountIndex] [FAILED] Could not get phone number. Skipping.")
                                val curFailed = failedCount.incrementAndGet()
                                withContext(Dispatchers.Main) {
                                    _uiState.value = _uiState.value.copy(terminalFailedCount = curFailed)
                                }
                                completedCount.incrementAndGet()
                                continue
                            }
                        }

                        val finalNumber = (phoneNumber ?: "").replace("+", "").trim()
                        appendTerminalLog("[THREAD #$threadId] [Account $accountIndex] 📱 Got Number: +$finalNumber")
                        
                        // Register into active numbers for real-time OTP tracking
                        withContext(Dispatchers.Main) {
                            val activeItem = VoltxActiveNumber(
                                phone = finalNumber,
                                rangeCode = range,
                                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            )
                            val updated = _uiState.value.activeNumbers + activeItem
                            _uiState.value = _uiState.value.copy(activeNumbers = updated)
                            saveActiveNumbersToPrefs(updated)
                        }

                        // If Find Account is ON, check whether account exists on this number
                        if (isFindAccountEnabled) {
                            appendTerminalLog("[THREAD #$threadId] [Account $accountIndex] 🔍 Checking if account exists on +$finalNumber...")
                            val hasAccount = try {
                                FbIdentifyService.checkAccountExists(finalNumber)
                            } catch (e: Exception) {
                                false
                            }

                            if (hasAccount) {
                                val curExist = existCount.incrementAndGet()
                                withContext(Dispatchers.Main) {
                                    _uiState.value = _uiState.value.copy(terminalExistCount = curExist)
                                }
                                appendTerminalLog("[THREAD #$threadId] [Account $accountIndex] ⚠️ Account ALREADY EXISTS on +$finalNumber! Skipping creation.")
                                completedCount.incrementAndGet()
                                delay(600)
                                continue
                            } else {
                                val curNoAcc = noAccountCount.incrementAndGet()
                                withContext(Dispatchers.Main) {
                                    _uiState.value = _uiState.value.copy(terminalNoAccountCount = curNoAcc)
                                }
                                appendTerminalLog("[THREAD #$threadId] [Account $accountIndex] 🟢 No Account Found on +$finalNumber (Fresh Number). Proceeding to create...")
                            }
                        }

                        appendTerminalLog("[THREAD #$threadId] [Account $accountIndex] ⚙️ Executing ${method.title} Registration...")

                        val result = try {
                            if (method == com.example.ui.CreationMethod.NM_OFFICIAL) {
                                FbAccountService.createAccountOfficial(
                                    phoneInput = finalNumber,
                                    country = country,
                                    proxyServer = proxyServerToUse,
                                    proxyPort = proxyPortToUse,
                                    proxyUsername = proxyUserToUse,
                                    proxyPassword = proxyPassToUse,
                                    customUserAgent = currentState.customUserAgent,
                                    isCustomUserAgentEnabled = currentState.isCustomUserAgentEnabled
                                )
                            } else {
                                val pwd = targetPassword
                                FbAccountService.createAccount(
                                    phoneInput = finalNumber,
                                    passwordInput = pwd,
                                    country = country,
                                    proxyServer = proxyServerToUse,
                                    proxyPort = proxyPortToUse,
                                    proxyUsername = proxyUserToUse,
                                    proxyPassword = proxyPassToUse,
                                    customUserAgent = currentState.customUserAgent,
                                    isCustomUserAgentEnabled = currentState.isCustomUserAgentEnabled
                                )
                            }
                        } catch (e: Exception) {
                            com.example.network.AccountResult(
                                success = false,
                                error = e.message ?: "Connection error"
                            )
                        }

                        if (result.success && !result.uid.isNullOrBlank()) {
                            val curSuccess = successCount.incrementAndGet()
                            withContext(Dispatchers.Main) {
                                _uiState.value = _uiState.value.copy(terminalSuccessCount = curSuccess)
                            }
                            appendTerminalLog("[THREAD #$threadId] [Account $accountIndex] [SUCCESS] ✅ UID: ${result.uid} | Name: ${result.name} | Pass: ${result.password}")
                            
                            val entity = AccountEntity(
                                phone = result.phone,
                                uid = result.uid,
                                name = result.name,
                                password = result.password,
                                cookies = result.cookies
                            )
                            val newId = accountDao.insertAccount(entity)
                            val savedEntity = entity.copy(id = newId)

                            appendRecordToCsv(result.uid, result.password, result.cookies)
                            
                            withContext(Dispatchers.Main) {
                                val updatedActives = _uiState.value.activeNumbers.map {
                                    if (it.phone == finalNumber) it.copy(accountUid = result.uid) else it
                                }
                                _uiState.value = _uiState.value.copy(
                                    lastCreatedAccount = savedEntity,
                                    activeNumbers = updatedActives
                                )
                            }
                            appendTerminalLog("[THREAD #$threadId] [Account $accountIndex] 💾 Auto-saved to Database & account.csv (Total Success: $curSuccess/$accountsToCreate)")
                        } else {
                            val curFailed = failedCount.incrementAndGet()
                            withContext(Dispatchers.Main) {
                                _uiState.value = _uiState.value.copy(terminalFailedCount = curFailed)
                            }
                            val err = result.error ?: "Checkpoint / Blocked"
                            appendTerminalLog("[THREAD #$threadId] [Account $accountIndex] [FAILED] ❌ $err")
                        }

                        completedCount.incrementAndGet()
                        delay(800)
                    }
                }
            }

            workers.joinAll()

            val finalSuccess = successCount.get()
            val finalExist = existCount.get()
            val finalNoAcc = noAccountCount.get()
            val finalFailed = failedCount.get()
            appendTerminalLog("[SYSTEM] 🎉 All tasks finished! Success: $finalSuccess | Exist: $finalExist | No Account: $finalNoAcc | Failed: $finalFailed")
            appendTerminalLog("[PROXY] 🔒 Disconnecting proxy automatically (AUTO OFF).")
            _uiState.value = _uiState.value.copy(
                isTerminalRunning = false,
                proxyStatus = "DISCONNECTED (AUTO OFF)"
            )
        }
    }

    fun createAccount() {
        val currentState = _uiState.value
        var phone = currentState.phoneInput.trim()
        if (phone.isEmpty()) {
            val prefixes = listOf("alihasanmiya", "siamahmed", "rakibhasan", "tanvirislam", "nayemhossain", "fahimhasan", "rifatislam", "tanishaakter")
            phone = prefixes.random() + (10000000..999999999).random() + "@hotmail.com"
            _uiState.value = currentState.copy(phoneInput = phone)
        }
        createAccountWithNumber(phone)
    }

    fun generateRandomHotmail() {
        val prefixes = listOf("alihasanmiya", "siamahmed", "rakibhasan", "tanvirislam", "nayemhossain", "fahimhasan", "rifatislam", "tanishaakter")
        val prefix = prefixes.random()
        val randomSuffix = (10000000..999999999).random()
        val email = "$prefix$randomSuffix@hotmail.com"
        _uiState.value = _uiState.value.copy(phoneInput = email)
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(emailInput = email)
    }

    fun generateRandomEmail() {
        val prefixes = listOf("alihasanmiya", "siamahmed", "rakibhasan", "tanvirislam", "nayemhossain", "fahimhasan", "rifatislam", "tanishaakter", "dudfhys", "arafat")
        val prefix = prefixes.random()
        val randomSuffix = (10000000..999999999).random()
        val email = "$prefix$randomSuffix@hotmail.com"
        _uiState.value = _uiState.value.copy(emailInput = email)
    }

    fun createAccountWithEmail(context: Context) {
        val currentState = _uiState.value
        var email = currentState.emailInput.trim()
        if (email.isEmpty()) {
            val prefixes = listOf("alihasanmiya", "siamahmed", "rakibhasan", "tanvirislam", "nayemhossain", "dudfhys", "arafat")
            email = prefixes.random() + (10000000..999999999).random() + "@hotmail.com"
            _uiState.value = _uiState.value.copy(emailInput = email)
        }

        // Auto copy email to clipboard
        copyToClipboard(context, email, "EMAIL")

        val password = currentState.passwordInput.ifEmpty { "Pass123456" }
        val useProxy = currentState.isProxyEnabled && currentState.proxyServer.isNotBlank() && currentState.proxyPort.isNotBlank()

        val proxyServerToUse = if (useProxy) currentState.proxyServer else ""
        val proxyPortToUse = if (useProxy) currentState.proxyPort else ""
        val proxyUserToUse = if (useProxy) currentState.proxyUsername else ""
        val proxyPassToUse = if (useProxy) currentState.proxyPassword else ""

        _uiState.value = currentState.copy(
            isCreating = true,
            errorMessage = null,
            proxyStatus = if (useProxy) "CONNECTING PROXY (${currentState.proxyServer}:${currentState.proxyPort})..." else "CONNECTING PROXY..."
        )

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                if (useProxy) {
                    delay(500)
                    _uiState.value = _uiState.value.copy(proxyStatus = "CONNECTED (PROXY ACTIVE)")
                } else {
                    delay(500)
                    _uiState.value = _uiState.value.copy(proxyStatus = "CONNECTED (PROXY ACTIVE)")
                }

                delay(1000)

                val result = FbAccountService.createAccount(
                    phoneInput = email,
                    passwordInput = password,
                    country = currentState.selectedCountry,
                    proxyServer = proxyServerToUse,
                    proxyPort = proxyPortToUse,
                    proxyUsername = proxyUserToUse,
                    proxyPassword = proxyPassToUse,
                    customUserAgent = currentState.customUserAgent,
                    isCustomUserAgentEnabled = currentState.isCustomUserAgentEnabled
                )

                if (result.success) {
                    val entity = AccountEntity(
                        phone = email,
                        uid = result.uid,
                        name = result.name,
                        password = result.password,
                        cookies = result.cookies
                    )
                    val newId = accountDao.insertAccount(entity)
                    val savedEntity = entity.copy(id = newId)

                    appendRecordToCsv(result.uid, result.password, result.cookies)

                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        lastCreatedAccount = savedEntity,
                        successMessage = "Email Account Created! UID: ${result.uid}. Email auto-copied!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = "Account Creation Failed: ${result.error}"
                    )
                }
            } finally {
                _uiState.value = _uiState.value.copy(
                    proxyStatus = "DISCONNECTED (AUTO OFF)"
                )
            }
        }
    }

    fun findAccount() {
        val currentState = _uiState.value
        val phone = currentState.phoneInput.trim()
        if (phone.isEmpty()) {
            _uiState.value = currentState.copy(errorMessage = "Please get or select a number first!")
            return
        }

        val now = System.currentTimeMillis()
        val elapsed = now - lastFindAccountTimeMs
        if (elapsed < 10000) {
            val secondsLeft = ((10000 - elapsed) / 1000) + 1
            _uiState.value = currentState.copy(
                errorMessage = "১০ সেকেন্ডের আগে চেক করা যাবে না! অনুগ্রহ করে $secondsLeft সেকেন্ড পর আবার চেষ্টা করুন।"
            )
            return
        }
        lastFindAccountTimeMs = now

        _uiState.value = currentState.copy(
            isFindingAccount = true,
            findAccountResult = null
        )

        viewModelScope.launch {
            val exists = com.example.network.FbIdentifyService.checkAccountExists(phone)
            val resultText = if (exists) "Yess ✅" else "No 🚫💥"
            _uiState.value = _uiState.value.copy(
                isFindingAccount = false,
                findAccountResult = resultText
            )
        }
    }

    fun clearAllAccounts() {
        viewModelScope.launch {
            accountDao.clearAllAccounts()
            _uiState.value = _uiState.value.copy(
                lastCreatedAccount = null,
                successMessage = "All saved accounts deleted!"
            )
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            accountDao.deleteAccountById(id)
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label Copied!", Toast.LENGTH_SHORT).show()
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    // SHEET TAB LOGIC
    fun onSheetPasswordChanged(pass: String) {
        _uiState.value = _uiState.value.copy(sheetPasswordInput = pass)
    }

    fun saveSheetPassword(pass: String) {
        val trimmed = pass.trim()
        prefsRepository.sheetPassword = trimmed
        _uiState.value = _uiState.value.copy(
            sheetPasswordInput = trimmed,
            successMessage = "Sheet password saved successfully!"
        )
    }

    fun onSheetUidChanged(uid: String) {
        val raw = uid.trim()
        if (raw.contains("\t")) {
            val parts = raw.split("\t").map { it.replace("\"", "").trim() }.filter { it.isNotEmpty() }
            if (parts.isNotEmpty() && !parts[0].contains("=")) {
                val extractedUid = parts[0]
                if (parts.size == 2) {
                    val secondPart = parts[1]
                    if (secondPart.contains("=")) {
                        _uiState.value = _uiState.value.copy(
                            sheetUidInput = extractedUid,
                            sheetCookiesInput = cleanCookiesString(secondPart)
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            sheetUidInput = extractedUid
                        )
                        prefsRepository.sheetPassword = secondPart
                        _uiState.value = _uiState.value.copy(
                            sheetPasswordInput = secondPart
                        )
                    }
                } else {
                    val extractedPassword = parts[1]
                    val extractedCookiesList = parts.subList(2, parts.size)
                    val rawCookiesCombined = extractedCookiesList.joinToString("; ")
                    val cleanCookies = cleanCookiesString(rawCookiesCombined)
                    
                    _uiState.value = _uiState.value.copy(
                        sheetUidInput = extractedUid,
                        sheetCookiesInput = cleanCookies
                    )
                    if (extractedPassword.isNotEmpty()) {
                        prefsRepository.sheetPassword = extractedPassword
                        _uiState.value = _uiState.value.copy(
                            sheetPasswordInput = extractedPassword
                        )
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(sheetUidInput = uid)
            }
        } else {
            _uiState.value = _uiState.value.copy(sheetUidInput = uid)
        }
    }

    fun onSheetCookiesChanged(cookies: String) {
        val raw = cookies.trim()
        if (raw.contains("\t")) {
            val parts = raw.split("\t").map { it.replace("\"", "").trim() }.filter { it.isNotEmpty() }
            if (parts.isNotEmpty() && !parts[0].contains("=")) {
                val extractedUid = parts[0]
                if (parts.size == 2) {
                    val secondPart = parts[1]
                    if (secondPart.contains("=")) {
                        _uiState.value = _uiState.value.copy(
                            sheetUidInput = extractedUid,
                            sheetCookiesInput = cleanCookiesString(secondPart)
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            sheetUidInput = extractedUid
                        )
                        prefsRepository.sheetPassword = secondPart
                        _uiState.value = _uiState.value.copy(
                            sheetPasswordInput = secondPart
                        )
                    }
                } else {
                    val extractedPassword = parts[1]
                    val extractedCookiesList = parts.subList(2, parts.size)
                    val rawCookiesCombined = extractedCookiesList.joinToString("; ")
                    val cleanCookies = cleanCookiesString(rawCookiesCombined)
                    
                    _uiState.value = _uiState.value.copy(
                        sheetUidInput = extractedUid,
                        sheetCookiesInput = cleanCookies
                    )
                    if (extractedPassword.isNotEmpty()) {
                        prefsRepository.sheetPassword = extractedPassword
                        _uiState.value = _uiState.value.copy(
                            sheetPasswordInput = extractedPassword
                        )
                    }
                }
            } else {
                val cleanedJoined = parts.joinToString("; ")
                _uiState.value = _uiState.value.copy(sheetCookiesInput = cleanCookiesString(cleanedJoined))
            }
        } else {
            _uiState.value = _uiState.value.copy(sheetCookiesInput = cleanCookiesString(cookies))
        }
    }

    private fun getAccountFbFile(): java.io.File {
        // Try root external storage directory first: /storage/emulated/0/ACCOUNT FB
        val rootDir = java.io.File(android.os.Environment.getExternalStorageDirectory(), "ACCOUNT FB")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (android.os.Environment.isExternalStorageManager()) {
                try {
                    if (!rootDir.exists()) {
                        rootDir.mkdirs()
                    }
                    return java.io.File(rootDir, "account.csv")
                } catch (_: Exception) {}
            }
        } else {
            // Android 10 or below, can write to root directory with WRITE_EXTERNAL_STORAGE permission
            try {
                if (!rootDir.exists()) {
                    rootDir.mkdirs()
                }
                return java.io.File(rootDir, "account.csv")
            } catch (_: Exception) {}
        }

        // Fallback 1: Public Download folder which is always accessible without permissions on Android 10+: /storage/emulated/0/Download/ACCOUNT FB
        val downloadDir = java.io.File(
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
            "ACCOUNT FB"
        )
        try {
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            return java.io.File(downloadDir, "account.csv")
        } catch (_: Exception) {}

        // Fallback 2: Private app files directory
        val context = getApplication<Application>()
        val privateDir = java.io.File(context.getExternalFilesDir(null), "ACCOUNT FB")
        try {
            if (!privateDir.exists()) {
                privateDir.mkdirs()
            }
        } catch (_: Exception) {}
        return java.io.File(privateDir, "account.csv")
    }

    private fun readSheetRecordsFromCsv(): List<String> {
        return try {
            val file = getAccountFbFile()
            if (file.exists()) {
                file.readLines().filter { it.isNotBlank() }
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun cleanCookiesString(rawCookies: String): String {
        val cleanRaw = rawCookies
            .replace("\r", "")
            .replace("\n", "")
            .replace("\t", " ") // replace tabs with space to prevent breaking columns
            .replace("\"", "")  // remove double quotes
            .trim()

        if (!cleanRaw.contains("=")) {
            return cleanRaw
        }

        val parts = cleanRaw.split(";")
        val cleanedParts = parts.map { part ->
            part.trim()
        }.filter { it.isNotEmpty() && it.contains("=") }

        return if (cleanedParts.isEmpty()) cleanRaw else cleanedParts.joinToString("; ")
    }

    fun appendRecordToCsv(uid: String, password: String, cookies: String) {
        val cleanUid = uid.trim().replace("\t", "").replace("\"", "")
        val cleanCookies = cleanCookiesString(cookies)
        val cleanPassword = password.trim().replace("\t", "").replace("\"", "")

        if (cleanUid.isEmpty()) return

        try {
            val file = getAccountFbFile()
            val recordLine = if (cleanCookies.isNotEmpty()) "$cleanUid\t$cleanPassword\t$cleanCookies" else "$cleanUid\t$cleanPassword\t"
            file.appendText("$recordLine\n")
            
            // Reload records for Sheet tab UI
            _uiState.value = _uiState.value.copy(
                sheetSavedRecords = readSheetRecordsFromCsv()
            )
        } catch (_: Exception) {}
    }

    fun saveSheetRecord() {
        val state = _uiState.value
        val uid = state.sheetUidInput.trim().replace("\t", "").replace("\"", "")
        val rawCookies = state.sheetCookiesInput.trim()
        val cookies = cleanCookiesString(rawCookies)
        val password = prefsRepository.sheetPassword.ifBlank { "Pass123456" }.trim().replace("\t", "").replace("\"", "")

        if (uid.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Please enter UID!")
            return
        }

        try {
            val file = getAccountFbFile()
            // Column A = UID, Column B = Password, Column C = Cookies (Separated by Tab \t)
            val recordLine = if (cookies.isNotEmpty()) "$uid\t$password\t$cookies" else "$uid\t$password\t"
            file.appendText("$recordLine\n")

            val updatedRecords = readSheetRecordsFromCsv()

            _uiState.value = _uiState.value.copy(
                sheetUidInput = "",
                sheetCookiesInput = "",
                sheetSavedRecords = updatedRecords,
                successMessage = "Saved successfully!\nLocation: ${file.absolutePath}"
            )
        } catch (e: Exception) {
            _uiState.value = state.copy(errorMessage = "Error saving to file: ${e.message}")
        }
    }

    fun clearSheetFile() {
        try {
            val file = getAccountFbFile()
            if (file.exists()) {
                file.writeText("")
            }
            _uiState.value = _uiState.value.copy(
                sheetSavedRecords = emptyList(),
                successMessage = "Cleared file at:\n${file.absolutePath}"
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = "Failed to clear file: ${e.message}")
        }
    }

    fun openApiKeyDialog() {
        _uiState.value = _uiState.value.copy(showApiKeyDialog = true)
    }

    fun closeApiKeyDialog() {
        _uiState.value = _uiState.value.copy(showApiKeyDialog = false)
    }

    fun updateApiKeyInput(newKey: String) {
        _uiState.value = _uiState.value.copy(voltxApiKey = newKey)
    }

    fun saveApiKey(masterKey: String, newKey: String) {
        val trimmedMaster = masterKey.trim()
        val trimmedApi = newKey.trim()

        if (trimmedMaster.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "মাস্টার কী ফাঁকা রাখা যাবে না!")
            return
        }
        if (trimmedApi.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "নতুন API কী ফাঁকা রাখা যাবে না!")
            return
        }

        _uiState.value = _uiState.value.copy(isSavingApiKey = true, errorMessage = null, successMessage = null)

        viewModelScope.launch {
            val validation = MasterKeyService.validateMasterKey(trimmedMaster)
            if (!validation.isValid) {
                _uiState.value = _uiState.value.copy(
                    isSavingApiKey = false,
                    errorMessage = validation.errorMessage.ifEmpty { "মাস্টার কী সঠিক নয় বা নেটওয়ার্ক এরর!" }
                )
                return@launch
            }

            if (validation.isUsed) {
                _uiState.value = _uiState.value.copy(
                    isSavingApiKey = false,
                    errorMessage = "এই মাস্টার কী-টি ইতিমধ্যে ব্যবহার করা হয়েছে! নতুন কী ব্যবহার করুন।"
                )
                return@launch
            }

            val success = MasterKeyService.claimMasterKey(trimmedMaster, trimmedApi)
            if (success) {
                prefsRepository.voltxApiKey = trimmedApi
                VoltxApiService.currentApiKey = trimmedApi
                _uiState.value = _uiState.value.copy(
                    isSavingApiKey = false,
                    voltxApiKey = trimmedApi,
                    showApiKeyDialog = false,
                    successMessage = "API Key সফলভাবে পরিবর্তন করা হয়েছে!"
                )
                refreshFacebookRanges()
            } else {
                _uiState.value = _uiState.value.copy(
                    isSavingApiKey = false,
                    errorMessage = "সার্ভারে মাস্টার কী রেজিস্টার করতে ব্যর্থ হয়েছে! অনুগ্রহ করে আবার চেষ্টা করুন।"
                )
            }
        }
    }

    fun checkLiveStatusForSavedAccounts() {
        val accounts = accountHistory.value
        if (accounts.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "কোনো সেভ একাউন্ট খুঁজে পাওয়া যায়নি!")
            return
        }

        val now = System.currentTimeMillis()
        val elapsed = now - lastLiveCheckTimeMs
        if (elapsed < 10000) {
            val secondsLeft = ((10000 - elapsed) / 1000) + 1
            _uiState.value = _uiState.value.copy(
                errorMessage = "১০ সেকেন্ডের আগে লাইভ চেক করা যাবে না! অনুগ্রহ করে $secondsLeft সেকেন্ড পর আবার চেষ্টা করুন।"
            )
            return
        }
        lastLiveCheckTimeMs = now

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingLive = true)
            val uids = accounts.map { it.uid }
            val results = com.example.network.LiveCheckService.checkLiveUids(uids)
            val updatedMap = _uiState.value.liveStatuses.toMutableMap()
            updatedMap.putAll(results)
            _uiState.value = _uiState.value.copy(
                isCheckingLive = false,
                liveStatuses = updatedMap,
                successMessage = "লাইভ চেক সম্পন্ন হয়েছে!"
            )
        }
    }

    fun checkLiveStatusForSingleAccount(uid: String) {
        if (uid.isBlank()) return

        val now = System.currentTimeMillis()
        val elapsed = now - lastLiveCheckTimeMs
        if (elapsed < 10000) {
            val secondsLeft = ((10000 - elapsed) / 1000) + 1
            _uiState.value = _uiState.value.copy(
                errorMessage = "১০ সেকেন্ডের আগে লাইভ চেক করা যাবে না! অনুগ্রহ করে $secondsLeft সেকেন্ড পর আবার চেষ্টা করুন।"
            )
            return
        }
        lastLiveCheckTimeMs = now

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingLive = true)
            val results = com.example.network.LiveCheckService.checkLiveUids(listOf(uid))
            val updatedMap = _uiState.value.liveStatuses.toMutableMap()
            updatedMap.putAll(results)
            _uiState.value = _uiState.value.copy(
                isCheckingLive = false,
                liveStatuses = updatedMap,
                successMessage = "লাইভ চেক সম্পন্ন হয়েছে!"
            )
        }
    }

    fun signUpUser(firstName: String, lastName: String, telegramUsername: String, email: String, password: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuthLoading = true, authError = null, authSuccess = null)
            val result = com.example.network.AuthService.signUp(firstName, lastName, telegramUsername, email, password)
            if (result.success) {
                prefsRepository.isLoggedIn = true
                prefsRepository.loggedInEmail = email.trim().lowercase()
                prefsRepository.loggedInFirstName = result.firstName
                prefsRepository.loggedInLastName = result.lastName
                prefsRepository.loggedInTelegram = result.telegramUsername

                _uiState.value = _uiState.value.copy(
                    isAuthLoading = false,
                    isLoggedIn = true,
                    loggedInEmail = email.trim().lowercase(),
                    loggedInFirstName = result.firstName,
                    loggedInLastName = result.lastName,
                    loggedInTelegram = result.telegramUsername,
                    authSuccess = result.message
                )
                refreshWalletData()
                onComplete(true, result.message)
            } else {
                _uiState.value = _uiState.value.copy(
                    isAuthLoading = false,
                    authError = result.message
                )
                onComplete(false, result.message)
            }
        }
    }

    fun logInUser(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuthLoading = true, authError = null, authSuccess = null)
            val result = com.example.network.AuthService.logIn(email, password)
            if (result.success) {
                prefsRepository.isLoggedIn = true
                prefsRepository.loggedInEmail = email.trim().lowercase()
                prefsRepository.loggedInFirstName = result.firstName
                prefsRepository.loggedInLastName = result.lastName
                prefsRepository.loggedInTelegram = result.telegramUsername

                _uiState.value = _uiState.value.copy(
                    isAuthLoading = false,
                    isLoggedIn = true,
                    loggedInEmail = email.trim().lowercase(),
                    loggedInFirstName = result.firstName,
                    loggedInLastName = result.lastName,
                    loggedInTelegram = result.telegramUsername,
                    authSuccess = result.message
                )
                refreshWalletData()
                onComplete(true, result.message)
            } else {
                _uiState.value = _uiState.value.copy(
                    isAuthLoading = false,
                    authError = result.message
                )
                onComplete(false, result.message)
            }
        }
    }

    fun logOutUser() {
        prefsRepository.isLoggedIn = false
        prefsRepository.loggedInEmail = ""
        prefsRepository.loggedInFirstName = ""
        prefsRepository.loggedInLastName = ""
        prefsRepository.loggedInTelegram = ""

        _uiState.value = _uiState.value.copy(
            isLoggedIn = false,
            loggedInEmail = "",
            loggedInFirstName = "",
            loggedInLastName = "",
            loggedInTelegram = "",
            authSuccess = "লগআউট সফল হয়েছে!"
        )
    }

    fun clearAuthMessages() {
        _uiState.value = _uiState.value.copy(authError = null, authSuccess = null)
    }

    fun refreshWalletData() {
        val email = _uiState.value.loggedInEmail
        if (email.isEmpty()) return
        viewModelScope.launch {
            val balance = com.example.network.WalletService.fetchUserBalance(email)
            val price = com.example.network.WalletService.fetchOtpPrice()
            val withdrawals = com.example.network.WalletService.fetchUserWithdrawals(email)

            prefsRepository.userBalance = balance.toFloat()

            _uiState.value = _uiState.value.copy(
                userBalance = balance,
                otpPrice = price,
                withdrawalsList = withdrawals
            )
        }
    }

    fun submitWithdrawal(method: String, details: String) {
        val email = _uiState.value.loggedInEmail
        val firstName = _uiState.value.loggedInFirstName
        val lastName = _uiState.value.loggedInLastName
        val name = "$firstName $lastName".trim()
        val amount = _uiState.value.userBalance

        if (email.isEmpty()) return

        _uiState.value = _uiState.value.copy(
            isWithdrawalLoading = true,
            withdrawalError = null,
            withdrawalSuccess = null
        )

        viewModelScope.launch {
            val (success, message) = com.example.network.WalletService.requestWithdrawal(
                email = email,
                name = name,
                method = method,
                value = details,
                amount = amount
            )

            if (success) {
                _uiState.value = _uiState.value.copy(
                    isWithdrawalLoading = false,
                    withdrawalSuccess = message
                )
                refreshWalletData()
            } else {
                _uiState.value = _uiState.value.copy(
                    isWithdrawalLoading = false,
                    withdrawalError = message
                )
            }
        }
    }

    fun clearWithdrawalMessages() {
        _uiState.value = _uiState.value.copy(withdrawalError = null, withdrawalSuccess = null)
    }

    fun checkForUpdates(manual: Boolean = false, context: Context? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingUpdate = true)
            val appCtx = getApplication<Application>()
            val currentVersion = try {
                val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    appCtx.packageManager.getPackageInfo(appCtx.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    appCtx.packageManager.getPackageInfo(appCtx.packageName, 0)
                }
                pInfo.versionName ?: "1.0"
            } catch (e: Exception) {
                "1.0"
            }
            val updateInfo = AppUpdateService.checkForUpdate(currentVersion)
            _uiState.value = _uiState.value.copy(isCheckingUpdate = false)

            if (updateInfo.hasUpdate) {
                _uiState.value = _uiState.value.copy(
                    showUpdateDialog = true,
                    updateInfo = updateInfo
                )
            } else if (manual && context != null) {
                Toast.makeText(context, "You are using the latest version (v$currentVersion)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startDownloadingAndInstallUpdate(context: Context) {
        val url = _uiState.value.updateInfo?.downloadUrl ?: return
        if (_uiState.value.isDownloadingUpdate) return

        _uiState.value = _uiState.value.copy(
            isDownloadingUpdate = true,
            downloadProgress = 0,
            updateDownloadError = null
        )

        viewModelScope.launch {
            val apkFile = AppUpdateService.downloadApk(context, url) { progress ->
                _uiState.value = _uiState.value.copy(downloadProgress = progress)
            }

            if (apkFile != null && apkFile.exists()) {
                _uiState.value = _uiState.value.copy(
                    isDownloadingUpdate = false,
                    showUpdateDialog = false
                )
                val launched = AppUpdateService.installApk(context, apkFile)
                if (!launched) {
                    _uiState.value = _uiState.value.copy(
                        updateDownloadError = "Unable to start package installer. Please allow install permissions in Android settings."
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isDownloadingUpdate = false,
                    updateDownloadError = "Failed to download update APK. Please check your internet connection."
                )
            }
        }
    }

    fun dismissUpdateDialog() {
        if (!_uiState.value.isDownloadingUpdate) {
            _uiState.value = _uiState.value.copy(showUpdateDialog = false)
        }
    }
}

