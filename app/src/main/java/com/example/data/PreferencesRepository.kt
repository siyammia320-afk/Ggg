package com.example.data

import android.content.Context

class PreferencesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("fb_creator_prefs", Context.MODE_PRIVATE)

    var savedPassword: String
        get() = prefs.getString("saved_password", "") ?: ""
        set(value) = prefs.edit().putString("saved_password", value).apply()

    var isTelegramJoined: Boolean
        get() = prefs.getBoolean("is_telegram_joined", false)
        set(value) = prefs.edit().putBoolean("is_telegram_joined", value).apply()

    var selectedCountryCode: String
        get() = prefs.getString("selected_country_code", "BD") ?: "BD"
        set(value) = prefs.edit().putString("selected_country_code", value).apply()

    var proxyServer: String
        get() = prefs.getString("proxy_server", "") ?: ""
        set(value) = prefs.edit().putString("proxy_server", value).apply()

    var proxyPort: String
        get() = prefs.getString("proxy_port", "") ?: ""
        set(value) = prefs.edit().putString("proxy_port", value).apply()

    var proxyUsername: String
        get() = prefs.getString("proxy_username", "") ?: ""
        set(value) = prefs.edit().putString("proxy_username", value).apply()

    var proxyPassword: String
        get() = prefs.getString("proxy_password", "") ?: ""
        set(value) = prefs.edit().putString("proxy_password", value).apply()

    var isProxyEnabled: Boolean
        get() = prefs.getBoolean("is_proxy_enabled", true)
        set(value) = prefs.edit().putBoolean("is_proxy_enabled", value).apply()

    var isCustomUserAgentEnabled: Boolean
        get() = prefs.getBoolean("is_custom_user_agent_enabled", false)
        set(value) = prefs.edit().putBoolean("is_custom_user_agent_enabled", value).apply()

    var customUserAgent: String
        get() = prefs.getString("custom_user_agent", "") ?: ""
        set(value) = prefs.edit().putString("custom_user_agent", value).apply()

    var activeNumbersJson: String
        get() = prefs.getString("active_numbers_json", "") ?: ""
        set(value) = prefs.edit().putString("active_numbers_json", value).apply()

    var sheetPassword: String
        get() = prefs.getString("sheet_password", "") ?: ""
        set(value) = prefs.edit().putString("sheet_password", value).apply()

    var voltxApiKey: String
        get() = prefs.getString("voltx_api_key", "MFSCNKJSFBI") ?: "MFSCNKJSFBI"
        set(value) = prefs.edit().putString("voltx_api_key", value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var loggedInEmail: String
        get() = prefs.getString("logged_in_email", "") ?: ""
        set(value) = prefs.edit().putString("logged_in_email", value).apply()

    var loggedInFirstName: String
        get() = prefs.getString("logged_in_first_name", "") ?: ""
        set(value) = prefs.edit().putString("logged_in_first_name", value).apply()

    var loggedInLastName: String
        get() = prefs.getString("logged_in_last_name", "") ?: ""
        set(value) = prefs.edit().putString("logged_in_last_name", value).apply()

    var loggedInTelegram: String
        get() = prefs.getString("logged_in_telegram", "") ?: ""
        set(value) = prefs.edit().putString("logged_in_telegram", value).apply()

    var userBalance: Float
        get() = prefs.getFloat("user_balance", 0.0f)
        set(value) = prefs.edit().putFloat("user_balance", value).apply()
}
