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
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onAuthSuccess()
        }
    }

    // Sync Kotlin ViewModel state with the HTML/JS UI
    LaunchedEffect(uiState.isAuthLoading, uiState.authError, uiState.authSuccess) {
        val webView = webViewRef ?: return@LaunchedEffect
        val isLoading = uiState.isAuthLoading
        val errorMsg = uiState.authError?.replace("'", "\\'")?.replace("\n", " ") ?: ""
        val successMsg = uiState.authSuccess?.replace("'", "\\'")?.replace("\n", " ") ?: ""

        val script = """
            if (window.handleKotlinState) {
                window.handleKotlinState($isLoading, '$errorMsg', '$successMsg');
            }
        """.trimIndent()
        
        Handler(Looper.getMainLooper()).post {
            webView.evaluateJavascript(script, null)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    setBackgroundColor(0xFF0F172A.toInt())

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false
                        }
                    }

                    class AndroidAuthBridge(private val context: Context) {
                        @JavascriptInterface
                        fun performLogin(email: String, pass: String) {
                            Handler(Looper.getMainLooper()).post {
                                viewModel.clearAuthMessages()
                                viewModel.logInUser(email.trim(), pass) { _, _ -> }
                            }
                        }

                        @JavascriptInterface
                        fun performSignup(fname: String, lname: String, tg: String, email: String, pass: String) {
                            Handler(Looper.getMainLooper()).post {
                                viewModel.clearAuthMessages()
                                viewModel.signUpUser(
                                    fname.trim(),
                                    lname.trim(),
                                    tg.trim(),
                                    email.trim(),
                                    pass
                                ) { success, _ ->
                                    if (success) {
                                        // Switch back to login mode on web UI
                                        webViewRef?.evaluateJavascript("window.switchToLogin();", null)
                                    }
                                }
                            }
                        }

                        @JavascriptInterface
                        fun clearMessages() {
                            Handler(Looper.getMainLooper()).post {
                                viewModel.clearAuthMessages()
                            }
                        }
                    }

                    addJavascriptInterface(AndroidAuthBridge(ctx), "AndroidBridge")
                    loadDataWithBaseURL("https://local.auth/", getAuthHtml(), "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = {
                webViewRef = it
            }
        )
    }
}

