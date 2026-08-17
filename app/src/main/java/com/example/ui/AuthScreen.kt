package com.example.ui

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onAuthSuccess()
        }
    }

    // Sync Loading / Error / Success state into Auth HTML
    LaunchedEffect(uiState.isAuthLoading, uiState.authError, uiState.authSuccess) {
        val webView = webViewRef ?: return@LaunchedEffect
        val stateObj = JSONObject().apply {
            put("isLoading", uiState.isAuthLoading)
            put("error", uiState.authError ?: "")
            put("success", uiState.authSuccess ?: "")
        }
        val script = "if (window.updateAuthState) { window.updateAuthState($stateObj); }"
        webView.evaluateJavascript(script, null)
    }

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
                    }

                    addJavascriptInterface(AuthWebInterface(
                        context = ctx,
                        viewModel = viewModel
                    ), "AndroidAuth")

                    loadDataWithBaseURL(null, getAuthScreenHtml(), "text/html", "UTF-8", null)
                }
            },
            update = {
                webViewRef = it
            }
        )
    }
}

class AuthWebInterface(
    private val context: Context,
    private val viewModel: MainViewModel
) {
    @JavascriptInterface
    fun doLogin(email: String, pass: String) {
        Handler(Looper.getMainLooper()).post {
            viewModel.clearAuthMessages()
            viewModel.logInUser(email.trim(), pass, onComplete = { _, _ -> })
        }
    }

    @JavascriptInterface
    fun doSignUp(first: String, last: String, tg: String, email: String, pass: String) {
        Handler(Looper.getMainLooper()).post {
            viewModel.clearAuthMessages()
            viewModel.signUpUser(
                firstName = first.trim(),
                lastName = last.trim(),
                telegramUsername = tg.trim(),
                email = email.trim(),
                password = pass,
                onComplete = { _, _ -> }
            )
        }
    }

    @JavascriptInterface
    fun clearMessages() {
        Handler(Looper.getMainLooper()).post {
            viewModel.clearAuthMessages()
        }
    }
}

