package com.example.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.network.AdminPanelService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdminPanelScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .systemBarsPadding()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewRef = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    setBackgroundColor(android.graphics.Color.parseColor("#020617"))

                    // Zero Lag Hardware Acceleration
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            loadAllAdminData(this@apply, scope)
                        }
                    }

                    addJavascriptInterface(AdminWebInterface(
                        context = ctx,
                        onClose = onClose,
                        scope = scope,
                        webView = this
                    ), "AndroidAdmin")

                    loadDataWithBaseURL("https://local.adminpanel/", getAdminPanelHtml(), "text/html", "UTF-8", null)
                }
            },
            update = {
                webViewRef = it
            }
        )
    }
}

private fun loadAllAdminData(webView: WebView, scope: CoroutineScope) {
    scope.launch {
        val config = AdminPanelService.fetchAppConfig()
        val users = AdminPanelService.fetchAllUsers()
        val withdrawals = AdminPanelService.fetchAllWithdrawals()

        val configJson = JSONObject().apply {
            put("isAppOn", config.isAppOn)
            put("notice", config.notice)
            put("isTerminalEnabled", config.isTerminalEnabled)
            put("terminalNotice", config.terminalNotice)
            put("isManualNumbersEnabled", config.isManualNumbersEnabled)
            put("otpPrice", config.otpPrice)
        }

        val usersArray = JSONArray().apply {
            users.forEach { u ->
                put(JSONObject().apply {
                    put("email", u.email)
                    put("firstName", u.firstName)
                    put("lastName", u.lastName)
                    put("telegramUsername", u.telegram)
                    put("balance", u.balance)
                    put("isBlocked", u.isBlocked)
                })
            }
        }

        val withdrawalsArray = JSONArray().apply {
            withdrawals.forEach { w ->
                put(JSONObject().apply {
                    put("id", w.id)
                    put("email", w.email)
                    put("name", w.name)
                    put("method", w.method)
                    put("value", w.value)
                    put("amount", w.amount)
                    put("timestamp", w.timestamp)
                    put("status", w.status)
                })
            }
        }

        val script = "if (window.populateAdminData) { window.populateAdminData($configJson, $usersArray, $withdrawalsArray); }"

        withContext(Dispatchers.Main) {
            webView.evaluateJavascript(script, null)
        }
    }
}

