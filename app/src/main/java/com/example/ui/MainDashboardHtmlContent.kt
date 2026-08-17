package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.AccountEntity
import com.example.data.Country
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MainDashboardHtmlContent(
    uiState: AccountCreatorUiState,
    accountsHistory: List<AccountEntity>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onRangeClicked: (String) -> Unit,
    onFetchCustomRange: () -> Unit,
    onCustomRangeChange: (String) -> Unit,
    onRefreshRanges: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onCheckActivation: () -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onCountrySelected: (Country) -> Unit,
    onCreateAccount: () -> Unit,
    onFindAccount: () -> Unit,
    onCreateOfficialAccount: () -> Unit,
    onCreateEmailAccount: () -> Unit,
    onGenerateRandomEmail: () -> Unit,
    onOpenProxySettings: () -> Unit,
    onCheckLiveSingle: (String) -> Unit,
    onCheckLiveAll: () -> Unit,
    onDeleteAccount: (AccountEntity) -> Unit,
    onClearAllAccounts: () -> Unit,
    onClearInbox: () -> Unit,
    onReloadInbox: () -> Unit,
    onDismissMessage: () -> Unit
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Sync full state to HTML Dashboard
    LaunchedEffect(
        selectedTabIndex,
        uiState.isCreating,
        uiState.isFetchingNumber,
        uiState.isFindingAccount,
        uiState.isCheckingLive,
        uiState.phoneInput,
        uiState.emailInput,
        uiState.passwordInput,
        uiState.selectedCountry,
        uiState.lastCreatedAccount,
        uiState.errorMessage,
        uiState.successMessage,
        uiState.proxyStatus,
        uiState.deviceId,
        uiState.isActivated,
        uiState.facebookRanges,
        uiState.activeNumbers,
        accountsHistory,
        uiState.liveStatuses
    ) {
        val webView = webViewRef ?: return@LaunchedEffect

        val stateJson = JSONObject().apply {
            put("selectedTab", selectedTabIndex)
            put("isCreating", uiState.isCreating)
            put("isFetchingNumber", uiState.isFetchingNumber)
            put("isFindingAccount", uiState.isFindingAccount)
            put("isCheckingLive", uiState.isCheckingLive)
            put("phoneInput", uiState.phoneInput)
            put("emailInput", uiState.emailInput)
            put("passwordInput", uiState.passwordInput)
            put("selectedCountryIndex", Country.values().indexOf(uiState.selectedCountry))
            put("proxyStatus", uiState.proxyStatus)
            put("deviceId", uiState.deviceId)
            put("isActivated", uiState.isActivated)
            put("errorMessage", uiState.errorMessage ?: "")
            put("successMessage", uiState.successMessage ?: "")

            // Ranges
            put("ranges", JSONArray(uiState.facebookRanges))

            // Last Created Account
            if (uiState.lastCreatedAccount != null) {
                val accObj = JSONObject().apply {
                    put("uid", uiState.lastCreatedAccount.uid)
                    put("phoneOrEmail", uiState.lastCreatedAccount.phone)
                    put("password", uiState.lastCreatedAccount.password)
                    put("cookies", uiState.lastCreatedAccount.cookies)
                    put("timestamp", uiState.lastCreatedAccount.createdAt)
                }
                put("lastAccount", accObj)
            } else {
                put("lastAccount", JSONObject.NULL)
            }

            // Active OTP Numbers
            val activeArr = JSONArray()
            uiState.activeNumbers.forEach { item ->
                val itObj = JSONObject().apply {
                    put("phone", item.phone)
                    put("rangeCode", item.rangeCode)
                    put("timestamp", item.timestamp)
                    put("otp", item.otp ?: "")
                    put("accountUid", item.accountUid ?: "")
                }
                activeArr.put(itObj)
            }
            put("activeNumbers", activeArr)

            // History
            val historyArr = JSONArray()
            accountsHistory.forEach { acc ->
                val hObj = JSONObject().apply {
                    put("uid", acc.uid)
                    put("phoneOrEmail", acc.phone)
                    put("password", acc.password)
                    put("cookies", acc.cookies)
                    put("timestamp", acc.createdAt)
                    put("isLive", uiState.liveStatuses[acc.uid])
                }
                historyArr.put(hObj)
            }
            put("accountsHistory", historyArr)
        }

        val script = "if (window.renderDashboard) { window.renderDashboard(" + stateJson.toString() + "); }"
        Handler(Looper.getMainLooper()).post {
            webView.evaluateJavascript(script, null)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    setBackgroundColor(0xFF0B0F19.toInt())

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Immediately push initial state once page finishes loading
                            val stateJson = JSONObject().apply {
                                put("selectedTab", selectedTabIndex)
                                put("isCreating", uiState.isCreating)
                                put("isFetchingNumber", uiState.isFetchingNumber)
                                put("isFindingAccount", uiState.isFindingAccount)
                                put("isCheckingLive", uiState.isCheckingLive)
                                put("phoneInput", uiState.phoneInput)
                                put("emailInput", uiState.emailInput)
                                put("passwordInput", uiState.passwordInput)
                                put("selectedCountryIndex", Country.values().indexOf(uiState.selectedCountry))
                                put("proxyStatus", uiState.proxyStatus)
                                put("deviceId", uiState.deviceId)
                                put("isActivated", uiState.isActivated)
                                put("errorMessage", uiState.errorMessage ?: "")
                                put("successMessage", uiState.successMessage ?: "")
                                put("ranges", JSONArray(uiState.facebookRanges))
                                if (uiState.lastCreatedAccount != null) {
                                    val accObj = JSONObject().apply {
                                        put("uid", uiState.lastCreatedAccount.uid)
                                        put("phoneOrEmail", uiState.lastCreatedAccount.phone)
                                        put("password", uiState.lastCreatedAccount.password)
                                        put("cookies", uiState.lastCreatedAccount.cookies)
                                        put("timestamp", uiState.lastCreatedAccount.createdAt)
                                    }
                                    put("lastAccount", accObj)
                                } else {
                                    put("lastAccount", JSONObject.NULL)
                                }
                                val activeArr = JSONArray()
                                uiState.activeNumbers.forEach { item ->
                                    val itObj = JSONObject().apply {
                                        put("phone", item.phone)
                                        put("rangeCode", item.rangeCode)
                                        put("timestamp", item.timestamp)
                                        put("otp", item.otp ?: "")
                                        put("accountUid", item.accountUid ?: "")
                                    }
                                    activeArr.put(itObj)
                                }
                                put("activeNumbers", activeArr)
                                val historyArr = JSONArray()
                                accountsHistory.forEach { acc ->
                                    val hObj = JSONObject().apply {
                                        put("uid", acc.uid)
                                        put("phoneOrEmail", acc.phone)
                                        put("password", acc.password)
                                        put("cookies", acc.cookies)
                                        put("timestamp", acc.createdAt)
                                        put("isLive", uiState.liveStatuses[acc.uid])
                                    }
                                    historyArr.put(hObj)
                                }
                                put("accountsHistory", historyArr)
                            }
                            val script = "if (window.renderDashboard) { window.renderDashboard(" + stateJson.toString() + "); }"
                            view?.evaluateJavascript(script, null)
                        }
                    }

                    class AndroidDashboardBridge {
                        @JavascriptInterface
                        fun switchTab(tabIndex: Int) {
                            Handler(Looper.getMainLooper()).post {
                                onTabSelected(tabIndex)
                            }
                        }

                        @JavascriptInterface
                        fun selectRange(range: String) {
                            Handler(Looper.getMainLooper()).post {
                                onRangeClicked(range)
                            }
                        }

                        @JavascriptInterface
                        fun fetchCustomRange(range: String) {
                            Handler(Looper.getMainLooper()).post {
                                onCustomRangeChange(range)
                                onFetchCustomRange()
                            }
                        }

                        @JavascriptInterface
                        fun refreshRanges() {
                            Handler(Looper.getMainLooper()).post {
                                onRefreshRanges()
                            }
                        }

                        @JavascriptInterface
                        fun copyText(text: String, label: String) {
                            Handler(Looper.getMainLooper()).post {
                                onCopyText(text, label)
                            }
                        }

                        @JavascriptInterface
                        fun checkActivation() {
                            Handler(Looper.getMainLooper()).post {
                                onCheckActivation()
                            }
                        }

                        @JavascriptInterface
                        fun setPhoneInput(phone: String) {
                            Handler(Looper.getMainLooper()).post {
                                onPhoneChange(phone)
                            }
                        }

                        @JavascriptInterface
                        fun setPasswordInput(pass: String) {
                            Handler(Looper.getMainLooper()).post {
                                onPasswordChange(pass)
                            }
                        }

                        @JavascriptInterface
                        fun setEmailInput(email: String) {
                            Handler(Looper.getMainLooper()).post {
                                onEmailChange(email)
                            }
                        }

                        @JavascriptInterface
                        fun selectCountry(countryIndex: Int) {
                            Handler(Looper.getMainLooper()).post {
                                val countries = Country.values()
                                if (countryIndex in countries.indices) {
                                    onCountrySelected(countries[countryIndex])
                                }
                            }
                        }

                        @JavascriptInterface
                        fun createAccount(phone: String, pass: String, countryIndex: Int) {
                            Handler(Looper.getMainLooper()).post {
                                onPhoneChange(phone)
                                onPasswordChange(pass)
                                selectCountry(countryIndex)
                                onCreateAccount()
                            }
                        }

                        @JavascriptInterface
                        fun findAccount(phone: String, countryIndex: Int) {
                            Handler(Looper.getMainLooper()).post {
                                onPhoneChange(phone)
                                selectCountry(countryIndex)
                                onFindAccount()
                            }
                        }

                        @JavascriptInterface
                        fun createOfficialAccount(phone: String, countryIndex: Int) {
                            Handler(Looper.getMainLooper()).post {
                                onPhoneChange(phone)
                                selectCountry(countryIndex)
                                onCreateOfficialAccount()
                            }
                        }

                        @JavascriptInterface
                        fun createEmailAccount(email: String, pass: String, countryIndex: Int) {
                            Handler(Looper.getMainLooper()).post {
                                onEmailChange(email)
                                onPasswordChange(pass)
                                selectCountry(countryIndex)
                                onCreateEmailAccount()
                            }
                        }

                        @JavascriptInterface
                        fun generateRandomEmail() {
                            Handler(Looper.getMainLooper()).post {
                                onGenerateRandomEmail()
                            }
                        }

                        @JavascriptInterface
                        fun openProxySettings() {
                            Handler(Looper.getMainLooper()).post {
                                onOpenProxySettings()
                            }
                        }

                        @JavascriptInterface
                        fun checkLiveSingle(uid: String) {
                            Handler(Looper.getMainLooper()).post {
                                onCheckLiveSingle(uid)
                            }
                        }

                        @JavascriptInterface
                        fun checkLiveAll() {
                            Handler(Looper.getMainLooper()).post {
                                onCheckLiveAll()
                            }
                        }

                        @JavascriptInterface
                        fun deleteAccount(uid: String) {
                            Handler(Looper.getMainLooper()).post {
                                val acc = accountsHistory.find { it.uid == uid }
                                if (acc != null) {
                                    onDeleteAccount(acc)
                                }
                            }
                        }

                        @JavascriptInterface
                        fun clearAllAccounts() {
                            Handler(Looper.getMainLooper()).post {
                                onClearAllAccounts()
                            }
                        }

                        @JavascriptInterface
                        fun clearInbox() {
                            Handler(Looper.getMainLooper()).post {
                                onClearInbox()
                            }
                        }

                        @JavascriptInterface
                        fun reloadInbox() {
                            Handler(Looper.getMainLooper()).post {
                                onReloadInbox()
                            }
                        }

                        @JavascriptInterface
                        fun dismissMessage() {
                            Handler(Looper.getMainLooper()).post {
                                onDismissMessage()
                            }
                        }
                    }

                    addJavascriptInterface(AndroidDashboardBridge(), "AndroidBridge")
                    loadDataWithBaseURL("https://local.dashboard/", getDashboardHtml(), "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = {
                webViewRef = it
            }
        )
    }
}

