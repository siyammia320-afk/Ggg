package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

object AppConfigService {

    private const val FIREBASE_DB_URL = "https://fb-virul-tools-default-rtdb.firebaseio.com"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    data class AppStatusResult(
        val isAppOn: Boolean = true,
        val message: String = "App is running"
    )

    suspend fun checkAppStatus(): AppStatusResult = withContext(Dispatchers.IO) {
        val configUrl = "$FIREBASE_DB_URL/app_config.json"
        try {
            val request = Request.Builder()
                .url(configUrl)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // If network response error, default to ON so app is accessible
                    return@withContext AppStatusResult(isAppOn = true, message = "App Active")
                }
                val bodyString = response.body?.string()?.trim() ?: ""
                if (bodyString.isEmpty() || bodyString == "null") {
                    return@withContext AppStatusResult(isAppOn = true, message = "App Active")
                }

                val json = JSONObject(bodyString)
                val statusStr = json.optString("status", "on").lowercase(Locale.ROOT)
                val isAppOnBool = json.optBoolean("isAppOn", statusStr != "off" && statusStr != "disabled" && statusStr != "maintenance")
                val notice = json.optString("notice", "অ্যাপ বর্তমানে এডমিন দ্বারা বন্ধ রাখা হয়েছে। অনুগ্রহ করে পরবর্তীতে চেষ্টা করুন।")

                val finalAppOn = if (statusStr == "off" || statusStr == "maintenance" || statusStr == "disabled") false else isAppOnBool

                return@withContext AppStatusResult(
                    isAppOn = finalAppOn,
                    message = if (finalAppOn) "App Active" else notice
                )
            }
        } catch (e: Exception) {
            // Default to ON in case of offline or minor connection hiccups
            return@withContext AppStatusResult(isAppOn = true, message = "App Active")
        }
    }
}
