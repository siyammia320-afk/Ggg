package com.example.network

import com.example.network.AuthService.sanitizeEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WalletService {
    private const val FIREBASE_DB_URL = "https://fb-virul-tools-default-rtdb.firebaseio.com"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    data class Withdrawal(
        val id: String = "",
        val email: String = "",
        val name: String = "",
        val method: String = "",
        val value: String = "",
        val amount: Double = 0.0,
        val status: String = "pending",
        val timestamp: Long = 0L
    )

    suspend fun fetchOtpPrice(): Double = withContext(Dispatchers.IO) {
        val url = "$FIREBASE_DB_URL/otp_price.json"
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()?.trim() ?: ""
                    if (body.isNotEmpty() && body != "null") {
                        return@withContext body.toDoubleOrNull() ?: 0.5
                    }
                }
            }
        } catch (_: Exception) {}
        return@withContext 0.5 // default price
    }

    suspend fun fetchUserBalance(email: String): Double = withContext(Dispatchers.IO) {
        if (email.isEmpty()) return@withContext 0.0
        val sanitized = sanitizeEmail(email)
        val url = "$FIREBASE_DB_URL/users/$sanitized/balance.json"
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()?.trim() ?: ""
                    if (body.isNotEmpty() && body != "null") {
                        return@withContext body.toDoubleOrNull() ?: 0.0
                    }
                }
            }
        } catch (_: Exception) {}
        return@withContext 0.0
    }

    suspend fun updateUserBalance(email: String, newBalance: Double): Boolean = withContext(Dispatchers.IO) {
        if (email.isEmpty()) return@withContext false
        val sanitized = sanitizeEmail(email)
        val url = "$FIREBASE_DB_URL/users/$sanitized/balance.json"
        try {
            val request = Request.Builder()
                .url(url)
                .put(newBalance.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (_: Exception) {
            return@withContext false
        }
    }

    suspend fun requestWithdrawal(
        email: String,
        name: String,
        method: String,
        value: String,
        amount: Double
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (email.isEmpty()) return@withContext Pair(false, "অ্যাকাউন্ট ভ্যালিডেশন ব্যর্থ হয়েছে!")
        if (amount < 20.0) return@withContext Pair(false, "নূন্যতম উইথড্রল ২০ টাকা হতে হবে!")

        val currentBalance = fetchUserBalance(email)
        if (currentBalance < amount) {
            return@withContext Pair(false, "আপনার ব্যালেন্স অপর্যাপ্ত!")
        }

        val id = System.currentTimeMillis().toString()
        val url = "$FIREBASE_DB_URL/withdrawals/$id.json"

        try {
            val withdrawalObj = JSONObject().apply {
                put("id", id)
                put("email", email)
                put("name", name)
                put("method", method)
                put("value", value)
                put("amount", amount)
                put("status", "pending")
                put("timestamp", System.currentTimeMillis())
            }

            val request = Request.Builder()
                .url(url)
                .put(withdrawalObj.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    // Deduct balance from user
                    val updatedBalance = currentBalance - amount
                    updateUserBalance(email, updatedBalance)
                    return@withContext Pair(true, "উইথড্রল রিকোয়েস্ট সফলভাবে পাঠানো হয়েছে এবং পেন্ডিং আছে!")
                }
            }
        } catch (e: Exception) {
            return@withContext Pair(false, "উইথড্রল রিকোয়েস্ট পাঠানো ব্যর্থ হয়েছে: ${e.message}")
        }
        return@withContext Pair(false, "সার্ভার রেসপন্স এরর!")
    }

    suspend fun fetchUserWithdrawals(email: String): List<Withdrawal> = withContext(Dispatchers.IO) {
        val url = "$FIREBASE_DB_URL/withdrawals.json"
        val list = mutableListOf<Withdrawal>()
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()?.trim() ?: ""
                    if (body.isNotEmpty() && body != "null") {
                        val rootJson = JSONObject(body)
                        val keys = rootJson.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = rootJson.getJSONObject(key)
                            val requestEmail = obj.optString("email", "")
                            if (requestEmail.lowercase() == email.lowercase()) {
                                list.add(
                                    Withdrawal(
                                        id = obj.optString("id", key),
                                        email = requestEmail,
                                        name = obj.optString("name", ""),
                                        method = obj.optString("method", ""),
                                        value = obj.optString("value", ""),
                                        amount = obj.optDouble("amount", 0.0),
                                        status = obj.optString("status", "pending"),
                                        timestamp = obj.optLong("timestamp", 0L)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return@withContext list.sortedByDescending { it.timestamp }
    }
}
