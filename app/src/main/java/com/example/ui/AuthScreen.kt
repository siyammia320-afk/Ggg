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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
            .background(Color(0xFFDCE9F0))
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
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
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    overScrollMode = android.view.View.OVER_SCROLL_NEVER
                    setBackgroundColor(0xFFDCE9F0.toInt())

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
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=yes">
      <title>FB TOOLS · Login / Sign Up</title>
      <!-- Font Awesome 6 (free) -->
      <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
      <style>
        * {
          margin: 0;
          padding: 0;
          box-sizing: border-box;
          font-family: system-ui, -apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif;
          -webkit-tap-highlight-color: transparent;
        }

        html, body {
          height: 100%;
          min-height: 100%;
          width: 100%;
        }

        body {
          min-height: 100vh;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #dce9f0;
          padding: 16px;
          overflow-y: auto;
        }

        .card {
          width: 100%;
          max-width: 420px;
          background: #f0f7fc;
          border-radius: 40px;
          box-shadow: 0 10px 25px rgba(0, 0, 0, 0.03);
          padding: 24px 20px 28px;
          border: 1px solid #c5dae6;
          margin: auto;
        }

        /* Header with FB TOOLS & logo */
        .app-header {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 12px;
          margin-bottom: 22px;
          padding-bottom: 14px;
          border-bottom: 2px solid #d4e3ed;
        }

        .app-header i {
          font-size: 2rem;
          color: #1877f2;
          background: white;
          padding: 8px;
          border-radius: 50%;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
        }

        .app-header h1 {
          font-size: 1.6rem;
          font-weight: 700;
          color: #1a3f52;
          letter-spacing: -0.5px;
        }

        .app-header h1 span {
          color: #1877f2;
        }

        /* Status Banners */
        .alert-banner {
          padding: 10px 14px;
          border-radius: 16px;
          font-size: 0.85rem;
          font-weight: 600;
          margin-bottom: 16px;
          display: none;
          line-height: 1.3;
          text-align: center;
        }
        .alert-banner.error {
          background: #fee2e2;
          color: #991b1b;
          border: 1px solid #f87171;
        }
        .alert-banner.success {
          background: #dcfce7;
          color: #166534;
          border: 1px solid #4ade80;
        }

        .tabs {
          display: flex;
          gap: 10px;
          margin-bottom: 24px;
          background: #d4e3ed;
          padding: 5px;
          border-radius: 50px;
        }

        .tab-btn {
          flex: 1;
          border: none;
          background: transparent;
          padding: 12px 0;
          font-size: 1rem;
          font-weight: 600;
          border-radius: 40px;
          color: #2d4d5e;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          transition: all 0.15s;
          box-shadow: none;
        }

        .tab-btn i {
          font-size: 1rem;
          opacity: 0.6;
        }

        .tab-btn.active {
          background: #ffffff;
          color: #1a3f52;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.01);
        }

        .tab-btn:active {
          transform: scale(0.97);
        }

        .form-container {
          margin-top: 4px;
        }

        .form {
          display: block;
          width: 100%;
        }

        .form.hidden {
          display: none;
        }

        .input-group {
          margin-bottom: 16px;
          position: relative;
        }

        .input-group label {
          display: block;
          font-size: 0.85rem;
          font-weight: 500;
          color: #1d4052;
          margin-bottom: 5px;
          padding-left: 4px;
        }

        .field-wrap {
          display: flex;
          align-items: center;
          background: #ffffff;
          border-radius: 30px;
          border: 1px solid #bed3e0;
          padding: 2px 16px;
          transition: all 0.1s;
        }

        .field-wrap:focus-within {
          border-color: #6f98ae;
          background: #ffffff;
        }

        .field-wrap i {
          color: #5d849b;
          font-size: 0.95rem;
          width: 20px;
          text-align: center;
          opacity: 0.5;
        }

        .field-wrap input {
          width: 100%;
          border: none;
          background: transparent;
          padding: 14px 8px 14px 10px;
          font-size: 0.95rem;
          color: #1a3342;
          outline: none;
        }

        .field-wrap input::placeholder {
          color: #97b3c6;
          font-weight: 300;
          font-size: 0.9rem;
        }

        .name-row {
          display: flex;
          gap: 10px;
        }

        .name-row .input-group {
          flex: 1;
        }

        .btn-submit {
          width: 100%;
          border: none;
          background: #1877f2;
          padding: 16px 10px;
          border-radius: 60px;
          color: white;
          font-size: 1rem;
          font-weight: 600;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 10px;
          cursor: pointer;
          transition: all 0.15s;
          margin-top: 10px;
          border: 1px solid #1b7ef5;
        }

        .btn-submit i {
          font-size: 0.95rem;
          opacity: 0.8;
        }

        .btn-submit:active {
          transform: scale(0.97);
          background: #1463cc;
        }

        .btn-submit:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        .spinner {
          width: 16px;
          height: 16px;
          border: 2px solid #ffffff;
          border-top-color: transparent;
          border-radius: 50%;
          display: inline-block;
          animation: spin 0.6s linear infinite;
        }
        @keyframes spin {
          to { transform: rotate(360deg); }
        }

        .extra-links {
          margin-top: 20px;
          display: flex;
          justify-content: center;
          gap: 4px;
          font-size: 0.9rem;
          color: #2d4d5e;
        }

        .extra-links span {
          opacity: 0.6;
        }

        .toggle-link {
          background: none;
          border: none;
          color: #1877f2;
          font-weight: 600;
          cursor: pointer;
          padding: 0 4px;
          font-size: 0.9rem;
          border-bottom: 1px dashed transparent;
        }

        .toggle-link:active {
          opacity: 0.6;
          transform: scale(0.96);
        }

        @media (max-width: 480px) {
          .card {
            padding: 20px 16px 24px;
            border-radius: 32px;
          }
          .app-header h1 {
            font-size: 1.3rem;
          }
          .app-header i {
            font-size: 1.6rem;
            padding: 6px;
          }
          .name-row {
            flex-direction: column;
            gap: 0;
          }
          .tab-btn {
            font-size: 0.9rem;
            padding: 10px 0;
          }
          .field-wrap input {
            padding: 12px 6px 12px 8px;
            font-size: 0.9rem;
          }
          .btn-submit {
            padding: 14px 10px;
            font-size: 0.95rem;
          }
        }

        .field-wrap:focus-within,
        .btn-submit:focus,
        .tab-btn:focus {
          outline: none;
          box-shadow: none;
        }

        .field-wrap input:-webkit-autofill {
          -webkit-box-shadow: 0 0 0 1000px #f0f7fc inset !important;
          -webkit-text-fill-color: #1a3342;
        }
      </style>
    </head>
    <body>

    <div class="card">
      <!-- Header with FB TOOLS & Facebook logo -->
      <div class="app-header">
        <i class="fab fa-facebook"></i>
        <h1>FB <span>TOOLS</span></h1>
      </div>

      <!-- Status Alerts -->
      <div id="errorBanner" class="alert-banner error"></div>
      <div id="successBanner" class="alert-banner success"></div>

      <div class="tabs" role="tablist">
        <button class="tab-btn active" data-tab="login" id="tabLogin" role="tab" aria-selected="true">
          <i class="fas fa-arrow-right-to-bracket"></i> Login
        </button>
        <button class="tab-btn" data-tab="signup" id="tabSignup" role="tab" aria-selected="false">
          <i class="fas fa-user-plus"></i> Sign Up
        </button>
      </div>

      <div class="form-container">
        <!-- Login Form -->
        <form id="loginForm" class="form" autocomplete="on">
          <div class="input-group">
            <label><i class="far fa-envelope" style="margin-right: 4px;"></i> Email</label>
            <div class="field-wrap">
              <i class="far fa-envelope"></i>
              <input type="email" id="loginEmail" placeholder="you@example.com" required autocomplete="email">
            </div>
          </div>
          <div class="input-group">
            <label><i class="fas fa-lock" style="margin-right: 4px;"></i> Password</label>
            <div class="field-wrap">
              <i class="fas fa-lock"></i>
              <input type="password" id="loginPassword" placeholder="" required autocomplete="current-password">
            </div>
          </div>
          <button type="submit" class="btn-submit" id="btnLoginSubmit">
            <span id="loginSpinner" class="spinner" style="display: none;"></span>
            <i class="fas fa-arrow-right-to-bracket" id="loginIcon"></i>
            <span id="loginBtnText">Log In</span>
          </button>
          <div class="extra-links">
            <span>New user?</span>
            <button type="button" class="toggle-link" id="switchToSignup">Sign up</button>
          </div>
        </form>

        <!-- Sign Up Form -->
        <form id="signupForm" class="form hidden" autocomplete="on">
          <div class="name-row">
            <div class="input-group">
              <label>First name</label>
              <div class="field-wrap">
                <i class="far fa-user"></i>
                <input type="text" id="firstName" placeholder="" required autocomplete="given-name">
              </div>
            </div>
            <div class="input-group">
              <label>Last name</label>
              <div class="field-wrap">
                <i class="far fa-user"></i>
                <input type="text" id="lastName" placeholder="" autocomplete="family-name">
              </div>
            </div>
          </div>

          <div class="input-group">
            <label><i class="fab fa-telegram-plane" style="margin-right: 4px;"></i> Telegram username</label>
            <div class="field-wrap">
              <i class="fab fa-telegram-plane"></i>
              <input type="text" id="telegramUser" placeholder="@username" required>
            </div>
          </div>

          <div class="input-group">
            <label><i class="far fa-envelope" style="margin-right: 4px;"></i> Email</label>
            <div class="field-wrap">
              <i class="far fa-envelope"></i>
              <input type="email" id="signupEmail" placeholder="you@example.com" required autocomplete="email">
            </div>
          </div>

          <div class="input-group">
            <label><i class="fas fa-lock" style="margin-right: 4px;"></i> Password</label>
            <div class="field-wrap">
              <i class="fas fa-lock"></i>
              <input type="password" id="signupPassword" placeholder="" required autocomplete="new-password">
            </div>
          </div>

          <button type="submit" class="btn-submit" id="btnSignupSubmit">
            <span id="signupSpinner" class="spinner" style="display: none;"></span>
            <i class="fas fa-user-plus" id="signupIcon"></i>
            <span id="signupBtnText">Sign Up</span>
          </button>
          <div class="extra-links">
            <span>Already have an account?</span>
            <button type="button" class="toggle-link" id="switchToLogin">Log in</button>
          </div>
        </form>
      </div>
    </div>

    <script>
      (function() {
        const loginTab = document.getElementById('tabLogin');
        const signupTab = document.getElementById('tabSignup');
        const loginForm = document.getElementById('loginForm');
        const signupForm = document.getElementById('signupForm');
        const switchToSignup = document.getElementById('switchToSignup');
        const switchToLoginBtn = document.getElementById('switchToLogin');
        const errorBanner = document.getElementById('errorBanner');
        const successBanner = document.getElementById('successBanner');
        let isCurrentLoading = false;

        function hideAlerts() {
          if (errorBanner) errorBanner.style.display = 'none';
          if (successBanner) successBanner.style.display = 'none';
        }

        function setActiveTab(tab) {
          hideAlerts();
          if (window.AndroidBridge && window.AndroidBridge.clearMessages) {
            window.AndroidBridge.clearMessages();
          }
          if (tab === 'login') {
            loginTab.classList.add('active');
            loginTab.setAttribute('aria-selected', 'true');
            signupTab.classList.remove('active');
            signupTab.setAttribute('aria-selected', 'false');
            loginForm.classList.remove('hidden');
            signupForm.classList.add('hidden');
          } else {
            signupTab.classList.add('active');
            signupTab.setAttribute('aria-selected', 'true');
            loginTab.classList.remove('active');
            loginTab.setAttribute('aria-selected', 'false');
            signupForm.classList.remove('hidden');
            loginForm.classList.add('hidden');
          }
        }

        window.switchToLogin = function() {
          setActiveTab('login');
        };

        loginTab.addEventListener('click', (e) => { e.preventDefault(); setActiveTab('login'); });
        signupTab.addEventListener('click', (e) => { e.preventDefault(); setActiveTab('signup'); });
        switchToSignup.addEventListener('click', (e) => { e.preventDefault(); setActiveTab('signup'); });
        switchToLoginBtn.addEventListener('click', (e) => { e.preventDefault(); setActiveTab('login'); });

        loginForm.addEventListener('submit', (e) => {
          e.preventDefault();
          if (isCurrentLoading) return;
          const email = document.getElementById('loginEmail').value.trim();
          const pass = document.getElementById('loginPassword').value.trim();
          if (!email || !pass) {
            showError('Please enter email and password.');
            return;
          }
          hideAlerts();
          if (window.AndroidBridge && window.AndroidBridge.performLogin) {
            window.AndroidBridge.performLogin(email, pass);
          }
        });

        signupForm.addEventListener('submit', (e) => {
          e.preventDefault();
          if (isCurrentLoading) return;
          const firstName = document.getElementById('firstName').value.trim();
          const lastName = document.getElementById('lastName').value.trim();
          const telegram = document.getElementById('telegramUser').value.trim();
          const email = document.getElementById('signupEmail').value.trim();
          const password = document.getElementById('signupPassword').value.trim();

          if (!firstName || !email || !password || !telegram) {
            showError('Please fill in first name, Telegram, email and password.');
            return;
          }
          if (password.length < 6) {
            showError('Password must be at least 6 characters.');
            return;
          }
          hideAlerts();
          if (window.AndroidBridge && window.AndroidBridge.performSignup) {
            window.AndroidBridge.performSignup(firstName, lastName, telegram, email, password);
          }
        });

        function showError(msg) {
          if (errorBanner) {
            errorBanner.innerText = '❌ ' + msg;
            errorBanner.style.display = 'block';
          }
          if (successBanner) {
            successBanner.style.display = 'none';
          }
        }

        window.handleKotlinState = function(loading, error, success) {
          isCurrentLoading = loading;
          const btnLogin = document.getElementById('btnLoginSubmit');
          const btnSignup = document.getElementById('btnSignupSubmit');
          const loginSpinner = document.getElementById('loginSpinner');
          const signupSpinner = document.getElementById('signupSpinner');
          const loginIcon = document.getElementById('loginIcon');
          const signupIcon = document.getElementById('signupIcon');

          if (btnLogin) btnLogin.disabled = loading;
          if (btnSignup) btnSignup.disabled = loading;

          if (loginSpinner) loginSpinner.style.display = loading ? 'inline-block' : 'none';
          if (signupSpinner) signupSpinner.style.display = loading ? 'inline-block' : 'none';
          if (loginIcon) loginIcon.style.display = loading ? 'none' : 'inline-block';
          if (signupIcon) signupIcon.style.display = loading ? 'none' : 'inline-block';

          if (error && error.length > 0) {
            if (errorBanner) {
              errorBanner.innerText = '❌ ' + error;
              errorBanner.style.display = 'block';
            }
          } else {
            if (errorBanner) errorBanner.style.display = 'none';
          }

          if (success && success.length > 0) {
            if (successBanner) {
              successBanner.innerText = '✅ ' + success;
              successBanner.style.display = 'block';
            }
          } else {
            if (successBanner) successBanner.style.display = 'none';
          }
        };
      })();
    </script>

    </body>
    </html>
    """.trimIndent()
}

