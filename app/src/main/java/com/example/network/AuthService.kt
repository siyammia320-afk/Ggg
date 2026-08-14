package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AuthService {
    private const val FIREBASE_DB_URL = "https://fb-virul-tools-default-rtdb.firebaseio.com"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun sanitizeEmail(email: String): String {
        return email.trim().lowercase()
            .replace(".", "_dot_")
            .replace("@", "_at_")
            .replace("#", "_hash_")
            .replace("$", "_dollar_")
            .replace("[", "_ob_")
            .replace("]", "_cb_")
    }

    data class AuthResult(
        val success: Boolean,
        val message: String,
        val firstName: String = "",
        val lastName: String = "",
        val telegramUsername: String = ""
    )

    suspend fun signUp(
        firstName: String,
        lastName: String,
        telegramUsername: String,
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val fName = firstName.trim()
        val lName = lastName.trim()
        val tgUser = telegramUsername.trim().replace("@", "")
        val mail = email.trim()
        val pass = password.trim()

        if (fName.isEmpty() || lName.isEmpty() || tgUser.isEmpty() || mail.isEmpty() || pass.isEmpty()) {
            return@withContext AuthResult(false, "সকল তথ্য পূরণ করা আবশ্যক!")
        }

        val sanitized = sanitizeEmail(mail)
        val url = "$FIREBASE_DB_URL/users/$sanitized.json"

        try {
            // Step 1: Check if user already exists
            val checkRequest = Request.Builder().url(url).get().build()
            client.newCall(checkRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()?.trim() ?: ""
                    if (body.isNotEmpty() && body != "null") {
                        return@withContext AuthResult(false, "এই ইমেইল দিয়ে ইতিমধ্যে অ্যাকাউন্ট তৈরি করা আছে!")
                    }
                }
            }

            // Step 2: Create new user
            val userJson = JSONObject().apply {
                put("firstName", fName)
                put("lastName", lName)
                put("telegramUsername", tgUser)
                put("email", mail)
                put("password", pass)
                put("createdAt", System.currentTimeMillis())
            }

            val signUpRequest = Request.Builder()
                .url(url)
                .put(userJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(signUpRequest).execute().use { response ->
                if (response.isSuccessful) {
                    return@withContext AuthResult(true, "অ্যাকাউন্ট তৈরি সফল হয়েছে!", fName, lName, tgUser)
                } else {
                    return@withContext AuthResult(false, "অ্যাকাউন্ট তৈরি ব্যর্থ হয়েছে: ${response.code}")
                }
            }
        } catch (e: Exception) {
            return@withContext AuthResult(false, "নেটওয়ার্ক সমস্যা! অনুগ্রহ করে আবার চেষ্টা করুন: ${e.message}")
        }
    }

    suspend fun logIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val mail = email.trim()
        val pass = password.trim()

        if (mail.isEmpty() || pass.isEmpty()) {
            return@withContext AuthResult(false, "ইমেইল এবং পাসওয়ার্ড আবশ্যক!")
        }

        val sanitized = sanitizeEmail(mail)
        val url = "$FIREBASE_DB_URL/users/$sanitized.json"

        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext AuthResult(false, "সার্ভার এরর: ${response.code}")
                }
                val body = response.body?.string()?.trim() ?: ""
                if (body.isEmpty() || body == "null") {
                    return@withContext AuthResult(false, "কোনো অ্যাকাউন্ট পাওয়া যায়নি! আগে সাইন আপ করুন।")
                }

                val json = JSONObject(body)
                val storedPassword = json.optString("password", "")
                if (storedPassword == pass) {
                    val firstName = json.optString("firstName", "")
                    val lastName = json.optString("lastName", "")
                    val telegramUsername = json.optString("telegramUsername", "")
                    return@withContext AuthResult(true, "লগইন সফল হয়েছে!", firstName, lastName, telegramUsername)
                } else {
                    return@withContext AuthResult(false, "ভুল পাসওয়ার্ড! দয়া করে সঠিক পাসওয়ার্ড দিন।")
                }
            }
        } catch (e: Exception) {
            return@withContext AuthResult(false, "নেটওয়ার্ক সমস্যা! অনুগ্রহ করে আবার চেষ্টা করুন: ${e.message}")
        }
    }
}