private fun getDashboardHtml(): String {
    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <title>Dashboard</title>
        <style>
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
                -webkit-tap-highlight-color: transparent;
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, monospace;
            }
            html, body {
                width: 100%;
                height: 100%;
                background-color: #0b0f19;
                color: #f1f5f9;
                overflow: hidden;
            }
            body {
                display: flex;
                flex-direction: column;
            }
            .alert-banner {
                padding: 6px 12px;
                font-size: 11px;
                font-weight: bold;
                display: none;
                cursor: pointer;
                border-bottom: 1px solid transparent;
            }
            .alert-banner.error {
                background: #450a0a;
                color: #fca5a5;
                border-color: #7f1d1d;
            }
            .alert-banner.success {
                background: #052e16;
                color: #86efac;
                border-color: #14532d;
            }

            .tabs-header {
                background: #111827;
                display: flex;
                overflow-x: auto;
                scrollbar-width: none;
                border-bottom: 1px solid #1f2937;
            }
            .tabs-header::-webkit-scrollbar { display: none; }
            .tab-btn {
                background: transparent;
                border: none;
                color: #94a3b8;
                padding: 10px 14px;
                font-size: 11px;
                font-weight: 700;
                white-space: nowrap;
                cursor: pointer;
                border-bottom: 2px solid transparent;
            }
            .tab-btn.active {
                color: #38bdf8;
                border-bottom-color: #2563eb;
                background: #1e293b;
            }
            .tab-badge {
                background: #2563eb;
                color: #ffffff;
                font-size: 9px;
                padding: 1px 5px;
                border-radius: 8px;
                margin-left: 3px;
            }

            .tab-content {
                flex: 1;
                overflow-y: auto;
                padding: 10px;
                display: flex;
                flex-direction: column;
                gap: 10px;
            }

            .card {
                background: #111827;
                border: 1px solid #1f2937;
                border-radius: 8px;
                padding: 10px;
                display: flex;
                flex-direction: column;
                gap: 8px;
            }
            .card-title {
                font-size: 11px;
                font-weight: 700;
                color: #cbd5e1;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            .input-group {
                display: flex;
                flex-direction: column;
                gap: 3px;
            }
            .label {
                font-size: 10px;
                font-weight: 600;
                color: #94a3b8;
            }
            .input-box {
                width: 100%;
                height: 38px;
                background: #1f2937;
                border: 1px solid #374151;
                border-radius: 6px;
                padding: 0 10px;
                font-size: 13px;
                color: #ffffff;
                outline: none;
                font-family: monospace;
                font-weight: bold;
            }
            .input-box:focus {
                border-color: #2563eb;
            }
            .input-box:disabled {
                background: #1e293b;
                color: #64748b;
                border-color: #334155;
            }

            .btn-row {
                display: flex;
                gap: 6px;
                align-items: center;
            }
            .btn-primary {
                flex: 1;
                height: 38px;
                background: #2563eb;
                color: #ffffff;
                border: none;
                border-radius: 6px;
                font-size: 12px;
                font-weight: 700;
                cursor: pointer;
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 4px;
            }
            .btn-primary:active { background: #1d4ed8; }
            .btn-success {
                flex: 1;
                height: 38px;
                background: #16a34a;
                color: #ffffff;
                border: none;
                border-radius: 6px;
                font-size: 12px;
                font-weight: 700;
                cursor: pointer;
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 4px;
            }
            .btn-success:active { background: #15803d; }
            .btn-amber {
                flex: 1;
                height: 38px;
                background: #d97706;
                color: #ffffff;
                border: none;
                border-radius: 6px;
                font-size: 12px;
                font-weight: 700;
                cursor: pointer;
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 4px;
            }
            .btn-amber:active { background: #b45309; }
            .btn-secondary {
                background: #334155;
                color: #cbd5e1;
                border: 1px solid #475569;
                border-radius: 6px;
                padding: 4px 8px;
                font-size: 10px;
                font-weight: 600;
                cursor: pointer;
            }
            .btn-danger {
                background: #dc2626;
                color: #ffffff;
                border: none;
                border-radius: 6px;
                padding: 4px 8px;
                font-size: 10px;
                font-weight: bold;
                cursor: pointer;
            }
            .btn-primary:disabled, .btn-success:disabled, .btn-amber:disabled {
                opacity: 0.5;
                cursor: not-allowed;
            }

            select.input-box {
                cursor: pointer;
            }

            .range-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
                gap: 6px;
            }
            .range-chip {
                background: #1e293b;
                border: 1px solid #334155;
                border-radius: 6px;
                padding: 6px 4px;
                text-align: center;
                font-size: 11px;
                font-family: monospace;
                font-weight: bold;
                color: #ffffff;
                cursor: pointer;
            }
            .range-chip:active {
                background: #2563eb;
                border-color: #60a5fa;
            }

            .result-card {
                background: #064e3b;
                border: 1px solid #059669;
                border-radius: 8px;
                padding: 10px;
                display: flex;
                flex-direction: column;
                gap: 6px;
            }
            .result-row {
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-size: 11px;
                font-family: monospace;
                background: rgba(0, 0, 0, 0.25);
                padding: 4px 8px;
                border-radius: 4px;
                word-break: break-all;
            }
            .result-label {
                color: #a7f3d0;
                font-weight: bold;
                margin-right: 6px;
                white-space: nowrap;
            }
            .result-val {
                color: #ffffff;
                flex: 1;
            }
            .copy-icon-btn {
                background: #047857;
                color: #ffffff;
                border: none;
                border-radius: 4px;
                padding: 2px 6px;
                font-size: 10px;
                font-weight: bold;
                cursor: pointer;
                margin-left: 6px;
                white-space: nowrap;
            }

            .item-card {
                background: #111827;
                border: 1px solid #1f2937;
                border-radius: 8px;
                padding: 8px 10px;
                display: flex;
                flex-direction: column;
                gap: 6px;
            }
            .item-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-size: 11px;
                font-weight: bold;
                color: #cbd5e1;
            }
            .otp-badge {
                background: #065f46;
                border: 1px solid #10b981;
                color: #34d399;
                padding: 4px 8px;
                border-radius: 4px;
                font-size: 13px;
                font-weight: 800;
                font-family: monospace;
                letter-spacing: 1px;
                display: inline-block;
            }
            .otp-waiting {
                background: #334155;
                color: #94a3b8;
                padding: 3px 6px;
                border-radius: 4px;
                font-size: 10px;
                font-weight: bold;
            }
            .live-badge {
                font-size: 9px;
                font-weight: 800;
                padding: 2px 6px;
                border-radius: 4px;
            }
            .live-yes { background: #065f46; color: #4ade80; }
            .live-no { background: #7f1d1d; color: #f87171; }
            .live-un { background: #334155; color: #94a3b8; }
        </style>
    </head>
    <body>
        <div id="alertBanner" class="alert-banner" onclick="handleDismissMessage()"></div>

        <div class="tabs-header">
            <button id="tab0" class="tab-btn active" onclick="switchTab(0)">RANGE</button>
            <button id="tab1" class="tab-btn" onclick="switchTab(1)">NM LIMIT</button>
            <button id="tab2" class="tab-btn" onclick="switchTab(2)">NM OFFICAL</button>
            <button id="tab3" class="tab-btn" onclick="switchTab(3)">EML LIMIT</button>
            <button id="tab4" class="tab-btn" onclick="switchTab(4)">OTP <span id="otpBadgeCount" class="tab-badge" style="display: none;">0</span></button>
            <button id="tab5" class="tab-btn" onclick="switchTab(5)">SAV <span id="savBadgeCount" class="tab-badge" style="display: none;">0</span></button>
        </div>

        <div class="tab-content" id="tabContent"></div>

        <script>
            var currentTab = 0;
            var lastState = null;

            var countries = [
                "Bangladesh (+880)", "India (+91)", "Pakistan (+92)", "USA (+1)",
                "Indonesia (+62)", "Vietnam (+84)", "Philippines (+63)", "Nigeria (+234)", "Others"
            ];

            function switchTab(index) {
                currentTab = index;
                var buttons = document.querySelectorAll('.tab-btn');
                for (var i = 0; i < buttons.length; i++) {
                    if (i === index) buttons[i].classList.add('active');
                    else buttons[i].classList.remove('active');
                }
                if (window.AndroidBridge && window.AndroidBridge.switchTab) {
                    window.AndroidBridge.switchTab(index);
                }
                renderActiveTab();
            }

            function handleDismissMessage() {
                document.getElementById('alertBanner').style.display = 'none';
                if (window.AndroidBridge && window.AndroidBridge.dismissMessage) {
                    window.AndroidBridge.dismissMessage();
                }
            }

            window.renderDashboard = function(state) {
                lastState = state;
                if (state.selectedTab !== undefined && state.selectedTab !== currentTab) {
                    currentTab = state.selectedTab;
                    var buttons = document.querySelectorAll('.tab-btn');
                    for (var i = 0; i < buttons.length; i++) {
                        if (i === currentTab) buttons[i].classList.add('active');
                        else buttons[i].classList.remove('active');
                    }
                }

                var otpBadge = document.getElementById('otpBadgeCount');
                if (state.activeNumbers && state.activeNumbers.length > 0) {
                    otpBadge.innerText = state.activeNumbers.length;
                    otpBadge.style.display = 'inline-block';
                } else {
                    otpBadge.style.display = 'none';
                }

                var savBadge = document.getElementById('savBadgeCount');
                if (state.accountsHistory && state.accountsHistory.length > 0) {
                    savBadge.innerText = state.accountsHistory.length;
                    savBadge.style.display = 'inline-block';
                } else {
                    savBadge.style.display = 'none';
                }

                var alertBanner = document.getElementById('alertBanner');
                if (state.errorMessage && state.errorMessage.length > 0) {
                    alertBanner.className = 'alert-banner error';
                    alertBanner.innerText = '❌ ' + state.errorMessage;
                    alertBanner.style.display = 'block';
                } else if (state.successMessage && state.successMessage.length > 0) {
                    alertBanner.className = 'alert-banner success';
                    alertBanner.innerText = '✅ ' + state.successMessage;
                    alertBanner.style.display = 'block';
                } else {
                    alertBanner.style.display = 'none';
                }

                renderActiveTab();
            };

            function renderActiveTab() {
                var container = document.getElementById('tabContent');
                if (!lastState) return;

                if (currentTab === 0) renderRangeTab(container, lastState);
                else if (currentTab === 1) renderNmLimitTab(container, lastState);
                else if (currentTab === 2) renderNmOfficialTab(container, lastState);
                else if (currentTab === 3) renderEmailTab(container, lastState);
                else if (currentTab === 4) renderOtpTab(container, lastState);
                else if (currentTab === 5) renderSavTab(container, lastState);
            }

            function renderRangeTab(container, state) {
                var phoneVal = state.phoneInput || '';
                var html = '';
                html += '<div class="card">';
                html += '  <div class="card-title"><span>✍️ Custom Phone Range (নিজের রেঞ্জ)</span></div>';
                html += '  <div class="btn-row">';
                html += '    <input type="text" id="customRangeInput" class="input-box" placeholder="e.g. 2250689XXXX" value="' + phoneVal + '" />';
                html += '    <button class="btn-primary" style="flex: 0.5;" onclick="triggerFetchCustomRange()" ' + (state.isFetchingNumber ? 'disabled' : '') + '>';
                html += '      ' + (state.isFetchingNumber ? '...' : '⚡ GET');
                html += '    </button>';
                html += '  </div>';
                html += '</div>';

                if (state.phoneInput) {
                    html += '<div class="result-card">';
                    html += '  <div class="card-title" style="color: #a7f3d0;">';
                    html += '    <span>📱 Last Fetched Number:</span>';
                    html += '    <span style="font-family: monospace; font-size: 13px; font-weight: bold; color: #ffffff;">+' + state.phoneInput + '</span>';
                    html += '  </div>';
                    html += '  <div class="btn-row" style="margin-top: 4px;">';
                    html += '    <button class="btn-secondary" style="flex: 1;" onclick="triggerCopy(\'' + state.phoneInput + '\', \'PHONE NUMBER\')">📋 Copy Number</button>';
                    html += '    <button class="btn-primary" style="flex: 1; background: #059669;" onclick="switchTab(1)">➔ Go to NM LIMIT</button>';
                    html += '  </div>';
                    html += '</div>';
                }

                html += '<div class="card">';
                html += '  <div class="card-title">';
                html += '    <span>📋 Available Facebook Ranges</span>';
                html += '    <button class="btn-secondary" onclick="triggerRefreshRanges()">🔄 Refresh</button>';
                html += '  </div>';
                html += '  <div class="range-grid">';
                if (state.ranges && state.ranges.length > 0) {
                    for (var i = 0; i < state.ranges.length; i++) {
                        var r = state.ranges[i];
                        html += '    <div class="range-chip" onclick="triggerSelectRange(\'' + r + '\')">' + r + '</div>';
                    }
                } else {
                    html += '    <div style="font-size: 11px; color: #64748b; grid-column: 1/-1;">No ranges loaded. Click refresh.</div>';
                }
                html += '  </div>';
                html += '</div>';

                html += '<div class="card">';
                html += '  <div class="card-title"><span>🛡️ Device Activation</span></div>';
                html += '  <div class="result-row">';
                html += '    <span class="result-label">Device ID:</span>';
                html += '    <span class="result-val">' + (state.deviceId || 'Loading...') + '</span>';
                html += '    <button class="copy-icon-btn" onclick="triggerCopy(\'' + state.deviceId + '\', \'DEVICE ID\')">Copy</button>';
                html += '  </div>';
                html += '  <div class="btn-row" style="margin-top: 4px;">';
                html += '    <button class="btn-secondary" style="flex: 1;" onclick="triggerCheckActivation()">Check Status</button>';
                html += '  </div>';
                html += '</div>';

                container.innerHTML = html;
            }

            function renderNmLimitTab(container, state) {
                var html = '';
                html += '<div class="card">';
                html += '  <div class="card-title">';
                html += '    <span>⚡ NM LIMIT Creation</span>';
                html += '    <button class="btn-secondary" onclick="triggerOpenProxy()">🌐 Proxy</button>';
                html += '  </div>';

                html += '  <div class="input-group">';
                html += '    <label class="label">Phone Number (নাম্বার)</label>';
                html += '    <input type="text" id="nmPhone" class="input-box" placeholder="Phone number" value="' + (state.phoneInput || '') + '" oninput="triggerPhoneInput(this.value)" />';
                html += '  </div>';

                html += '  <div class="input-group">';
                html += '    <label class="label">Password (পাসওয়ার্ড)</label>';
                html += '    <input type="text" id="nmPass" class="input-box" placeholder="Password" value="' + (state.passwordInput || 'arafat@@##') + '" oninput="triggerPassInput(this.value)" />';
                html += '  </div>';

                html += '  <div class="input-group">';
                html += '    <label class="label">Country (দেশ)</label>';
                html += '    <select id="nmCountry" class="input-box" onchange="triggerCountrySelect(this.selectedIndex)">';
                for (var i = 0; i < countries.length; i++) {
                    html += '      <option value="' + i + '" ' + (i === state.selectedCountryIndex ? 'selected' : '') + '>' + countries[i] + '</option>';
                }
                html += '    </select>';
                html += '  </div>';

                html += '  <div class="btn-row" style="margin-top: 6px;">';
                html += '    <button class="btn-amber" onclick="triggerFindAccount()" ' + (state.isFindingAccount || state.isCreating ? 'disabled' : '') + '>';
                html += '      ' + (state.isFindingAccount ? 'Searching...' : '🔍 FIND ACCOUNT');
                html += '    </button>';
                html += '    <button class="btn-success" onclick="triggerCreateNmLimit()" ' + (state.isCreating || state.isFindingAccount ? 'disabled' : '') + '>';
                html += '      ' + (state.isCreating ? 'Creating...' : '⚡ CREATE');
                html += '    </button>';
                html += '  </div>';
                html += '</div>';

                html += renderLastAccountResult(state.lastAccount);
                container.innerHTML = html;
            }

            function renderNmOfficialTab(container, state) {
                var html = '';
                html += '<div class="card">';
                html += '  <div class="card-title">';
                html += '    <span>⚡ NM OFFICIAL Creation</span>';
                html += '    <button class="btn-secondary" onclick="triggerOpenProxy()">🌐 Proxy</button>';
                html += '  </div>';

                html += '  <div class="input-group">';
                html += '    <label class="label">Phone Number (নাম্বার)</label>';
                html += '    <input type="text" id="offPhone" class="input-box" placeholder="Phone number" value="' + (state.phoneInput || '') + '" oninput="triggerPhoneInput(this.value)" />';
                html += '  </div>';

                html += '  <div class="input-group">';
                html += '    <label class="label">Password (অটো লকড)</label>';
                html += '    <input type="text" class="input-box" value="arafat@@##" disabled />';
                html += '  </div>';

                html += '  <div class="input-group">';
                html += '    <label class="label">Country (দেশ)</label>';
                html += '    <select class="input-box" onchange="triggerCountrySelect(this.selectedIndex)">';
                for (var i = 0; i < countries.length; i++) {
                    html += '      <option value="' + i + '" ' + (i === state.selectedCountryIndex ? 'selected' : '') + '>' + countries[i] + '</option>';
                }
                html += '    </select>';
                html += '  </div>';

                html += '  <div class="btn-row" style="margin-top: 6px;">';
                html += '    <button class="btn-success" onclick="triggerCreateNmOfficial()" ' + (state.isCreating ? 'disabled' : '') + '>';
                html += '      ' + (state.isCreating ? 'Creating Official...' : '⚡ CREATE OFFICIAL');
                html += '    </button>';
                html += '  </div>';
                html += '</div>';

                html += renderLastAccountResult(state.lastAccount);
                container.innerHTML = html;
            }

            function renderEmailTab(container, state) {
                var html = '';
                html += '<div class="card">';
                html += '  <div class="card-title">';
                html += '    <span>⚡ EMAIL LIMIT Creation</span>';
                html += '    <button class="btn-secondary" onclick="triggerOpenProxy()">🌐 Proxy</button>';
                html += '  </div>';

                html += '  <div class="input-group">';
                html += '    <div class="card-title">';
                html += '      <label class="label">Email Address (ইমেইল)</label>';
                html += '      <span style="color: #38bdf8; font-size: 10px; cursor: pointer;" onclick="triggerRandomEmail()">🎲 Random Email</span>';
                html += '    </div>';
                html += '    <input type="email" id="emlInput" class="input-box" placeholder="Email address" value="' + (state.emailInput || '') + '" oninput="triggerEmailInput(this.value)" />';
                html += '  </div>';

                html += '  <div class="input-group">';
                html += '    <label class="label">Password (পাসওয়ার্ড)</label>';
                html += '    <input type="text" id="emlPass" class="input-box" placeholder="Password" value="' + (state.passwordInput || 'arafat@@##') + '" oninput="triggerPassInput(this.value)" />';
                html += '  </div>';

                html += '  <div class="input-group">';
                html += '    <label class="label">Country (দেশ)</label>';
                html += '    <select class="input-box" onchange="triggerCountrySelect(this.selectedIndex)">';
                for (var i = 0; i < countries.length; i++) {
                    html += '      <option value="' + i + '" ' + (i === state.selectedCountryIndex ? 'selected' : '') + '>' + countries[i] + '</option>';
                }
                html += '    </select>';
                html += '  </div>';

                html += '  <div class="btn-row" style="margin-top: 6px;">';
                html += '    <button class="btn-success" onclick="triggerCreateEmailAccount()" ' + (state.isCreating ? 'disabled' : '') + '>';
                html += '      ' + (state.isCreating ? 'Creating...' : '⚡ CREATE WITH EMAIL');
                html += '    </button>';
                html += '  </div>';
                html += '</div>';

                html += renderLastAccountResult(state.lastAccount);
                container.innerHTML = html;
            }

            function renderOtpTab(container, state) {
                var items = state.activeNumbers || [];
                var html = '';
                html += '<div class="card">';
                html += '  <div class="card-title">';
                html += '    <span>📥 Active Live Numbers (' + items.length + ')</span>';
                html += '    <div class="btn-row">';
                html += '      <button class="btn-secondary" onclick="triggerReloadInbox()">🔄 Reload</button>';
                html += '      <button class="btn-danger" onclick="triggerClearInbox()">🗑️ Clear</button>';
                html += '    </div>';
                html += '  </div>';
                html += '</div>';

                if (items.length === 0) {
                    html += '<div style="text-align: center; color: #64748b; font-size: 12px; margin-top: 20px;">';
                    html += '  No active numbers in inbox.<br/>Created numbers will appear here with live OTP.';
                    html += '</div>';
                } else {
                    for (var i = 0; i < items.length; i++) {
                        var it = items[i];
                        html += '<div class="item-card">';
                        html += '  <div class="item-header">';
                        html += '    <span style="color: #38bdf8; font-family: monospace;">+' + it.phone + '</span>';
                        html += '    <span style="font-size: 9px; color: #94a3b8;">' + it.timestamp + '</span>';
                        html += '  </div>';
                        html += '  <div style="display: flex; justify-content: space-between; align-items: center; margin: 4px 0;">';
                        if (it.otp) {
                            html += '    <div class="otp-badge">OTP: ' + it.otp + '</div>';
                        } else {
                            html += '    <div class="otp-waiting">⏳ Waiting for OTP...</div>';
                        }
                        html += '    <div class="btn-row">';
                        if (it.otp) {
                            html += '      <button class="copy-icon-btn" onclick="triggerCopy(\'' + it.otp + '\', \'OTP CODE\')">Copy OTP</button>';
                        }
                        html += '      <button class="copy-icon-btn" onclick="triggerCopy(\'' + it.phone + '\', \'PHONE\')">Copy Num</button>';
                        if (it.accountUid) {
                            html += '      <button class="copy-icon-btn" onclick="triggerCopy(\'' + it.accountUid + '\', \'UID\')">UID</button>';
                        }
                        html += '    </div>';
                        html += '  </div>';
                        html += '</div>';
                    }
                }
                container.innerHTML = html;
            }

            function renderSavTab(container, state) {
                var items = state.accountsHistory || [];
                var html = '';
                html += '<div class="card">';
                html += '  <div class="card-title">';
                html += '    <span>💾 Saved Created Accounts (' + items.length + ')</span>';
                html += '    <div class="btn-row">';
                html += '      <button class="btn-secondary" onclick="triggerCheckLiveAll()" ' + (state.isCheckingLive ? 'disabled' : '') + '>';
                html += '        ' + (state.isCheckingLive ? 'Checking...' : '🛡️ Check Live');
                html += '      </button>';
                html += '      <button class="btn-danger" onclick="triggerClearAllAccounts()">🗑️ Clear All</button>';
                html += '    </div>';
                html += '  </div>';
                html += '</div>';

                if (items.length === 0) {
                    html += '<div style="text-align: center; color: #64748b; font-size: 12px; margin-top: 20px;">';
                    html += '  No saved accounts yet.<br/>Successfully created accounts are stored here.';
                    html += '</div>';
                } else {
                    for (var i = 0; i < items.length; i++) {
                        var acc = items[i];
                        var badgeHtml = '<span class="live-badge live-un">UNKNOWN</span>';
                        if (acc.isLive === true) badgeHtml = '<span class="live-badge live-yes">LIVE</span>';
                        else if (acc.isLive === false) badgeHtml = '<span class="live-badge live-no">DIE</span>';

                        var safeCookies = (acc.cookies || '').replace(/'/g, "\\'");
                        html += '<div class="item-card">';
                        html += '  <div class="item-header">';
                        html += '    <div>';
                        html += '      <span style="font-family: monospace; color: #ffffff; font-weight: bold;">' + acc.phoneOrEmail + '</span> ';
                        html += '      ' + badgeHtml;
                        html += '    </div>';
                        html += '    <button class="btn-danger" style="padding: 1px 6px; font-size: 9px;" onclick="triggerDeleteAccount(\'' + acc.uid + '\')">✕</button>';
                        html += '  </div>';
                        html += '  <div class="result-row">';
                        html += '    <span class="result-label">UID:</span>';
                        html += '    <span class="result-val">' + acc.uid + '</span>';
                        html += '    <button class="copy-icon-btn" onclick="triggerCopy(\'' + acc.uid + '\', \'UID\')">Copy</button>';
                        html += '  </div>';
                        html += '  <div class="result-row">';
                        html += '    <span class="result-label">Pass:</span>';
                        html += '    <span class="result-val">' + acc.password + '</span>';
                        html += '    <button class="copy-icon-btn" onclick="triggerCopy(\'' + acc.password + '\', \'PASS\')">Copy</button>';
                        html += '  </div>';
                        html += '  <div class="result-row">';
                        html += '    <span class="result-label">Cookie:</span>';
                        html += '    <span class="result-val" style="font-size: 9px;">' + (acc.cookies || '').substring(0, 30) + '...</span>';
                        html += '    <button class="copy-icon-btn" onclick="triggerCopy(\'' + safeCookies + '\', \'COOKIES\')">Copy</button>';
                        html += '  </div>';
                        html += '</div>';
                    }
                }
                container.innerHTML = html;
            }

            function renderLastAccountResult(acc) {
                if (!acc) return '';
                var safeCookies = (acc.cookies || '').replace(/'/g, "\\'");
                var html = '';
                html += '<div class="result-card">';
                html += '  <div class="card-title" style="color: #a7f3d0;">';
                html += '    <span>🎉 Account Created Successfully!</span>';
                html += '    <button class="btn-secondary" onclick="triggerCheckLiveSingle(\'' + acc.uid + '\')">🛡️ Check Live</button>';
                html += '  </div>';
                html += '  <div class="result-row">';
                html += '    <span class="result-label">UID:</span>';
                html += '    <span class="result-val">' + acc.uid + '</span>';
                html += '    <button class="copy-icon-btn" onclick="triggerCopy(\'' + acc.uid + '\', \'UID\')">Copy</button>';
                html += '  </div>';
                html += '  <div class="result-row">';
                html += '    <span class="result-label">Phone/Email:</span>';
                html += '    <span class="result-val">' + acc.phoneOrEmail + '</span>';
                html += '    <button class="copy-icon-btn" onclick="triggerCopy(\'' + acc.phoneOrEmail + '\', \'PHONE/EMAIL\')">Copy</button>';
                html += '  </div>';
                html += '  <div class="result-row">';
                html += '    <span class="result-label">Password:</span>';
                html += '    <span class="result-val">' + acc.password + '</span>';
                html += '    <button class="copy-icon-btn" onclick="triggerCopy(\'' + acc.password + '\', \'PASSWORD\')">Copy</button>';
                html += '  </div>';
                html += '  <div class="result-row">';
                html += '    <span class="result-label">Cookies:</span>';
                html += '    <span class="result-val" style="font-size: 9px;">' + (acc.cookies || '').substring(0, 30) + '...</span>';
                html += '    <button class="copy-icon-btn" onclick="triggerCopy(\'' + safeCookies + '\', \'COOKIES\')">Copy</button>';
                html += '  </div>';
                html += '</div>';
                return html;
            }

            function triggerFetchCustomRange() {
                var input = document.getElementById('customRangeInput');
                var range = input ? input.value.trim() : '';
                if (window.AndroidBridge && window.AndroidBridge.fetchCustomRange) {
                    window.AndroidBridge.fetchCustomRange(range);
                }
            }

            function triggerSelectRange(r) {
                if (window.AndroidBridge && window.AndroidBridge.selectRange) {
                    window.AndroidBridge.selectRange(r);
                }
            }

            function triggerRefreshRanges() {
                if (window.AndroidBridge && window.AndroidBridge.refreshRanges) {
                    window.AndroidBridge.refreshRanges();
                }
            }

            function triggerCopy(text, label) {
                if (window.AndroidBridge && window.AndroidBridge.copyText) {
                    window.AndroidBridge.copyText(text, label);
                }
            }

            function triggerCheckActivation() {
                if (window.AndroidBridge && window.AndroidBridge.checkActivation) {
                    window.AndroidBridge.checkActivation();
                }
            }

            function triggerPhoneInput(v) {
                if (window.AndroidBridge && window.AndroidBridge.setPhoneInput) {
                    window.AndroidBridge.setPhoneInput(v);
                }
            }

            function triggerPassInput(v) {
                if (window.AndroidBridge && window.AndroidBridge.setPasswordInput) {
                    window.AndroidBridge.setPasswordInput(v);
                }
            }

            function triggerEmailInput(v) {
                if (window.AndroidBridge && window.AndroidBridge.setEmailInput) {
                    window.AndroidBridge.setEmailInput(v);
                }
            }

            function triggerCountrySelect(idx) {
                if (window.AndroidBridge && window.AndroidBridge.selectCountry) {
                    window.AndroidBridge.selectCountry(idx);
                }
            }

            function triggerCreateNmLimit() {
                var p = document.getElementById('nmPhone').value.trim();
                var pwd = document.getElementById('nmPass').value;
                var cIdx = document.getElementById('nmCountry').selectedIndex;
                if (window.AndroidBridge && window.AndroidBridge.createAccount) {
                    window.AndroidBridge.createAccount(p, pwd, cIdx);
                }
            }

            function triggerFindAccount() {
                var p = document.getElementById('nmPhone').value.trim();
                var cIdx = document.getElementById('nmCountry').selectedIndex;
                if (window.AndroidBridge && window.AndroidBridge.findAccount) {
                    window.AndroidBridge.findAccount(p, cIdx);
                }
            }

            function triggerCreateNmOfficial() {
                var p = document.getElementById('offPhone').value.trim();
                if (window.AndroidBridge && window.AndroidBridge.createOfficialAccount) {
                    window.AndroidBridge.createOfficialAccount(p, 0);
                }
            }

            function triggerCreateEmailAccount() {
                var eml = document.getElementById('emlInput').value.trim();
                var pwd = document.getElementById('emlPass').value;
                if (window.AndroidBridge && window.AndroidBridge.createEmailAccount) {
                    window.AndroidBridge.createEmailAccount(eml, pwd, 0);
                }
            }

            function triggerRandomEmail() {
                if (window.AndroidBridge && window.AndroidBridge.generateRandomEmail) {
                    window.AndroidBridge.generateRandomEmail();
                }
            }

            function triggerOpenProxy() {
                if (window.AndroidBridge && window.AndroidBridge.openProxySettings) {
                    window.AndroidBridge.openProxySettings();
                }
            }

            function triggerCheckLiveSingle(uid) {
                if (window.AndroidBridge && window.AndroidBridge.checkLiveSingle) {
                    window.AndroidBridge.checkLiveSingle(uid);
                }
            }

            function triggerCheckLiveAll() {
                if (window.AndroidBridge && window.AndroidBridge.checkLiveAll) {
                    window.AndroidBridge.checkLiveAll();
                }
            }

            function triggerDeleteAccount(uid) {
                if (window.AndroidBridge && window.AndroidBridge.deleteAccount) {
                    window.AndroidBridge.deleteAccount(uid);
                }
            }

            function triggerClearAllAccounts() {
                if (window.AndroidBridge && window.AndroidBridge.clearAllAccounts) {
                    window.AndroidBridge.clearAllAccounts();
                }
            }

            function triggerClearInbox() {
                if (window.AndroidBridge && window.AndroidBridge.clearInbox) {
                    window.AndroidBridge.clearInbox();
                }
            }

            function triggerReloadInbox() {
                if (window.AndroidBridge && window.AndroidBridge.reloadInbox) {
                    window.AndroidBridge.reloadInbox();
                }
            }
        </script>
    </body>
    </html>
    """.trimIndent()
}
