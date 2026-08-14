package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object MasterKeyService {

    private const val FIREBASE_DB_URL = "https://fb-virul-tools-default-rtdb.firebaseio.com"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    data class ValidationResult(
        val isValid: Boolean,
        val isUsed: Boolean,
        val usedByApiKey: String = "",
        val errorMessage: String = ""
    )

    suspend fun validateMasterKey(masterKey: String): ValidationResult = withContext(Dispatchers.IO) {
        val sanitizedKey = masterKey.trim()
        if (sanitizedKey.isEmpty()) {
            return@withContext ValidationResult(false, false, errorMessage = "Master Key cannot be empty!")
        }
        val url = "$FIREBASE_DB_URL/master_keys/$sanitizedKey.json"
        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ValidationResult(false, false, errorMessage = "Server error: ${response.code}")
                }
                val body = response.body?.string()?.trim() ?: ""
                if (body.isEmpty() || body == "null") {
                    return@withContext ValidationResult(false, false, errorMessage = "মাস্টার কী-টি সঠিক নয়! দয়া করে সঠিক কী দিন।")
                }

                val json = JSONObject(body)
                val status = json.optString("status", "unused")
                val usedBy = json.optString("used_by_api_key", "")
                
                return@withContext ValidationResult(
                    isValid = true,
                    isUsed = status == "used",
                    usedByApiKey = usedBy
                )
            }
        } catch (e: Exception) {
            return@withContext ValidationResult(false, false, errorMessage = "নেটওয়ার্ক সমস্যা! আবার চেষ্টা করুন: ${e.message}")
        }
    }

    suspend fun claimMasterKey(masterKey: String, apiKey: String): Boolean = withContext(Dispatchers.IO) {
        val sanitizedKey = masterKey.trim()
        val sanitizedApi = apiKey.trim()
        val url = "$FIREBASE_DB_URL/master_keys/$sanitizedKey.json"
        
        try {
            val payload = JSONObject().apply {
                put("status", "used")
                put("used_by_api_key", sanitizedApi)
                put("used_at", System.currentTimeMillis())
            }

            val request = Request.Builder()
                .url(url)
                .patch(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            return@withContext false
        }
    }
}
