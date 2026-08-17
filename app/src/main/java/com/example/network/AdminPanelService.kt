package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AdminPanelService {

    private const val FIREBASE_DB_URL = "https://fb-virul-tools-default-rtdb.firebaseio.com"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    data class AppConfigData(
        val isAppOn: Boolean = true,
        val status: String = "on",
        val notice: String = "",
        val isTerminalEnabled: Boolean = true,
        val terminalStatus: String = "on",
        val terminalNotice: String = "",
        val isManualNumbersEnabled: Boolean = true,
        val manualNumberStatus: String = "on",
        val otpPrice: Double = 0.50
    )

    data class AdminUser(
        val email: String = "",
        val firstName: String = "",
        val lastName: String = "",
        val telegram: String = "",
        val balance: Double = 0.0,
        val isBlocked: Boolean = false
    )

    data class AdminWithdrawal(
        val id: String = "",
        val email: String = "",
        val name: String = "",
        val method: String = "",
        val value: String = "",
        val amount: Double = 0.0,
        val timestamp: Long = 0L,
        val status: String = "pending"
    )

    data class MasterKeyItem(
        val key: String = "",
        val status: String = "unused",
        val usedByApiKey: String = "",
        val usedAt: Long = 0L
    )

    suspend fun fetchAppConfig(): AppConfigData = withContext(Dispatchers.IO) {
        val url = "$FIREBASE_DB_URL/app_config.json"
        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext AppConfigData()
                val body = response.body?.string()?.trim() ?: ""
                if (body.isEmpty() || body == "null") return@withContext AppConfigData()

                val json = JSONObject(body)
                val statusStr = json.optString("status", "on")
                val isAppOnBool = json.optBoolean("isAppOn", statusStr != "off" && statusStr != "disabled" && statusStr != "maintenance")
                val notice = json.optString("notice", "")
                val terminalStatusStr = json.optString("terminal_status", "on")
                val isTerminalOn = json.optBoolean("isTerminalEnabled", json.optBoolean("terminal_enabled", terminalStatusStr != "off" && terminalStatusStr != "disabled"))
                val manualStatusStr = json.optString("manual_number_status", "on")
                val isManualOn = json.optBoolean("isManualNumbersEnabled", json.optBoolean("manual_numbers_enabled", manualStatusStr != "off" && manualStatusStr != "disabled"))
                val termNotice = json.optString("terminal_notice", "")

                // also fetch otp price
                val otpPrice = WalletService.fetchOtpPrice()

                return@withContext AppConfigData(
                    isAppOn = isAppOnBool,
                    status = statusStr,
                    notice = notice,
                    isTerminalEnabled = isTerminalOn,
                    terminalStatus = terminalStatusStr,
                    terminalNotice = termNotice,
                    isManualNumbersEnabled = isManualOn,
                    manualNumberStatus = manualStatusStr,
                    otpPrice = otpPrice
                )
            }
        } catch (e: Exception) {
            return@withContext AppConfigData()
        }
    }

    suspend fun saveAppConfig(
        isAppOn: Boolean,
        notice: String,
        isTerminalEnabled: Boolean,
        terminalNotice: String,
        isManualNumbersEnabled: Boolean,
        otpPrice: Double
    ): Boolean = withContext(Dispatchers.IO) {
        val url = "$FIREBASE_DB_URL/app_config.json"
        try {
            val payload = JSONObject().apply {
                put("status", if (isAppOn) "on" else "off")
                put("isAppOn", isAppOn)
                put("notice", notice)
                put("terminal_status", if (isTerminalEnabled) "on" else "off")
                put("isTerminalEnabled", isTerminalEnabled)
                put("terminal_enabled", isTerminalEnabled)
                put("terminal_notice", terminalNotice)
                put("manual_number_status", if (isManualNumbersEnabled) "on" else "off")
                put("isManualNumbersEnabled", isManualNumbersEnabled)
                put("manual_numbers_enabled", isManualNumbersEnabled)
                put("updated_at", System.currentTimeMillis())
            }

            val request = Request.Builder()
                .url(url)
                .patch(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val success = client.newCall(request).execute().use { it.isSuccessful }

            // also save OTP price
            saveOtpPrice(otpPrice)

            return@withContext success
        } catch (e: Exception) {
            return@withContext false
        }
    }

    suspend fun saveOtpPrice(price: Double): Boolean = withContext(Dispatchers.IO) {
        val url = "$FIREBASE_DB_URL/otp_price.json"
        try {
            val request = Request.Builder()
                .url(url)
                .put(price.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchAllUsers(): List<AdminUser> = withContext(Dispatchers.IO) {
        val url = "$FIREBASE_DB_URL/users.json"
        val list = mutableListOf<AdminUser>()
        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string()?.trim() ?: ""
                if (body.isEmpty() || body == "null") return@withContext emptyList()

                val json = JSONObject(body)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val userObj = json.optJSONObject(k) ?: continue
                    list.add(
                        AdminUser(
                            email = userObj.optString("email", k.replace("_", ".")),
                            firstName = userObj.optString("firstName", ""),
                            lastName = userObj.optString("lastName", ""),
                            telegram = userObj.optString("telegram", ""),
                            balance = userObj.optDouble("balance", 0.0),
                            isBlocked = userObj.optBoolean("isBlocked", false)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext list
    }

    suspend fun updateUserBalance(email: String, newBalance: Double): Boolean = withContext(Dispatchers.IO) {
        val sanitized = email.replace(".", "_")
        val url = "$FIREBASE_DB_URL/users/$sanitized/balance.json"
        try {
            val request = Request.Builder()
                .url(url)
                .put(newBalance.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleUserBlock(email: String, block: Boolean): Boolean = withContext(Dispatchers.IO) {
        val sanitized = email.replace(".", "_")
        val url = "$FIREBASE_DB_URL/users/$sanitized/isBlocked.json"
        try {
            val request = Request.Builder()
                .url(url)
                .put(block.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchAllWithdrawals(): List<AdminWithdrawal> = withContext(Dispatchers.IO) {
        val url = "$FIREBASE_DB_URL/withdrawals.json"
        val list = mutableListOf<AdminWithdrawal>()
        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string()?.trim() ?: ""
                if (body.isEmpty() || body == "null") return@withContext emptyList()

                val json = JSONObject(body)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val obj = json.optJSONObject(k) ?: continue
                    list.add(
                        AdminWithdrawal(
                            id = obj.optString("id", k),
                            email = obj.optString("email", ""),
                            name = obj.optString("name", ""),
                            method = obj.optString("method", ""),
                            value = obj.optString("value", ""),
                            amount = obj.optDouble("amount", 0.0),
                            timestamp = obj.optLong("timestamp", 0L),
                            status = obj.optString("status", "pending")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list.sortByDescending { it.timestamp }
        return@withContext list
    }

    suspend fun updateWithdrawalStatus(id: String, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        val url = "$FIREBASE_DB_URL/withdrawals/$id/status.json"
        try {
            val request = Request.Builder()
                .url(url)
                .put("\"$newStatus\"".toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchMasterKeys(): List<MasterKeyItem> = withContext(Dispatchers.IO) {
        val url = "$FIREBASE_DB_URL/master_keys.json"
        val list = mutableListOf<MasterKeyItem>()
        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string()?.trim() ?: ""
                if (body.isEmpty() || body == "null") return@withContext emptyList()

                val json = JSONObject(body)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val obj = json.optJSONObject(k)
                    if (obj != null) {
                        list.add(
                            MasterKeyItem(
                                key = k,
                                status = obj.optString("status", "unused"),
                                usedByApiKey = obj.optString("used_by_api_key", ""),
                                usedAt = obj.optLong("used_at", 0L)
                            )
                        )
                    } else {
                        list.add(MasterKeyItem(key = k, status = "unused"))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext list
    }

    suspend fun addMasterKey(key: String): Boolean = withContext(Dispatchers.IO) {
        val sanitized = key.trim()
        if (sanitized.isEmpty()) return@withContext false
        val url = "$FIREBASE_DB_URL/master_keys/$sanitized.json"
        try {
            val payload = JSONObject().apply {
                put("status", "unused")
                put("created_at", System.currentTimeMillis())
            }
            val request = Request.Builder()
                .url(url)
                .put(payload.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteMasterKey(key: String): Boolean = withContext(Dispatchers.IO) {
        val sanitized = key.trim()
        val url = "$FIREBASE_DB_URL/master_keys/$sanitized.json"
        try {
            val request = Request.Builder().url(url).delete().build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }
}
