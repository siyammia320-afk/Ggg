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
            .background(Color(0xFF0F172A))
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
                    setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))

                    // Zero Lag Hardware Acceleration
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Initial data load into Admin HTML
                            loadAllAdminData(this@apply, scope)
                        }
                    }

                    addJavascriptInterface(AdminWebInterface(
                        context = ctx,
                        onClose = onClose,
                        scope = scope,
                        webView = this
                    ), "AndroidAdmin")

                    loadDataWithBaseURL(null, getAdminPanelHtml(), "text/html", "UTF-8", null)
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
        val keys = AdminPanelService.fetchMasterKeys()

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
                    put("name", "${u.firstName} ${u.lastName}".trim())
                    put("telegram", u.telegram)
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

        val keysArray = JSONArray().apply {
            keys.forEach { k ->
                put(JSONObject().apply {
                    put("key", k.key)
                    put("status", k.status)
                    put("usedByApiKey", k.usedByApiKey)
                    put("usedAt", k.usedAt)
                })
            }
        }

        val script = "if (window.populateAdminData) { window.populateAdminData($configJson, $usersArray, $withdrawalsArray, $keysArray); }"

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
                    Toast.makeText(context, "Withdrawal marked as " + status + "! ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to update withdrawal ❌", Toast.LENGTH_SHORT).show()
                }
                loadAllAdminData(webView, scope)
            }
        }
    }

    @JavascriptInterface
    fun addMasterKey(key: String) {
        scope.launch {
            val success = AdminPanelService.addMasterKey(key)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Master Key Added! 🔑", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to add key ❌", Toast.LENGTH_SHORT).show()
                }
                loadAllAdminData(webView, scope)
            }
        }
    }

    @JavascriptInterface
    fun deleteMasterKey(key: String) {
        scope.launch {
            val success = AdminPanelService.deleteMasterKey(key)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Master Key Deleted! 🗑️", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to delete key ❌", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, label + " Copied!", Toast.LENGTH_SHORT).show()
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
  <title>FB TOOL Admin Control Panel</title>
  <style>
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
      -webkit-tap-highlight-color: transparent;
      user-select: none;
    }
    body {
      background-color: #0b0f19;
      color: #f1f5f9;
      font-size: 13px;
      padding-bottom: 30px;
      overflow-x: hidden;
    }
    
    /* Top Header Bar */
    .admin-header {
      position: sticky;
      top: 0;
      z-index: 100;
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
      font-size: 15px;
      font-weight: 800;
      letter-spacing: 0.5px;
      color: #38bdf8;
    }
    .brand-badge {
      background: #dc2626;
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
      gap: 8px;
    }
    .hdr-btn {
      background: #1e293b;
      color: #94a3b8;
      border: 1px solid #334155;
      padding: 6px 10px;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 700;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 4px;
    }
    .hdr-btn:active {
      transform: scale(0.96);
      background: #334155;
    }
    .close-btn {
      background: #7f1d1d;
      color: #fca5a5;
      border-color: #991b1b;
    }

    /* Tabs Bar */
    .nav-tabs {
      display: flex;
      background: #111827;
      border-bottom: 1px solid #1f2937;
      overflow-x: auto;
      white-space: nowrap;
      scrollbar-width: none;
    }
    .nav-tabs::-webkit-scrollbar {
      display: none;
    }
    .tab-btn {
      flex: 1;
      min-width: 90px;
      text-align: center;
      padding: 10px 8px;
      font-size: 11px;
      font-weight: 700;
      color: #94a3b8;
      background: transparent;
      border: none;
      border-bottom: 2px solid transparent;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
    }
    .tab-btn.active {
      color: #38bdf8;
      border-bottom: 2px solid #38bdf8;
      background: rgba(56, 189, 248, 0.05);
    }
    .tab-badge {
      background: #374151;
      color: #f3f4f6;
      font-size: 9px;
      padding: 1px 5px;
      border-radius: 10px;
    }

    /* Tab Panes */
    .content-container {
      padding: 12px;
    }
    .tab-pane {
      display: none;
    }
    .tab-pane.active {
      display: block;
      animation: fadeIn 0.15s ease-out;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(3px); }
      to { opacity: 1; transform: translateY(0); }
    }

    /* Card Box */
    .card {
      background: #111827;
      border: 1px solid #1f2937;
      border-radius: 12px;
      padding: 14px;
      margin-bottom: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.3);
    }
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
      border-bottom: 1px solid #1f2937;
      padding-bottom: 8px;
    }
    .card-title {
      font-size: 13px;
      font-weight: 800;
      color: #e2e8f0;
      display: flex;
      align-items: center;
      gap: 6px;
    }

    /* Form Inputs */
    .form-group {
      margin-bottom: 12px;
    }
    .form-label {
      display: block;
      font-size: 11px;
      font-weight: 700;
      color: #94a3b8;
      margin-bottom: 5px;
    }
    .form-control {
      width: 100%;
      background: #1e293b;
      border: 1px solid #334155;
      color: #ffffff;
      padding: 9px 12px;
      border-radius: 8px;
      font-size: 12px;
      outline: none;
    }
    .form-control:focus {
      border-color: #38bdf8;
      box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.2);
    }
    
    /* Toggle Switch */
    .toggle-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #1f2937;
    }
    .toggle-row:last-child {
      border-bottom: none;
    }
    .toggle-label-group {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }
    .toggle-title {
      font-size: 12px;
      font-weight: 700;
      color: #e2e8f0;
    }
    .toggle-desc {
      font-size: 10px;
      color: #64748b;
    }
    
    .switch {
      position: relative;
      display: inline-block;
      width: 44px;
      height: 24px;
    }
    .switch input {
      opacity: 0;
      width: 0;
      height: 0;
    }
    .slider {
      position: absolute;
      cursor: pointer;
      top: 0; left: 0; right: 0; bottom: 0;
      background-color: #334155;
      transition: .2s;
      border-radius: 24px;
    }
    .slider:before {
      position: absolute;
      content: "";
      height: 18px;
      width: 18px;
      left: 3px;
      bottom: 3px;
      background-color: white;
      transition: .2s;
      border-radius: 50%;
    }
    input:checked + .slider {
      background-color: #10b981;
    }
    input:checked + .slider:before {
      transform: translateX(20px);
    }

    /* Buttons */
    .btn {
      width: 100%;
      padding: 10px;
      border-radius: 8px;
      font-size: 12px;
      font-weight: 800;
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      transition: transform 0.1s;
    }
    .btn:active {
      transform: scale(0.98);
    }
    .btn-primary {
      background: #2563eb;
      color: white;
    }
    .btn-success {
      background: #059669;
      color: white;
    }
    .btn-danger {
      background: #dc2626;
      color: white;
    }
    .btn-sm {
      padding: 5px 10px;
      font-size: 11px;
      width: auto;
      border-radius: 6px;
    }

    /* Table / Lists */
    .data-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .item-card {
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 8px;
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
    .item-subtitle {
      font-size: 10px;
      color: #94a3b8;
    }
    .badge {
      font-size: 10px;
      font-weight: 700;
      padding: 2px 6px;
      border-radius: 4px;
    }
    .badge-green { background: #065f46; color: #6ee7b7; }
    .badge-red { background: #7f1d1d; color: #fca5a5; }
    .badge-yellow { background: #78350f; color: #fde68a; }
    .badge-blue { background: #1e3a8a; color: #93c5fd; }

    .action-row {
      display: flex;
      gap: 6px;
      margin-top: 8px;
      padding-top: 6px;
      border-top: 1px dashed #334155;
    }
  </style>
</head>
<body>

  <!-- Top Navigation Header -->
  <div class="admin-header">
    <div class="brand-title">
      <span>👑 FB TOOL</span>
      <span class="brand-badge">ADMIN</span>
    </div>
    <div class="header-actions">
      <button class="hdr-btn" onclick="AndroidAdmin.refreshData()">🔄 Refresh</button>
      <button class="hdr-btn close-btn" onclick="AndroidAdmin.closeAdmin()">✖ Close</button>
    </div>
  </div>

  <!-- Tabs Navigation Bar -->
  <div class="nav-tabs">
    <button class="tab-btn active" onclick="switchTab(0, this)">
      ⚙️ Controls
    </button>
    <button class="tab-btn" onclick="switchTab(1, this)">
      👥 Users <span id="usersCount" class="tab-badge">0</span>
    </button>
    <button class="tab-btn" onclick="switchTab(2, this)">
      💳 Withdrawals <span id="withdCount" class="tab-badge">0</span>
    </button>
    <button class="tab-btn" onclick="switchTab(3, this)">
      🔑 Master Keys <span id="keysCount" class="tab-badge">0</span>
    </button>
  </div>

  <div class="content-container">

    <!-- TAB 0: APP CONTROLS -->
    <div id="tab0" class="tab-pane active">
      <div class="card">
        <div class="card-header">
          <span class="card-title">⚡ App & System Control</span>
        </div>

        <div class="toggle-row">
          <div class="toggle-label-group">
            <span class="toggle-title">App Master Switch (ON/OFF)</span>
            <span class="toggle-desc">Turn off to put entire application into maintenance</span>
          </div>
          <label class="switch">
            <input type="checkbox" id="cfgAppOn">
            <span class="slider"></span>
          </label>
        </div>

        <div class="form-group" style="margin-top: 10px;">
          <label class="form-label">App Off Maintenance Notice</label>
          <input type="text" id="cfgAppNotice" class="form-control" placeholder="অ্যাপ বর্তমানে বন্ধ রয়েছে...">
        </div>

        <div class="toggle-row">
          <div class="toggle-label-group">
            <span class="toggle-title">Terminal Auto-OTP Feature</span>
            <span class="toggle-desc">Enable or disable floating terminal auto OTP</span>
          </div>
          <label class="switch">
            <input type="checkbox" id="cfgTerminalOn">
            <span class="slider"></span>
          </label>
        </div>

        <div class="form-group" style="margin-top: 10px;">
          <label class="form-label">Terminal Disabled Notice</label>
          <input type="text" id="cfgTerminalNotice" class="form-control" placeholder="টার্মিনাল বর্তমানে বন্ধ আছে...">
        </div>

        <div class="toggle-row">
          <div class="toggle-label-group">
            <span class="toggle-title">Manual Numbers Feature</span>
            <span class="toggle-desc">Enable or disable manual range/number input</span>
          </div>
          <label class="switch">
            <input type="checkbox" id="cfgManualOn">
            <span class="slider"></span>
          </label>
        </div>

        <div class="form-group" style="margin-top: 10px;">
          <label class="form-label">Per OTP Commission Price (৳ / $)</label>
          <input type="number" step="0.01" id="cfgOtpPrice" class="form-control" placeholder="0.50">
        </div>

        <button class="btn btn-primary" onclick="saveControls()">💾 SAVE SETTINGS TO FIREBASE</button>
      </div>
    </div>

    <!-- TAB 1: USERS MANAGEMENT -->
    <div id="tab1" class="tab-pane">
      <div class="card">
        <div class="card-header">
          <span class="card-title">👥 Registered Users</span>
          <input type="text" id="userSearchInput" class="form-control" style="width: 140px; padding: 4px 8px; font-size: 11px;" placeholder="Search user..." oninput="filterUsers()">
        </div>
        <div id="usersList" class="data-list">
          <div style="text-align: center; color: #64748b; padding: 20px;">Loading users...</div>
        </div>
      </div>
    </div>

    <!-- TAB 2: WITHDRAWALS -->
    <div id="tab2" class="tab-pane">
      <div class="card">
        <div class="card-header">
          <span class="card-title">💳 Withdrawal Requests</span>
        </div>
        <div id="withdrawalsList" class="data-list">
          <div style="text-align: center; color: #64748b; padding: 20px;">Loading withdrawals...</div>
        </div>
      </div>
    </div>

    <!-- TAB 3: MASTER KEYS -->
    <div id="tab3" class="tab-pane">
      <div class="card">
        <div class="card-header">
          <span class="card-title">🔑 Generate / Add Master Key</span>
        </div>
        <div style="display: flex; gap: 8px; margin-bottom: 10px;">
          <input type="text" id="newKeyInput" class="form-control" placeholder="e.g. VIP-FB-9988">
          <button class="btn btn-success btn-sm" style="white-space: nowrap;" onclick="submitNewKey()">+ Add Key</button>
        </div>
        <div id="keysList" class="data-list">
          <div style="text-align: center; color: #64748b; padding: 20px;">Loading keys...</div>
        </div>
      </div>
    </div>

  </div>

  <script>
    var globalUsers = [];
    var globalWithdrawals = [];
    var globalKeys = [];

    function switchTab(index, btn) {
      var panes = document.querySelectorAll('.tab-pane');
      for (var i = 0; i < panes.length; i++) {
        panes[i].classList.toggle('active', i === index);
      }
      var tabs = document.querySelectorAll('.tab-btn');
      for (var j = 0; j < tabs.length; j++) {
        tabs[j].classList.remove('active');
      }
      btn.classList.add('active');
    }

    // Called from Kotlin via evaluateJavascript
    window.populateAdminData = function(config, users, withdrawals, keys) {
      globalUsers = users || [];
      globalWithdrawals = withdrawals || [];
      globalKeys = keys || [];

      // Update counters
      document.getElementById('usersCount').innerText = globalUsers.length;
      document.getElementById('withdCount').innerText = globalWithdrawals.length;
      document.getElementById('keysCount').innerText = globalKeys.length;

      // Populate Controls Tab
      if (config) {
        document.getElementById('cfgAppOn').checked = config.isAppOn;
        document.getElementById('cfgAppNotice').value = config.notice || '';
        document.getElementById('cfgTerminalOn').checked = config.isTerminalEnabled;
        document.getElementById('cfgTerminalNotice').value = config.terminalNotice || '';
        document.getElementById('cfgManualOn').checked = config.isManualNumbersEnabled;
        document.getElementById('cfgOtpPrice').value = config.otpPrice || 0.5;
      }

      renderUsers(globalUsers);
      renderWithdrawals(globalWithdrawals);
      renderKeys(globalKeys);
    };

    function saveControls() {
      var isAppOn = document.getElementById('cfgAppOn').checked;
      var notice = document.getElementById('cfgAppNotice').value.trim();
      var isTerminalOn = document.getElementById('cfgTerminalOn').checked;
      var terminalNotice = document.getElementById('cfgTerminalNotice').value.trim();
      var isManualOn = document.getElementById('cfgManualOn').checked;
      var otpPrice = parseFloat(document.getElementById('cfgOtpPrice').value) || 0.5;

      AndroidAdmin.saveAppConfig(isAppOn, notice, isTerminalOn, terminalNotice, isManualOn, otpPrice);
    }

    function renderUsers(list) {
      var container = document.getElementById('usersList');
      if (!list || list.length === 0) {
        container.innerHTML = '<div style="text-align: center; color: #64748b; padding: 20px;">No users found</div>';
        return;
      }

      var html = '';
      for (var i = 0; i < list.length; i++) {
        var u = list[i];
        var badgeClass = u.isBlocked ? 'badge-red' : 'badge-green';
        var badgeText = u.isBlocked ? 'BLOCKED 🚫' : 'ACTIVE 🟢';
        var blockActionText = u.isBlocked ? '🟢 Unblock' : '🚫 Block';
        var blockActionClass = u.isBlocked ? 'btn-success' : 'btn-danger';
        var balanceNum = parseFloat(u.balance || 0).toFixed(2);

        html += '<div class="item-card">' +
          '<div class="item-row">' +
            '<div>' +
              '<span class="item-title">' + escapeHtml(u.name || 'Anonymous') + '</span>' +
              '<div class="item-subtitle">' + escapeHtml(u.email) + ' • TG: ' + escapeHtml(u.telegram || 'N/A') + '</div>' +
            '</div>' +
            '<span class="badge ' + badgeClass + '">' + badgeText + '</span>' +
          '</div>' +
          '<div class="item-row" style="margin-top: 6px;">' +
            '<span style="font-size: 11px; color: #38bdf8; font-weight: bold;">Balance: ৳ ' + balanceNum + '</span>' +
          '</div>' +
          '<div class="action-row">' +
            '<button class="btn btn-primary btn-sm" onclick="editUserBalance(\'' + escapeHtml(u.email) + '\', ' + (u.balance || 0) + ')">💰 Edit Balance</button>' +
            '<button class="btn ' + blockActionClass + ' btn-sm" onclick="toggleBlock(\'' + escapeHtml(u.email) + '\', ' + (!u.isBlocked) + ')">' + blockActionText + '</button>' +
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
        if ((u.email && u.email.toLowerCase().indexOf(q) !== -1) ||
            (u.name && u.name.toLowerCase().indexOf(q) !== -1) ||
            (u.telegram && u.telegram.toLowerCase().indexOf(q) !== -1)) {
          filtered.push(u);
        }
      }
      renderUsers(filtered);
    }

    function editUserBalance(email, currentBal) {
      var newBal = prompt('Enter new balance for ' + email + ':', currentBal);
      if (newBal !== null && !isNaN(newBal)) {
        AndroidAdmin.updateUserBalance(email, parseFloat(newBal));
      }
    }

    function toggleBlock(email, block) {
      if (confirm((block ? 'Block ' : 'Unblock ') + email + '?')) {
        AndroidAdmin.toggleUserBlock(email, block);
      }
    }

    function renderWithdrawals(list) {
      var container = document.getElementById('withdrawalsList');
      if (!list || list.length === 0) {
        container.innerHTML = '<div style="text-align: center; color: #64748b; padding: 20px;">No withdrawal requests</div>';
        return;
      }

      var html = '';
      for (var i = 0; i < list.length; i++) {
        var w = list[i];
        var badgeClass = 'badge-yellow';
        if (w.status === 'completed' || w.status === 'approved') badgeClass = 'badge-green';
        if (w.status === 'rejected') badgeClass = 'badge-red';

        var dateStr = w.timestamp ? new Date(w.timestamp).toLocaleDateString() : '';
        var amtStr = parseFloat(w.amount || 0).toFixed(2);

        html += '<div class="item-card">' +
          '<div class="item-row">' +
            '<div>' +
              '<span class="item-title">' + escapeHtml(w.name || 'User') + ' (' + escapeHtml(w.method) + ')</span>' +
              '<div class="item-subtitle">' + escapeHtml(w.email) + ' • ' + dateStr + '</div>' +
            '</div>' +
            '<span class="badge ' + badgeClass + '">' + escapeHtml(w.status.toUpperCase()) + '</span>' +
          '</div>' +
          '<div class="item-row" style="margin-top: 6px;">' +
            '<span style="font-size: 12px; font-weight: bold; color: #34d399;">Amount: ৳ ' + amtStr + '</span>' +
            '<span style="font-size: 11px; color: #cbd5e1; cursor: pointer;" onclick="AndroidAdmin.copyText(\'' + escapeHtml(w.value) + '\', \'Account Details\')">📋 ' + escapeHtml(w.value) + '</span>' +
          '</div>' +
          '<div class="action-row">' +
            '<button class="btn btn-success btn-sm" onclick="updateWithdrawStatus(\'' + escapeHtml(w.id) + '\', \'completed\')">✅ Approve & Pay</button>' +
            '<button class="btn btn-danger btn-sm" onclick="updateWithdrawStatus(\'' + escapeHtml(w.id) + '\', \'rejected\')">❌ Reject</button>' +
          '</div>' +
        '</div>';
      }
      container.innerHTML = html;
    }

    function updateWithdrawStatus(id, status) {
      AndroidAdmin.updateWithdrawalStatus(id, status);
    }

    function renderKeys(list) {
      var container = document.getElementById('keysList');
      if (!list || list.length === 0) {
        container.innerHTML = '<div style="text-align: center; color: #64748b; padding: 20px;">No master keys found</div>';
        return;
      }

      var html = '';
      for (var i = 0; i < list.length; i++) {
        var k = list[i];
        var badgeClass = k.status === 'used' ? 'badge-red' : 'badge-green';
        var subtitle = k.usedByApiKey ? 'Used by: ' + escapeHtml(k.usedByApiKey) : 'Available for activation';

        html += '<div class="item-card">' +
          '<div class="item-row">' +
            '<div>' +
              '<span class="item-title" style="color: #38bdf8; font-family: monospace;">🔑 ' + escapeHtml(k.key) + '</span>' +
              '<div class="item-subtitle">' + subtitle + '</div>' +
            '</div>' +
            '<span class="badge ' + badgeClass + '">' + escapeHtml(k.status.toUpperCase()) + '</span>' +
          '</div>' +
          '<div class="action-row">' +
            '<button class="btn btn-primary btn-sm" onclick="AndroidAdmin.copyText(\'' + escapeHtml(k.key) + '\', \'Master Key\')">📋 Copy Key</button>' +
            '<button class="btn btn-danger btn-sm" onclick="deleteKey(\'' + escapeHtml(k.key) + '\')">🗑️ Delete</button>' +
          '</div>' +
        '</div>';
      }
      container.innerHTML = html;
    }

    function submitNewKey() {
      var input = document.getElementById('newKeyInput');
      var val = input.value.trim();
      if (!val) {
        alert('Please enter a key string');
        return;
      }
      AndroidAdmin.addMasterKey(val);
      input.value = '';
    }

    function deleteKey(key) {
      if (confirm('Delete master key ' + key + '?')) {
        AndroidAdmin.deleteMasterKey(key);
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