private fun getAuthScreenHtml(): String {
    return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>FB TOOLS Authentication</title>
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
      background: linear-gradient(135deg, #0b1120 0%, #0f172a 50%, #1e1b4b 100%);
      color: #f8fafc;
      font-size: 13px;
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 16px;
      overflow-x: hidden;
    }
    
    .auth-container {
      width: 100%;
      max-width: 420px;
      background: #111827;
      border: 1px solid #1f2937;
      border-radius: 24px;
      padding: 24px 20px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.6);
      animation: fadeIn 0.2s ease-out;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: scale(0.98); }
      to { opacity: 1; transform: scale(1); }
    }

    /* Logo & Header */
    .header-box {
      text-align: center;
      margin-bottom: 20px;
    }
    .logo-badge {
      width: 54px;
      height: 54px;
      background: #1877f2;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 10px auto;
      box-shadow: 0 4px 16px rgba(24, 119, 242, 0.4);
    }
    .logo-f {
      font-size: 36px;
      font-weight: 900;
      color: white;
      line-height: 1;
      font-family: sans-serif;
      margin-top: 4px;
    }
    .brand-title {
      font-size: 22px;
      font-weight: 900;
      letter-spacing: 0.5px;
    }
    .brand-fb { color: #f8fafc; }
    .brand-tool { color: #38bdf8; }
    .brand-sub {
      font-size: 11px;
      color: #94a3b8;
      margin-top: 2px;
      font-weight: 500;
    }

    /* Tab Switcher */
    .tab-pill {
      display: flex;
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 30px;
      padding: 3px;
      margin-bottom: 20px;
    }
    .tab-btn {
      flex: 1;
      text-align: center;
      padding: 9px 0;
      font-size: 13px;
      font-weight: 700;
      color: #94a3b8;
      border-radius: 25px;
      border: none;
      background: transparent;
      cursor: pointer;
      transition: all 0.15s;
    }
    .tab-btn.active {
      background: #1877f2;
      color: #ffffff;
      box-shadow: 0 2px 8px rgba(24, 119, 242, 0.35);
    }

    /* Form Fields */
    .form-pane {
      display: none;
    }
    .form-pane.active {
      display: block;
    }
    .form-group {
      margin-bottom: 14px;
    }
    .form-label {
      display: block;
      font-size: 11px;
      font-weight: 700;
      color: #cbd5e1;
      margin-bottom: 5px;
    }
    .input-wrapper {
      position: relative;
      display: flex;
      align-items: center;
    }
    .form-control {
      width: 100%;
      background: #1e293b;
      border: 1px solid #334155;
      color: #ffffff;
      padding: 11px 14px;
      border-radius: 10px;
      font-size: 13px;
      outline: none;
      transition: border-color 0.15s;
    }
    .form-control:focus {
      border-color: #38bdf8;
      box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.2);
    }
    .pwd-toggle {
      position: absolute;
      right: 12px;
      cursor: pointer;
      color: #94a3b8;
      font-size: 13px;
    }

    /* Action Button */
    .btn-submit {
      width: 100%;
      background: #1877f2;
      color: white;
      border: none;
      padding: 13px;
      border-radius: 12px;
      font-size: 14px;
      font-weight: 800;
      cursor: pointer;
      margin-top: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      box-shadow: 0 4px 12px rgba(24, 119, 242, 0.3);
    }
    .btn-submit:active {
      transform: scale(0.98);
    }
    .btn-submit:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    /* Alert Banner */
    .alert-box {
      padding: 10px 12px;
      border-radius: 10px;
      margin-bottom: 14px;
      font-size: 11px;
      font-weight: 700;
      display: none;
    }
    .alert-error {
      background: #450a0a;
      border: 1px solid #991b1b;
      color: #fca5a5;
    }
    .alert-success {
      background: #052e16;
      border: 1px solid #166534;
      color: #86efac;
    }

    .footer-link {
      text-align: center;
      margin-top: 16px;
      font-size: 11px;
      color: #94a3b8;
    }
    .footer-link span {
      color: #38bdf8;
      font-weight: 700;
      cursor: pointer;
    }
  </style>
</head>
<body>

  <div class="auth-container">
    <div class="header-box">
      <div class="logo-badge">
        <div class="logo-f">f</div>
      </div>
      <div class="brand-title">
        <span class="brand-fb">FB</span> <span class="brand-tool">TOOLS</span>
      </div>
      <div class="brand-sub">Automation & Account Suite</div>
    </div>

    <!-- Alert Notices -->
    <div id="alertBox" class="alert-box"></div>

    <!-- Pill Tabs -->
    <div class="tab-pill">
      <button class="tab-btn active" id="tabLoginBtn" onclick="switchAuthTab(0)">🔒 Login</button>
      <button class="tab-btn" id="tabSignupBtn" onclick="switchAuthTab(1)">📝 Sign Up</button>
    </div>

    <!-- LOGIN FORM -->
    <div id="loginPane" class="form-pane active">
      <div class="form-group">
        <label class="form-label">Email / Username</label>
        <input type="email" id="loginEmail" class="form-control" placeholder="user@gmail.com">
      </div>
      <div class="form-group">
        <label class="form-label">Password</label>
        <div class="input-wrapper">
          <input type="password" id="loginPassword" class="form-control" placeholder="Enter password">
          <span class="pwd-toggle" onclick="togglePassword('loginPassword', this)">👁️</span>
        </div>
      </div>
      <button class="btn-submit" id="loginSubmitBtn" onclick="submitLogin()">
        🚀 LOGIN TO DASHBOARD
      </button>
      <div class="footer-link">
        Don't have an account? <span onclick="switchAuthTab(1)">Sign up here</span>
      </div>
    </div>

    <!-- SIGN UP FORM -->
    <div id="signupPane" class="form-pane">
      <div style="display: flex; gap: 8px;">
        <div class="form-group" style="flex: 1;">
          <label class="form-label">First Name</label>
          <input type="text" id="signFirst" class="form-control" placeholder="First">
        </div>
        <div class="form-group" style="flex: 1;">
          <label class="form-label">Last Name</label>
          <input type="text" id="signLast" class="form-control" placeholder="Last">
        </div>
      </div>
      <div class="form-group">
        <label class="form-label">Telegram Username (@)</label>
        <input type="text" id="signTg" class="form-control" placeholder="@username">
      </div>
      <div class="form-group">
        <label class="form-label">Email Address</label>
        <input type="email" id="signEmail" class="form-control" placeholder="name@example.com">
      </div>
      <div class="form-group">
        <label class="form-label">Create Password</label>
        <div class="input-wrapper">
          <input type="password" id="signPassword" class="form-control" placeholder="At least 6 characters">
          <span class="pwd-toggle" onclick="togglePassword('signPassword', this)">👁️</span>
        </div>
      </div>
      <button class="btn-submit" id="signupSubmitBtn" onclick="submitSignUp()">
        ✨ CREATE ACCOUNT
      </button>
      <div class="footer-link">
        Already registered? <span onclick="switchAuthTab(0)">Log in here</span>
      </div>
    </div>

  </div>

  <script>
    var currentTab = 0;

    function switchAuthTab(index) {
      currentTab = index;
      document.getElementById('loginPane').classList.toggle('active', index === 0);
      document.getElementById('signupPane').classList.toggle('active', index === 1);
      document.getElementById('tabLoginBtn').classList.toggle('active', index === 0);
      document.getElementById('tabSignupBtn').classList.toggle('active', index === 1);
      hideAlert();
      AndroidAuth.clearMessages();
    }

    function togglePassword(inputId, elem) {
      var inp = document.getElementById(inputId);
      if (inp.type === 'password') {
        inp.type = 'text';
        elem.innerText = '🔒';
      } else {
        inp.type = 'password';
        elem.innerText = '👁️';
      }
    }

    function submitLogin() {
      var email = document.getElementById('loginEmail').value.trim();
      var pass = document.getElementById('loginPassword').value;
      if (!email || !pass) {
        showAlert('Please enter both email and password', 'error');
        return;
      }
      document.getElementById('loginSubmitBtn').disabled = true;
      document.getElementById('loginSubmitBtn').innerText = 'Logging in... ⏳';
      AndroidAuth.doLogin(email, pass);
    }

    function submitSignUp() {
      var first = document.getElementById('signFirst').value.trim();
      var last = document.getElementById('signLast').value.trim();
      var tg = document.getElementById('signTg').value.trim();
      var email = document.getElementById('signEmail').value.trim();
      var pass = document.getElementById('signPassword').value;

      if (!email || !pass) {
        showAlert('Email and Password are required!', 'error');
        return;
      }
      if (pass.length < 6) {
        showAlert('Password must be at least 6 characters!', 'error');
        return;
      }

      document.getElementById('signupSubmitBtn').disabled = true;
      document.getElementById('signupSubmitBtn').innerText = 'Creating account... ⏳';
      AndroidAuth.doSignUp(first, last, tg, email, pass);
    }

    function showAlert(text, type) {
      var box = document.getElementById('alertBox');
      box.innerText = text;
      box.className = 'alert-box ' + (type === 'error' ? 'alert-error' : 'alert-success');
      box.style.display = 'block';
    }

    function hideAlert() {
      var box = document.getElementById('alertBox');
      box.style.display = 'none';
    }

    // Called from Kotlin on state updates
    window.updateAuthState = function(state) {
      var logBtn = document.getElementById('loginSubmitBtn');
      var signBtn = document.getElementById('signupSubmitBtn');

      if (state.isLoading) {
        logBtn.disabled = true;
        signBtn.disabled = true;
      } else {
        logBtn.disabled = false;
        logBtn.innerText = '🚀 LOGIN TO DASHBOARD';
        signBtn.disabled = false;
        signBtn.innerText = '✨ CREATE ACCOUNT';
      }

      if (state.error) {
        showAlert('❌ ' + state.error, 'error');
      } else if (state.success) {
        showAlert('✅ ' + state.success, 'success');
        if (currentTab === 1) {
          setTimeout(function() {
            switchAuthTab(0);
          }, 1200);
        }
      }
    };
  </script>
</body>
</html>
    """.trimIndent()
}