private fun getAuthHtml(): String {
    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <title>Auth Screen</title>
        <style>
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
                -webkit-tap-highlight-color: transparent;
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            }
            body {
                background-color: #0b0f19;
                color: #f1f5f9;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                padding: 20px 16px;
            }
            .container {
                width: 100%;
                max-width: 400px;
                display: flex;
                flex-direction: column;
                gap: 16px;
            }
            .header {
                text-align: center;
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 8px;
            }
            .logo-icon {
                width: 60px;
                height: 60px;
                background-color: #1e293b;
                border: 1px solid #334155;
                border-radius: 16px;
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 28px;
                margin-bottom: 4px;
            }
            .title {
                font-size: 22px;
                font-weight: 800;
                letter-spacing: 0.5px;
                color: #ffffff;
            }
            .subtitle {
                font-size: 13px;
                color: #94a3b8;
                line-height: 1.4;
            }
            .card {
                background: #111827;
                border: 1px solid #1f2937;
                border-radius: 14px;
                padding: 18px 16px;
                display: flex;
                flex-direction: column;
                gap: 12px;
            }
            .input-group {
                display: flex;
                flex-direction: column;
                gap: 5px;
            }
            .label {
                font-size: 12px;
                font-weight: 600;
                color: #cbd5e1;
            }
            .input-box {
                position: relative;
                display: flex;
                align-items: center;
            }
            .input-box input {
                width: 100%;
                height: 44px;
                background: #1f2937;
                border: 1px solid #374151;
                border-radius: 8px;
                padding: 0 12px;
                font-size: 14px;
                color: #ffffff;
                outline: none;
                transition: border-color 0.15s ease;
            }
            .input-box input:focus {
                border-color: #2563eb;
            }
            .input-box input::placeholder {
                color: #6b7280;
            }
            .toggle-pass {
                position: absolute;
                right: 12px;
                cursor: pointer;
                color: #9ca3af;
                font-size: 13px;
                user-select: none;
                font-weight: bold;
            }
            .btn-primary {
                width: 100%;
                height: 46px;
                background: #2563eb;
                color: #ffffff;
                border: none;
                border-radius: 8px;
                font-size: 15px;
                font-weight: 700;
                cursor: pointer;
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 8px;
                transition: background 0.15s ease, opacity 0.15s ease;
            }
            .btn-primary:active {
                background: #1d4ed8;
            }
            .btn-primary:disabled {
                opacity: 0.6;
                cursor: not-allowed;
            }
            .alert-box {
                padding: 10px 12px;
                border-radius: 8px;
                font-size: 13px;
                line-height: 1.4;
                font-weight: 500;
                display: none;
            }
            .alert-error {
                background: #450a0a;
                color: #fca5a5;
                border: 1px solid #7f1d1d;
            }
            .alert-success {
                background: #052e16;
                color: #86efac;
                border: 1px solid #14532d;
            }
            .toggle-mode {
                text-align: center;
                font-size: 13px;
                color: #94a3b8;
                margin-top: 4px;
            }
            .toggle-mode a {
                color: #38bdf8;
                font-weight: 700;
                text-decoration: none;
                margin-left: 4px;
                cursor: pointer;
            }
            .spinner {
                width: 18px;
                height: 18px;
                border: 2px solid #ffffff;
                border-top-color: transparent;
                border-radius: 50%;
                animation: spin 0.6s linear infinite;
            }
            @keyframes spin {
                to { transform: rotate(360deg); }
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <div class="logo-icon">🛡️</div>
                <div class="title">FB TOOL 💣💥</div>
                <div class="subtitle" id="subTitle">অ্যাপ ব্যবহার করতে আপনার অ্যাকাউন্টে লগইন করুন</div>
            </div>

            <!-- Error Banner -->
            <div id="errorBanner" class="alert-box alert-error"></div>

            <!-- Success Banner -->
            <div id="successBanner" class="alert-box alert-success"></div>

            <!-- Form Card -->
            <div class="card">
                <!-- Signup Only Fields -->
                <div id="signupFields" style="display: none; flex-direction: column; gap: 12px;">
                    <div class="input-group">
                        <label class="label">First Name (প্রথম নাম)</label>
                        <div class="input-box">
                            <input type="text" id="fname" placeholder="Enter first name" autocomplete="off" />
                        </div>
                    </div>
                    <div class="input-group">
                        <label class="label">Last Name (শেষ নাম)</label>
                        <div class="input-box">
                            <input type="text" id="lname" placeholder="Enter last name" autocomplete="off" />
                        </div>
                    </div>
                    <div class="input-group">
                        <label class="label">Telegram Username</label>
                        <div class="input-box">
                            <input type="text" id="tg" placeholder="e.g. username" autocomplete="off" />
                        </div>
                    </div>
                </div>

                <!-- Common Fields -->
                <div class="input-group">
                    <label class="label">Email (ইমেইল)</label>
                    <div class="input-box">
                        <input type="email" id="email" placeholder="example@mail.com" autocomplete="off" />
                    </div>
                </div>

                <div class="input-group">
                    <label class="label">Password (পাসওয়ার্ড)</label>
                    <div class="input-box">
                        <input type="password" id="pass" placeholder="••••••••" />
                        <span class="toggle-pass" id="togglePassBtn" onclick="togglePasswordVisibility()">SHOW</span>
                    </div>
                </div>
            </div>

            <!-- Action Button -->
            <button class="btn-primary" id="actionBtn" onclick="handleSubmit()">
                <span id="btnSpinner" class="spinner" style="display: none;"></span>
                <span id="btnText">Log In (লগইন করুন)</span>
            </button>

            <!-- Mode Toggle Link -->
            <div class="toggle-mode">
                <span id="toggleQuestion">নতুন অ্যাকাউন্ট তৈরি করতে চান?</span>
                <a id="toggleLink" onclick="switchMode()">Sign Up (নিবন্ধন)</a>
            </div>
        </div>

        <script>
            let isLoginMode = true;
            let isLoading = false;

            function togglePasswordVisibility() {
                const passInput = document.getElementById('pass');
                const toggleBtn = document.getElementById('togglePassBtn');
                if (passInput.type === 'password') {
                    passInput.type = 'text';
                    toggleBtn.innerText = 'HIDE';
                } else {
                    passInput.type = 'password';
                    toggleBtn.innerText = 'SHOW';
                }
            }

            function switchMode() {
                if (isLoading) return;
                isLoginMode = !isLoginMode;
                updateModeUI();
                if (window.AndroidBridge && window.AndroidBridge.clearMessages) {
                    window.AndroidBridge.clearMessages();
                }
                hideAlerts();
            }

            function switchToLogin() {
                isLoginMode = true;
                updateModeUI();
            }
            window.switchToLogin = switchToLogin;

            function updateModeUI() {
                const signupFields = document.getElementById('signupFields');
                const subTitle = document.getElementById('subTitle');
                const btnText = document.getElementById('btnText');
                const toggleQuestion = document.getElementById('toggleQuestion');
                const toggleLink = document.getElementById('toggleLink');

                if (isLoginMode) {
                    signupFields.style.display = 'none';
                    subTitle.innerText = 'অ্যাপ ব্যবহার করতে আপনার অ্যাকাউন্টে লগইন করুন';
                    btnText.innerText = 'Log In (লগইন করুন)';
                    toggleQuestion.innerText = 'নতুন অ্যাকাউন্ট তৈরি করতে চান?';
                    toggleLink.innerText = 'Sign Up (নিবন্ধন)';
                } else {
                    signupFields.style.display = 'flex';
                    subTitle.innerText = 'নতুন অ্যাকাউন্ট তৈরি করতে তথ্যগুলো পূরণ করুন';
                    btnText.innerText = 'Sign Up (নিবন্ধন করুন)';
                    toggleQuestion.innerText = 'ইতিমধ্যে অ্যাকাউন্ট তৈরি করা আছে?';
                    toggleLink.innerText = 'Log In (লগইন)';
                }
            }

            function hideAlerts() {
                document.getElementById('errorBanner').style.display = 'none';
                document.getElementById('successBanner').style.display = 'none';
            }

            function handleSubmit() {
                if (isLoading) return;

                const email = document.getElementById('email').value.trim();
                const pass = document.getElementById('pass').value;

                if (!email) {
                    showLocalError('Please enter your email.');
                    return;
                }
                if (!pass) {
                    showLocalError('Please enter your password.');
                    return;
                }

                hideAlerts();

                if (isLoginMode) {
                    if (window.AndroidBridge && window.AndroidBridge.performLogin) {
                        window.AndroidBridge.performLogin(email, pass);
                    }
                } else {
                    const fname = document.getElementById('fname').value.trim();
                    const lname = document.getElementById('lname').value.trim();
                    const tg = document.getElementById('tg').value.trim();

                    if (!fname) {
                        showLocalError('Please enter your first name.');
                        return;
                    }
                    if (window.AndroidBridge && window.AndroidBridge.performSignup) {
                        window.AndroidBridge.performSignup(fname, lname, tg, email, pass);
                    }
                }
            }

            function showLocalError(msg) {
                const errorBanner = document.getElementById('errorBanner');
                errorBanner.innerText = '❌ ' + msg;
                errorBanner.style.display = 'block';
                document.getElementById('successBanner').style.display = 'none';
            }

            // Called from Kotlin side
            window.handleKotlinState = function(loading, error, success) {
                isLoading = loading;
                const actionBtn = document.getElementById('actionBtn');
                const btnSpinner = document.getElementById('btnSpinner');
                const errorBanner = document.getElementById('errorBanner');
                const successBanner = document.getElementById('successBanner');

                actionBtn.disabled = loading;
                btnSpinner.style.display = loading ? 'inline-block' : 'none';

                if (error && error.length > 0) {
                    errorBanner.innerText = '❌ ' + error;
                    errorBanner.style.display = 'block';
                } else {
                    errorBanner.style.display = 'none';
                }

                if (success && success.length > 0) {
                    successBanner.innerText = '✅ ' + success;
                    successBanner.style.display = 'block';
                } else {
                    successBanner.style.display = 'none';
                }
            };
        </script>
    </body>
    </html>
    """.trimIndent()
}
