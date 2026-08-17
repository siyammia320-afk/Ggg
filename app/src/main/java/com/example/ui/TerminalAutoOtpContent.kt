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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray

enum class CreationMethod(val title: String) {
    NM_OFFICIAL("NM OFFICAL"),
    NM_LIMIT("NM LIMIT")
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TerminalAutoOtpScreen(
    onClose: () -> Unit,
    onStart: (
        range: String,
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
    terminalDisabledNotice: String = "Terminal is currently disabled by admin.",
    successCount: Int = 0,
    noAccountCount: Int = 0,
    existCount: Int = 0,
    failedCount: Int = 0,
    availableRanges: List<String> = emptyList(),
    onRefreshRanges: () -> Unit = {}
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var lastLogIndex by remember { mutableIntStateOf(0) }

    // Sync Counters and Running State to HTML UI
    LaunchedEffect(isRunning, isTerminalEnabledByAdmin, successCount, noAccountCount, existCount, failedCount, proxyStatus, terminalDisabledNotice) {
        val webView = webViewRef ?: return@LaunchedEffect
        val safeProxy = proxyStatus.replace("'", "\\'")
        val safeNotice = terminalDisabledNotice.replace("'", "\\'")
        val script = """
            if (window.updateStatsAndState) {
                window.updateStatsAndState(
                    $isRunning,
                    $isTerminalEnabledByAdmin,
                    $successCount,
                    $noAccountCount,
                    $existCount,
                    $failedCount,
                    '$safeProxy',
                    '$safeNotice'
                );
            }
        """.trimIndent()
        Handler(Looper.getMainLooper()).post {
            webView.evaluateJavascript(script, null)
        }
    }

    // Sync Available Ranges to HTML UI
    LaunchedEffect(availableRanges) {
        val webView = webViewRef ?: return@LaunchedEffect
        val jsonArray = JSONArray(availableRanges).toString()
        val script = """
            if (window.updateRanges) {
                window.updateRanges($jsonArray);
            }
        """.trimIndent()
        Handler(Looper.getMainLooper()).post {
            webView.evaluateJavascript(script, null)
        }
    }

    // Fast incremental log streaming to HTML UI (Zero Recomposition Lag)
    LaunchedEffect(logs.size) {
        val webView = webViewRef ?: return@LaunchedEffect
        if (logs.isEmpty()) {
            lastLogIndex = 0
            Handler(Looper.getMainLooper()).post {
                webView.evaluateJavascript("if (window.clearTerminalLogs) window.clearTerminalLogs();", null)
            }
            return@LaunchedEffect
        }

        if (logs.size < lastLogIndex) {
            // Logs were reset
            lastLogIndex = 0
        }

        val newLines = logs.subList(lastLogIndex, logs.size)
        if (newLines.isNotEmpty()) {
            val jsonArray = JSONArray(newLines).toString()
            val script = """
                if (window.appendTerminalLogs) {
                    window.appendTerminalLogs($jsonArray);
                }
            """.trimIndent()
            Handler(Looper.getMainLooper()).post {
                webView.evaluateJavascript(script, null)
            }
            lastLogIndex = logs.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    setBackgroundColor(0xFF0A0F1D.toInt())

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false
                        }
                    }

                    class AndroidTerminalBridge {
                        @JavascriptInterface
                        fun startCreation(range: String, countStr: String, threadsStr: String, methodStr: String, isFindAccount: Boolean, password: String) {
                            Handler(Looper.getMainLooper()).post {
                                val count = (countStr.toIntOrNull() ?: 5).coerceIn(1, 5)
                                val threads = (threadsStr.toIntOrNull() ?: 2).coerceIn(1, 10)
                                val method = if (methodStr.equals("NM_LIMIT", ignoreCase = true)) {
                                    CreationMethod.NM_LIMIT
                                } else {
                                    CreationMethod.NM_OFFICIAL
                                }
                                val finalPwd = if (method == CreationMethod.NM_OFFICIAL) "arafat@@##" else password.trim().ifEmpty { "arafat@@##" }
                                onStart(range.trim(), count, threads, method, isFindAccount, finalPwd)
                            }
                        }

                        @JavascriptInterface
                        fun stopCreation() {
                            Handler(Looper.getMainLooper()).post {
                                onStop()
                            }
                        }

                        @JavascriptInterface
                        fun closeScreen() {
                            Handler(Looper.getMainLooper()).post {
                                if (isRunning) onStop()
                                onClose()
                            }
                        }

                        @JavascriptInterface
                        fun refreshRanges() {
                            Handler(Looper.getMainLooper()).post {
                                onRefreshRanges()
                            }
                        }

                        @JavascriptInterface
                        fun copyLogs(content: String) {
                            Handler(Looper.getMainLooper()).post {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Terminal Logs", content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Logs Copied to Clipboard", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    addJavascriptInterface(AndroidTerminalBridge(), "AndroidBridge")
                    loadDataWithBaseURL("https://local.terminal/", getTerminalHtml(initialPassword), "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = {
                webViewRef = it
            }
        )
    }
}

private fun getTerminalHtml(defaultPassword: String): String {
    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <title>Terminal Engine</title>
        <style>
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
                -webkit-tap-highlight-color: transparent;
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, monospace;
            }
            body {
                background-color: #0a0f1d;
                color: #f1f5f9;
                display: flex;
                flex-direction: column;
                height: 100vh;
                overflow: hidden;
            }
            /* Header */
            .header-bar {
                background: #1e293b;
                padding: 8px 12px;
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 1px solid #334155;
            }
            .header-left {
                display: flex;
                align-items: center;
                gap: 8px;
            }
            .status-dot {
                width: 10px;
                height: 10px;
                border-radius: 50%;
                background: #94a3b8;
            }
            .status-dot.running { background: #22c55e; }
            .status-dot.disabled { background: #ef4444; }
            .title {
                font-size: 15px;
                font-weight: 800;
                color: #ffffff;
                letter-spacing: 0.5px;
            }
            .disabled-tag {
                font-size: 10px;
                font-weight: bold;
                color: #f87171;
            }
            .close-btn {
                background: none;
                border: none;
                color: #cbd5e1;
                font-size: 18px;
                font-weight: bold;
                cursor: pointer;
                padding: 4px 8px;
                border-radius: 4px;
            }
            .close-btn:active {
                background: #334155;
            }

            /* Proxy Bar */
            .proxy-bar {
                background: #111827;
                padding: 4px 12px;
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-size: 11px;
                font-family: monospace;
                border-bottom: 1px solid #1f2937;
            }
            .proxy-text {
                color: #38bdf8;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                max-width: 65%;
            }
            .method-text {
                color: #a5b4fc;
                font-weight: bold;
            }

            /* Admin Notice */
            .admin-banner {
                background: #7f1d1d;
                color: #fef2f2;
                padding: 8px 12px;
                font-size: 12px;
                font-weight: bold;
                display: none;
                margin: 4px 8px;
                border-radius: 6px;
            }

            /* Stats Counter Bar */
            .stats-bar {
                display: grid;
                grid-template-columns: 1fr 1fr 1fr 1fr;
                gap: 6px;
                padding: 6px 8px;
            }
            .stat-chip {
                background: #111827;
                border: 1px solid #1f2937;
                border-radius: 6px;
                padding: 4px 6px;
                display: flex;
                flex-direction: column;
                align-items: center;
            }
            .stat-label {
                font-size: 9px;
                color: #94a3b8;
                font-weight: 600;
            }
            .stat-val {
                font-size: 13px;
                font-weight: 800;
                font-family: monospace;
            }
            .val-success { color: #22c55e; }
            .val-noacc { color: #38bdf8; }
            .val-exist { color: #f59e0b; }
            .val-failed { color: #ef4444; }

            /* Controls Container */
            .controls-card {
                background: #1e293b;
                border-radius: 8px;
                margin: 0 8px 6px 8px;
                padding: 8px;
                display: flex;
                flex-direction: column;
                gap: 6px;
            }
            .row-flex {
                display: flex;
                gap: 6px;
                align-items: center;
            }
            .btn-method {
                flex: 1;
                height: 28px;
                background: #334155;
                color: #94a3b8;
                border: 1px solid transparent;
                border-radius: 6px;
                font-size: 11px;
                font-weight: bold;
                cursor: pointer;
            }
            .btn-method.active {
                background: #2563eb;
                color: #ffffff;
                border-color: #60a5fa;
            }
            .btn-toggle {
                flex: 1;
                height: 28px;
                background: #334155;
                color: #94a3b8;
                border: 1px solid transparent;
                border-radius: 6px;
                font-size: 11px;
                font-weight: bold;
                cursor: pointer;
            }
            .btn-toggle.active {
                background: #059669;
                color: #ffffff;
                border-color: #34d399;
            }

            /* Inputs */
            .input-label {
                font-size: 10px;
                color: #94a3b8;
                font-weight: 600;
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 2px;
            }
            .input-box {
                width: 100%;
                height: 32px;
                background: #0f172a;
                border: 1px solid #38bdf8;
                border-radius: 6px;
                padding: 0 8px;
                color: #ffffff;
                font-size: 12px;
                font-family: monospace;
                font-weight: bold;
                outline: none;
            }
            .input-box:disabled {
                background: #1e293b;
                border-color: #475569;
                color: #94a3b8;
            }
            .refresh-link {
                color: #38bdf8;
                font-size: 10px;
                font-weight: bold;
                cursor: pointer;
            }

            /* Chips */
            .chips-scroll {
                display: flex;
                gap: 4px;
                overflow-x: auto;
                padding-bottom: 2px;
                scrollbar-width: none;
            }
            .chips-scroll::-webkit-scrollbar { display: none; }
            .chip {
                background: #334155;
                color: #ffffff;
                font-size: 10px;
                font-family: monospace;
                font-weight: bold;
                padding: 3px 8px;
                border-radius: 4px;
                cursor: pointer;
                white-space: nowrap;
            }
            .chip.selected {
                background: #2563eb;
            }

            /* Bottom Action Bar */
            .btn-start {
                height: 34px;
                background: #16a34a;
                color: #ffffff;
                border: none;
                border-radius: 6px;
                font-size: 12px;
                font-weight: 800;
                cursor: pointer;
                padding: 0 12px;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 4px;
                flex: 1.2;
            }
            .btn-start:disabled {
                opacity: 0.5;
                cursor: not-allowed;
            }
            .btn-stop {
                height: 34px;
                background: #dc2626;
                color: #ffffff;
                border: none;
                border-radius: 6px;
                font-size: 12px;
                font-weight: 800;
                cursor: pointer;
                padding: 0 12px;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 4px;
                flex: 1.2;
            }

            /* Quick Num Selector (1..5) */
            .num-btn {
                width: 24px;
                height: 24px;
                background: #334155;
                color: #ffffff;
                border: none;
                border-radius: 4px;
                font-size: 11px;
                font-weight: bold;
                cursor: pointer;
            }
            .num-btn.active {
                background: #2563eb;
            }

            /* Terminal Log Console */
            .console-wrapper {
                flex: 1;
                margin: 0 8px 8px 8px;
                background: #050811;
                border: 1px solid #1e293b;
                border-radius: 8px;
                display: flex;
                flex-direction: column;
                overflow: hidden;
            }
            .console-header {
                background: #0f172a;
                padding: 6px 10px;
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 1px solid #1e293b;
            }
            .console-title {
                font-size: 11px;
                font-weight: bold;
                color: #38bdf8;
                font-family: monospace;
            }
            .console-actions {
                display: flex;
                gap: 6px;
            }
            .btn-tool {
                background: #1e293b;
                border: 1px solid #334155;
                color: #cbd5e1;
                font-size: 10px;
                font-weight: bold;
                padding: 2px 8px;
                border-radius: 4px;
                cursor: pointer;
            }
            .btn-tool:active { background: #334155; }
            .console-logs {
                flex: 1;
                padding: 8px;
                overflow-y: auto;
                font-family: 'Courier New', Courier, monospace;
                font-size: 11px;
                line-height: 1.35;
                display: flex;
                flex-direction: column;
                gap: 3px;
                word-break: break-all;
            }
            .log-line {
                color: #94a3b8;
            }
            .log-success { color: #4ade80; font-weight: bold; }
            .log-error { color: #f87171; }
            .log-warning { color: #fbbf24; }
            .log-info { color: #38bdf8; }
            .log-system { color: #a5b4fc; font-weight: bold; }
        </style>
    </head>
    <body>
        <!-- Header -->
        <div class="header-bar">
            <div class="header-left">
                <div id="statusDot" class="status-dot"></div>
                <div class="title">Terminal Engine ⚡</div>
                <div id="disabledTag" class="disabled-tag" style="display: none;">(DISABLED)</div>
            </div>
            <button class="close-btn" onclick="handleClose()">✕</button>
        </div>

        <!-- Proxy & Method -->
        <div class="proxy-bar">
            <span id="proxyText" class="proxy-text">🌐 Proxy: Checking...</span>
            <span id="methodText" class="method-text">NM OFFICAL</span>
        </div>

        <!-- Admin Disabled Notice -->
        <div id="adminNotice" class="admin-banner"></div>

        <!-- Stats Bar -->
        <div class="stats-bar">
            <div class="stat-chip">
                <span class="stat-label">Success</span>
                <span id="statSuccess" class="stat-val val-success">0</span>
            </div>
            <div class="stat-chip">
                <span class="stat-label">No Account</span>
                <span id="statNoAcc" class="stat-val val-noacc">0</span>
            </div>
            <div class="stat-chip">
                <span class="stat-label">Exist</span>
                <span id="statExist" class="stat-val val-exist">0</span>
            </div>
            <div class="stat-chip">
                <span class="stat-label">Failed</span>
                <span id="statFailed" class="stat-val val-failed">0</span>
            </div>
        </div>

        <!-- Controls Card -->
        <div class="controls-card">
            <!-- Method and Find Account Selectors -->
            <div class="row-flex">
                <button id="btnOfficial" class="btn-method active" onclick="selectMethod('NM_OFFICIAL')">NM OFFICAL</button>
                <button id="btnLimit" class="btn-method" onclick="selectMethod('NM_LIMIT')">NM LIMIT</button>
                <button id="btnFindAcc" class="btn-toggle" onclick="toggleFindAccount()">🔍 Find: OFF</button>
            </div>

            <!-- Password Box -->
            <div>
                <div class="input-label">
                    <span id="pwdLabel">🔑 Password (Auto Locked: arafat@@##)</span>
                </div>
                <input type="text" id="pwdInput" class="input-box" value="${defaultPassword}" disabled />
            </div>

            <!-- Range Input -->
            <div>
                <div class="input-label">
                    <span>📞 Phone Range</span>
                    <span class="refresh-link" onclick="handleRefreshRanges()">🔄 Refresh</span>
                </div>
                <input type="text" id="rangeInput" class="input-box" placeholder="e.g. 26134XXX" />
            </div>

            <!-- Range Quick Chips -->
            <div id="chipsContainer" class="chips-scroll"></div>

            <!-- Quantity (1..5), Threads, and Action -->
            <div class="row-flex">
                <!-- Accounts Qty (Strictly Max 5) -->
                <div style="flex: 1;">
                    <div class="input-label">
                        <span>Accounts</span>
                        <span style="color: #38bdf8; font-size: 8px; font-weight: bold;">MAX 5</span>
                    </div>
                    <div class="row-flex" style="gap: 2px;">
                        <button class="num-btn" onclick="selectQty(1)">1</button>
                        <button class="num-btn" onclick="selectQty(2)">2</button>
                        <button class="num-btn" onclick="selectQty(3)">3</button>
                        <button class="num-btn" onclick="selectQty(4)">4</button>
                        <button class="num-btn active" id="btnQty5" onclick="selectQty(5)">5</button>
                    </div>
                </div>

                <!-- Threads -->
                <div style="width: 50px;">
                    <div class="input-label"><span>Threads</span></div>
                    <input type="number" id="threadInput" class="input-box" value="2" min="1" max="10" />
                </div>

                <!-- Start / Stop Button -->
                <div style="flex: 1.2; display: flex; align-items: flex-end; height: 100%;">
                    <button id="btnStart" class="btn-start" onclick="handleStart()">▶ START</button>
                    <button id="btnStop" class="btn-stop" style="display: none;" onclick="handleStop()">⏹ STOP</button>
                </div>
            </div>
        </div>

        <!-- Terminal Console -->
        <div class="console-wrapper">
            <div class="console-header">
                <span class="console-title">>_ Live Terminal Stream</span>
                <div class="console-actions">
                    <button class="btn-tool" onclick="handleCopyLogs()">📋 Copy</button>
                    <button class="btn-tool" onclick="handleClearLogs()">🗑️ Clear</button>
                </div>
            </div>
            <div id="consoleLogs" class="console-logs">
                <div class="log-line">>_ Terminal Ready. Click START to begin.</div>
            </div>
        </div>

        <script>
            let currentMethod = 'NM_OFFICIAL';
            let isFindAccountOn = false;
            let currentQty = 5;
            let isRunningEngine = false;
            let isTerminalEnabled = true;
            let allLogsText = [];

            function selectMethod(method) {
                if (isRunningEngine) return;
                currentMethod = method;
                const btnOff = document.getElementById('btnOfficial');
                const btnLim = document.getElementById('btnLimit');
                const pwdInput = document.getElementById('pwdInput');
                const pwdLabel = document.getElementById('pwdLabel');
                const methodText = document.getElementById('methodText');

                if (method === 'NM_OFFICIAL') {
                    btnOff.classList.add('active');
                    btnLim.classList.remove('active');
                    pwdInput.value = 'arafat@@##';
                    pwdInput.disabled = true;
                    pwdLabel.innerText = '🔑 Password (Auto Locked: arafat@@##)';
                    methodText.innerText = 'NM OFFICAL';
                } else {
                    btnLim.classList.add('active');
                    btnOff.classList.remove('active');
                    pwdInput.disabled = false;
                    pwdLabel.innerText = '🔑 Custom Password (NM LIMIT)';
                    methodText.innerText = 'NM LIMIT';
                }
            }

            function toggleFindAccount() {
                if (isRunningEngine) return;
                isFindAccountOn = !isFindAccountOn;
                const btn = document.getElementById('btnFindAcc');
                if (isFindAccountOn) {
                    btn.classList.add('active');
                    btn.innerText = '🔍 Find: ON';
                } else {
                    btn.classList.remove('active');
                    btn.innerText = '🔍 Find: OFF';
                }
            }

            function selectQty(num) {
                if (isRunningEngine) return;
                currentQty = Math.max(1, Math.min(5, num));
                document.querySelectorAll('.num-btn').forEach((btn, idx) => {
                    if (idx + 1 === currentQty) {
                        btn.classList.add('active');
                    } else {
                        btn.classList.remove('active');
                    }
                });
            }

            function handleStart() {
                if (!isTerminalEnabled) return;
                const range = document.getElementById('rangeInput').value.trim();
                const threads = document.getElementById('threadInput').value.trim() || '2';
                const pwd = document.getElementById('pwdInput').value;

                if (!range) {
                    alert('Please enter or select a range first.');
                    return;
                }

                if (window.AndroidBridge && window.AndroidBridge.startCreation) {
                    window.AndroidBridge.startCreation(
                        range,
                        currentQty.toString(),
                        threads,
                        currentMethod,
                        isFindAccountOn,
                        pwd
                    );
                }
            }

            function handleStop() {
                if (window.AndroidBridge && window.AndroidBridge.stopCreation) {
                    window.AndroidBridge.stopCreation();
                }
            }

            function handleClose() {
                if (window.AndroidBridge && window.AndroidBridge.closeScreen) {
                    window.AndroidBridge.closeScreen();
                }
            }

            function handleRefreshRanges() {
                if (isRunningEngine) return;
                if (window.AndroidBridge && window.AndroidBridge.refreshRanges) {
                    window.AndroidBridge.refreshRanges();
                }
            }

            function handleCopyLogs() {
                const logsStr = allLogsText.join('\n');
                if (window.AndroidBridge && window.AndroidBridge.copyLogs) {
                    window.AndroidBridge.copyLogs(logsStr);
                }
            }

            function handleClearLogs() {
                allLogsText = [];
                const container = document.getElementById('consoleLogs');
                container.innerHTML = '<div class="log-line">>_ Terminal Cleared.</div>';
            }

            // Called by Kotlin to push stats & engine state
            window.updateStatsAndState = function(running, enabled, success, noAcc, exist, failed, proxy, notice) {
                isRunningEngine = running;
                isTerminalEnabled = enabled;

                document.getElementById('statSuccess').innerText = success;
                document.getElementById('statNoAcc').innerText = noAcc;
                document.getElementById('statExist').innerText = exist;
                document.getElementById('statFailed').innerText = failed;
                document.getElementById('proxyText').innerText = '🌐 Proxy: ' + proxy;

                const statusDot = document.getElementById('statusDot');
                const disabledTag = document.getElementById('disabledTag');
                const adminNotice = document.getElementById('adminNotice');
                const btnStart = document.getElementById('btnStart');
                const btnStop = document.getElementById('btnStop');

                if (!enabled) {
                    statusDot.className = 'status-dot disabled';
                    disabledTag.style.display = 'inline';
                    adminNotice.style.display = 'block';
                    adminNotice.innerText = '⚠️ ' + notice;
                    btnStart.disabled = true;
                } else {
                    disabledTag.style.display = 'none';
                    adminNotice.style.display = 'none';
                    btnStart.disabled = false;

                    if (running) {
                        statusDot.className = 'status-dot running';
                        btnStart.style.display = 'none';
                        btnStop.style.display = 'flex';
                    } else {
                        statusDot.className = 'status-dot';
                        btnStart.style.display = 'flex';
                        btnStop.style.display = 'none';
                    }
                }
            };

            // Called by Kotlin to update ranges
            window.updateRanges = function(ranges) {
                const container = document.getElementById('chipsContainer');
                container.innerHTML = '';
                const rangeInput = document.getElementById('rangeInput');

                if (ranges && ranges.length > 0) {
                    if (!rangeInput.value) {
                        rangeInput.value = ranges[0];
                    }
                    ranges.slice(0, 12).forEach((r) => {
                        const chip = document.createElement('div');
                        chip.className = 'chip' + (rangeInput.value === r ? ' selected' : '');
                        chip.innerText = r;
                        chip.onclick = function() {
                            if (!isRunningEngine) {
                                rangeInput.value = r;
                                document.querySelectorAll('.chip').forEach(c => c.classList.remove('selected'));
                                chip.classList.add('selected');
                            }
                        };
                        container.appendChild(chip);
                    });
                }
            };

            // Fast incremental log appending (Zero lag)
            window.appendTerminalLogs = function(newLines) {
                const container = document.getElementById('consoleLogs');
                newLines.forEach((line) => {
                    allLogsText.push(line);
                    const div = document.createElement('div');
                    div.className = 'log-line';
                    
                    if (line.includes('[SUCCESS]') || line.includes('OTP Code:') || line.includes('DONE')) {
                        div.className = 'log-line log-success';
                    } else if (line.includes('[FAILED]') || line.includes('[ERROR]') || line.includes('❌')) {
                        div.className = 'log-line log-error';
                    } else if (line.includes('[WARNING]') || line.includes('[EXISTS]') || line.includes('⚠️')) {
                        div.className = 'log-line log-warning';
                    } else if (line.includes('Got Number:') || line.includes('📱')) {
                        div.className = 'log-line log-info';
                    } else if (line.includes('[SYSTEM]') || line.includes('[CONFIG]') || line.includes('🚀')) {
                        div.className = 'log-line log-system';
                    }

                    div.innerText = line;
                    container.appendChild(div);
                });

                // Auto scroll to bottom
                container.scrollTop = container.scrollHeight;
            };

            window.clearTerminalLogs = function() {
                handleClearLogs();
            };
        </script>
    </body>
    </html>
    """.trimIndent()
}