class AdminWebInterface(
    private val context: Context,
    private val onClose: () -> Unit,
    private val scope: CoroutineScope,
    private val webView: WebView
) {
    @JavascriptInterface
    fun closeAdmin() {
        Handler(Looper.getMainLooper()).post {
            onClose()
        }
    }

    @JavascriptInterface
    fun refreshData() {
        Handler(Looper.getMainLooper()).post {
            loadAllAdminData(webView, scope)
            Toast.makeText(context, "Data Refreshed 🔄", Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun saveAppConfig(
        isAppOn: Boolean,
        notice: String,
        isTerminalEnabled: Boolean,
        terminalNotice: String,
        isManualNumbersEnabled: Boolean,
        otpPrice: Double
    ) {
        scope.launch {
            val success = AdminPanelService.saveAppConfig(
                isAppOn = isAppOn,
                notice = notice,
                isTerminalEnabled = isTerminalEnabled,
                terminalNotice = terminalNotice,
                isManualNumbersEnabled = isManualNumbersEnabled,
                otpPrice = otpPrice
            )
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "App Settings Saved Successfully! ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to save settings ❌", Toast.LENGTH_SHORT).show()
                }
                loadAllAdminData(webView, scope)
            }
        }
    }

    @JavascriptInterface
    fun updateUserBalance(email: String, newBalance: Double) {
        scope.launch {
            val success = AdminPanelService.updateUserBalance(email, newBalance)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "User balance updated! 💰", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to update balance ❌", Toast.LENGTH_SHORT).show()
                }
                loadAllAdminData(webView, scope)
            }
        }
    }

    @JavascriptInterface
    fun toggleUserBlock(email: String, block: Boolean) {
        scope.launch {
            val success = AdminPanelService.toggleUserBlock(email, block)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, if (block) "User Blocked! 🚫" else "User Unblocked! 🟢", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to change user status ❌", Toast.LENGTH_SHORT).show()
                }
                loadAllAdminData(webView, scope)
            }
        }
    }

    @JavascriptInterface
    fun updateWithdrawalStatus(id: String, status: String) {
        scope.launch {
            val success = AdminPanelService.updateWithdrawalStatus(id, status)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Withdrawal marked as $status! ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to update withdrawal ❌", Toast.LENGTH_SHORT).show()
                }
                loadAllAdminData(webView, scope)
            }
        }
    }

    @JavascriptInterface
    fun copyText(text: String, label: String) {
        Handler(Looper.getMainLooper()).post {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText(label, text))
            Toast.makeText(context, "$label Copied!", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun getAdminPanelHtml(): String {
    return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>ADMIN PNAL - ALL IN ONE CONTROL</title>
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            -webkit-tap-highlight-color: transparent;
            user-select: none;
        }
        body {
            background: #020617;
            color: #f8fafc;
            font-size: 12px;
            padding-bottom: 30px;
            overflow-x: hidden;
        }

        /* Top Header */
        .admin-header {
            position: sticky;
            top: 0;
            z-index: 50;
            background: #0f172a;
            border-bottom: 1px solid #1e293b;
            padding: 10px 14px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            box-shadow: 0 4px 12px rgba(0,0,0,0.5);
        }
        .brand-title {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            font-weight: 800;
            color: #38bdf8;
        }
        .brand-badge {
            background: #ef4444;
            color: #ffffff;
            font-size: 10px;
            font-weight: 800;
            padding: 2px 6px;
            border-radius: 4px;
            text-transform: uppercase;
        }
        .header-actions {
            display: flex;
            align-items: center;
            gap: 6px;
        }
        .hdr-btn {
            background: #1e293b;
            color: #94a3b8;
            border: 1px solid #334155;
            padding: 6px 10px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 700;
            cursor: pointer;
        }
        .close-btn {
            background: #7f1d1d;
            color: #fca5a5;
            border-color: #991b1b;
        }

        .container {
            padding: 12px;
        }

        /* Cards Grid */
        .cards-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 12px;
            margin-bottom: 16px;
        }

        .glass-card {
            background: #0f172a;
            border: 1px solid #1e293b;
            border-radius: 12px;
            padding: 14px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
        }

        .card-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 10px;
            border-bottom: 1px solid #1e293b;
            padding-bottom: 6px;
        }
        .card-title {
            font-size: 12px;
            font-weight: 800;
            color: #38bdf8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .row-between {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: #1e293b;
            padding: 8px 10px;
            border-radius: 8px;
            margin-bottom: 8px;
        }

        /* Toggle Button */
        .toggle-btn {
            padding: 5px 12px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 800;
            border: none;
            cursor: pointer;
            color: white;
        }
        .toggle-btn.on {
            background: #059669;
        }
        .toggle-btn.off {
            background: #dc2626;
        }

        /* Inputs & Buttons */
        .form-control {
            width: 100%;
            background: #1e293b;
            border: 1px solid #334155;
            color: #ffffff;
            padding: 8px 10px;
            border-radius: 8px;
            font-size: 12px;
            outline: none;
            margin-bottom: 8px;
        }
        .btn-action {
            width: 100%;
            background: #0284c7;
            color: white;
            padding: 9px;
            border-radius: 8px;
            font-size: 12px;
            font-weight: 800;
            border: none;
            cursor: pointer;
        }
        .btn-action:active {
            transform: scale(0.98);
        }

        /* Stat Box */
        .stat-box {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: #0f172a;
            border: 1px solid #1e293b;
            padding: 12px;
            border-radius: 12px;
        }
        .stat-val {
            font-size: 20px;
            font-weight: 800;
            color: #ffffff;
        }

        /* Data Tables / Lists */
        .section-title {
            font-size: 14px;
            font-weight: 800;
            color: #f1f5f9;
            margin: 16px 0 8px 0;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .data-list {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }
        .item-card {
            background: #0f172a;
            border: 1px solid #1e293b;
            border-radius: 10px;
            padding: 10px;
        }
        .item-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 4px;
        }
        .item-title {
            font-size: 12px;
            font-weight: 700;
            color: #f8fafc;
        }
        .item-sub {
            font-size: 10px;
            color: #94a3b8;
        }
        .badge {
            font-size: 10px;
            font-weight: 800;
            padding: 2px 6px;
            border-radius: 4px;
        }
        .badge-green { background: #065f46; color: #6ee7b7; }
        .badge-yellow { background: #78350f; color: #fde68a; }
        .badge-red { background: #7f1d1d; color: #fca5a5; }

        .btn-sm {
            padding: 4px 8px;
            font-size: 10px;
            font-weight: 700;
            border-radius: 6px;
            border: none;
            cursor: pointer;
            color: white;
        }
        .btn-green { background: #059669; }
        .btn-red { background: #dc2626; }
        .btn-blue { background: #0284c7; }
    </style>
</head>
<body>

    <!-- Header -->
    <div class="admin-header">
        <div class="brand-title">
            <span>⚙️ ADMIN PNAL</span>
            <span class="brand-badge">CONTROL</span>
        </div>
        <div class="header-actions">
            <button class="hdr-btn" onclick="AndroidAdmin.refreshData()">🔄 Refresh</button>
            <button class="hdr-btn close-btn" onclick="AndroidAdmin.closeAdmin()">✖ Close</button>
        </div>
    </div>

    <div class="container">
        
        <!-- Controls Grid -->
        <div class="cards-grid">
            
            <!-- 1. App Master Power -->
            <div class="glass-card">
                <div class="card-header">
                    <span class="card-title">📱 App Master Switch</span>
                </div>
                <div class="row-between">
                    <span style="font-weight: 700; color: #cbd5e1;">App Status</span>
                    <button id="btnAppToggle" class="toggle-btn on" onclick="toggleAppStatus()">ON</button>
                </div>
                <input type="text" id="appNoticeInput" class="form-control" placeholder="Maintenance notice...">
                <button class="btn-action" onclick="saveAppConfig()">Save App Config</button>
            </div>

            <!-- 2. Terminal Controls -->
            <div class="glass-card">
                <div class="card-header">
                    <span class="card-title">💻 Terminal System</span>
                </div>
                <div class="row-between">
                    <span style="font-weight: 700; color: #cbd5e1;">Terminal Auto OTP</span>
                    <button id="btnTerminalToggle" class="toggle-btn on" onclick="toggleTerminalStatus()">ON</button>
                </div>
                <div class="row-between">
                    <span style="font-weight: 700; color: #cbd5e1;">Manual Numbers</span>
                    <button id="btnManualToggle" class="toggle-btn on" onclick="toggleManualStatus()">ON</button>
                </div>
                <input type="text" id="terminalNoticeInput" class="form-control" placeholder="Terminal notice...">
                <button class="btn-action" style="background: #d97706;" onclick="saveTerminalConfig()">Save Terminal Config</button>
            </div>

            <!-- 3. OTP Price & Stats -->
            <div class="glass-card">
                <div class="card-header">
                    <span class="card-title">💰 Set OTP Price (৳)</span>
                </div>
                <input type="number" id="otpPriceInput" step="0.01" class="form-control" placeholder="e.g. 0.50">
                <button class="btn-action" style="background: #4f46e5; margin-bottom: 12px;" onclick="saveOtpPrice()">Save OTP Price</button>

                <div style="display: flex; gap: 8px;">
                    <div class="stat-box" style="flex: 1;">
                        <div>
                            <div style="font-size: 10px; color: #94a3b8; font-weight: 700;">USERS</div>
                            <div id="statTotalUsers" class="stat-val">0</div>
                        </div>
                    </div>
                    <div class="stat-box" style="flex: 1;">
                        <div>
                            <div style="font-size: 10px; color: #94a3b8; font-weight: 700;">PENDING</div>
                            <div id="statPendingWithdrawals" class="stat-val" style="color: #fbbf24;">0</div>
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <!-- Withdrawals List -->
        <div class="section-title">
            <span>💳 Pending Withdrawals</span>
            <button class="btn-sm btn-blue" onclick="AndroidAdmin.refreshData()">Refresh</button>
        </div>
        <div id="withdrawalsList" class="data-list">
            <div style="text-align: center; color: #64748b; padding: 16px;">Loading withdrawals...</div>
        </div>

        <!-- Users Directory -->
        <div class="section-title">
            <span>👥 Registered Users</span>
            <input type="text" id="userSearchInput" class="form-control" style="width: 140px; margin: 0; padding: 4px 8px;" placeholder="Search..." oninput="filterUsers()">
        </div>
        <div id="usersList" class="data-list">
            <div style="text-align: center; color: #64748b; padding: 16px;">Loading users...</div>
        </div>

    </div>

    <script>
        var isAppOn = true;
        var isTerminalOn = true;
        var isManualOn = true;
        var globalUsers = [];
        var globalWithdrawals = [];

        function updateToggleUi(btnId, isOn) {
            var btn = document.getElementById(btnId);
            if (!btn) return;
            btn.innerText = isOn ? 'ON' : 'OFF';
            btn.className = 'toggle-btn ' + (isOn ? 'on' : 'off');
        }

        function toggleAppStatus() {
            isAppOn = !isAppOn;
            updateToggleUi('btnAppToggle', isAppOn);
        }

        function toggleTerminalStatus() {
            isTerminalOn = !isTerminalOn;
            updateToggleUi('btnTerminalToggle', isTerminalOn);
        }

        function toggleManualStatus() {
            isManualOn = !isManualOn;
            updateToggleUi('btnManualToggle', isManualOn);
        }

        // Native callback from Kotlin
        window.populateAdminData = function(config, users, withdrawals) {
            globalUsers = users || [];
            globalWithdrawals = withdrawals || [];

            if (config) {
                isAppOn = config.isAppOn !== false;
                isTerminalOn = config.isTerminalEnabled !== false;
                isManualOn = config.isManualNumbersEnabled !== false;

                updateToggleUi('btnAppToggle', isAppOn);
                updateToggleUi('btnTerminalToggle', isTerminalOn);
                updateToggleUi('btnManualToggle', isManualOn);

                document.getElementById('appNoticeInput').value = config.notice || '';
                document.getElementById('terminalNoticeInput').value = config.terminalNotice || '';
                document.getElementById('otpPriceInput').value = config.otpPrice || 0.5;
            }

            document.getElementById('statTotalUsers').innerText = globalUsers.length;
            var pendingCount = 0;
            for (var i = 0; i < globalWithdrawals.length; i++) {
                if (globalWithdrawals[i].status === 'pending') pendingCount++;
            }
            document.getElementById('statPendingWithdrawals').innerText = pendingCount;

            renderWithdrawals(globalWithdrawals);
            renderUsers(globalUsers);
        };

        function saveAppConfig() {
            var notice = document.getElementById('appNoticeInput').value.trim();
            var termNotice = document.getElementById('terminalNoticeInput').value.trim();
            var price = parseFloat(document.getElementById('otpPriceInput').value) || 0.5;

            AndroidAdmin.saveAppConfig(isAppOn, notice, isTerminalOn, termNotice, isManualOn, price);
        }

        function saveTerminalConfig() {
            saveAppConfig();
        }

        function saveOtpPrice() {
            saveAppConfig();
        }

        function renderWithdrawals(list) {
            var container = document.getElementById('withdrawalsList');
            if (!list || list.length === 0) {
                container.innerHTML = '<div style="text-align: center; color: #64748b; padding: 12px;">No withdrawal requests found</div>';
                return;
            }

            var html = '';
            for (var i = 0; i < list.length; i++) {
                var w = list[i];
                var badgeClass = 'badge-yellow';
                if (w.status === 'completed' || w.status === 'approved') badgeClass = 'badge-green';
                if (w.status === 'rejected') badgeClass = 'badge-red';

                html += '<div class="item-card">' +
                    '<div class="item-row">' +
                        '<div>' +
                            '<span class="item-title">' + escapeHtml(w.name || 'User') + ' (' + escapeHtml(w.method) + ')</span>' +
                            '<div class="item-sub">' + escapeHtml(w.email) + '</div>' +
                        '</div>' +
                        '<span class="badge ' + badgeClass + '">' + escapeHtml(w.status.toUpperCase()) + '</span>' +
                    '</div>' +
                    '<div class="item-row" style="margin-top: 4px;">' +
                        '<span style="font-weight: 800; color: #34d399;">৳ ' + parseFloat(w.amount || 0).toFixed(2) + '</span>' +
                        '<span style="color: #cbd5e1; cursor: pointer;" onclick="AndroidAdmin.copyText(\'' + escapeHtml(w.value) + '\', \'Account Details\')">📋 ' + escapeHtml(w.value) + '</span>' +
                    '</div>' +
                    '<div style="display: flex; gap: 6px; margin-top: 6px;">' +
                        '<button class="btn-sm btn-green" onclick="AndroidAdmin.updateWithdrawalStatus(\'' + escapeHtml(w.id) + '\', \'completed\')">✅ Approve</button>' +
                        '<button class="btn-sm btn-red" onclick="AndroidAdmin.updateWithdrawalStatus(\'' + escapeHtml(w.id) + '\', \'rejected\')">❌ Reject</button>' +
                    '</div>' +
                '</div>';
            }
            container.innerHTML = html;
        }

        function renderUsers(list) {
            var container = document.getElementById('usersList');
            if (!list || list.length === 0) {
                container.innerHTML = '<div style="text-align: center; color: #64748b; padding: 12px;">No users found</div>';
                return;
            }

            var html = '';
            for (var i = 0; i < list.length; i++) {
                var u = list[i];
                var name = ((u.firstName || '') + ' ' + (u.lastName || '')).trim() || 'No Name';
                var tg = u.telegramUsername ? ('@' + u.telegramUsername) : 'N/A';
                var isBlocked = u.isBlocked === true;

                html += '<div class="item-card">' +
                    '<div class="item-row">' +
                        '<div>' +
                            '<span class="item-title">' + escapeHtml(name) + '</span>' +
                            '<div class="item-sub">' + escapeHtml(u.email) + ' • TG: ' + escapeHtml(tg) + '</div>' +
                        '</div>' +
                        '<span class="badge ' + (isBlocked ? 'badge-red' : 'badge-green') + '">' + (isBlocked ? 'BLOCKED' : 'ACTIVE') + '</span>' +
                    '</div>' +
                    '<div class="item-row" style="margin-top: 4px;">' +
                        '<span style="font-weight: 800; color: #38bdf8;">Balance: ৳ ' + parseFloat(u.balance || 0).toFixed(2) + '</span>' +
                    '</div>' +
                    '<div style="display: flex; gap: 6px; margin-top: 6px;">' +
                        '<button class="btn-sm btn-blue" onclick="editBalance(\'' + escapeHtml(u.email) + '\', ' + (u.balance || 0) + ')">💰 Edit Balance</button>' +
                        '<button class="btn-sm ' + (isBlocked ? 'btn-green' : 'btn-red') + '" onclick="AndroidAdmin.toggleUserBlock(\'' + escapeHtml(u.email) + '\', ' + (!isBlocked) + ')">' + (isBlocked ? '🟢 Unblock' : '🚫 Block') + '</button>' +
                    '</div>' +
                '</div>';
            }
            container.innerHTML = html;
        }

        function filterUsers() {
            var q = document.getElementById('userSearchInput').value.toLowerCase();
            var filtered = [];
            for (var i = 0; i < globalUsers.length; i++) {
                var u = globalUsers[i];
                var name = ((u.firstName || '') + ' ' + (u.lastName || '')).toLowerCase();
                var email = (u.email || '').toLowerCase();
                var tg = (u.telegramUsername || '').toLowerCase();
                if (name.indexOf(q) !== -1 || email.indexOf(q) !== -1 || tg.indexOf(q) !== -1) {
                    filtered.push(u);
                }
            }
            renderUsers(filtered);
        }

        function editBalance(email, curr) {
            var newVal = prompt('Enter new balance for ' + email + ':', curr);
            if (newVal !== null && !isNaN(newVal)) {
                AndroidAdmin.updateUserBalance(email, parseFloat(newVal));
            }
        }

        function escapeHtml(str) {
            if (!str) return '';
            return String(str)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }
    </script>
</body>
</html>
    """.trimIndent()
}
