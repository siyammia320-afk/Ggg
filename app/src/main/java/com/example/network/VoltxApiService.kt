package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class VoltxOtpItem(
    val number: String,
    val message: String,
    val otp: String,
    val service: String
)

object VoltxApiService {

    private const val API_BASE_URL = "https://api.2oo9.cloud/MXS47FLFX0U/tnevs/@public/api"
    
    var currentApiKey: String
        get() = "MFSCNKJSFBI"
        set(value) {}

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun fetchLiveFacebookRanges(): List<String> = withContext(Dispatchers.IO) {
        val url = "$API_BASE_URL/liveaccess"
        val request = Request.Builder()
            .url(url)
            .header("mauthapi", currentApiKey)
            .get()
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                val jsonObj = JSONObject(bodyStr)
                if (jsonObj.optJSONObject("meta")?.optInt("code") == 200) {
                    val dataObj = jsonObj.optJSONObject("data")
                    val servicesArr = dataObj?.optJSONArray("services")
                    val facebookRanges = mutableListOf<String>()

                    if (servicesArr != null) {
                        for (i in 0 until servicesArr.length()) {
                            val serviceObj = servicesArr.getJSONObject(i)
                            val sid = serviceObj.optString("sid", "")
                            if (sid.equals("facebook", ignoreCase = true)) {
                                val rangesArr = serviceObj.optJSONArray("ranges")
                                if (rangesArr != null) {
                                    for (j in 0 until rangesArr.length()) {
                                        val rangeStr = rangesArr.getString(j)
                                        if (rangeStr.isNotBlank()) {
                                            facebookRanges.add(rangeStr)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return@withContext facebookRanges
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList()
    }

    suspend fun fetchPhoneNumber(rangeCode: String): String? = withContext(Dispatchers.IO) {
        val rid = rangeCode.replace("XXX", "", ignoreCase = true)
            .replace("X", "", ignoreCase = true)
            .trim()

        if (rid.isEmpty()) return@withContext null

        val url = "$API_BASE_URL/getnum"
        val payload = JSONObject().apply {
            put("rid", rid)
        }.toString()

        val request = Request.Builder()
            .url(url)
            .header("mauthapi", currentApiKey)
            .header("Content-Type", "application/json")
            .post(payload.toRequestBody(jsonMediaType))
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                val jsonObj = JSONObject(bodyStr)
                if (jsonObj.optJSONObject("meta")?.optInt("code") == 200) {
                    val dataObj = jsonObj.optJSONObject("data")
                    val fullNumber = dataObj?.optString("full_number")
                        ?: dataObj?.optString("no_plus_number")
                    if (!fullNumber.isNullOrBlank()) {
                        return@withContext fullNumber.replace("+", "").trim()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun checkSuccessOtps(): List<VoltxOtpItem> = withContext(Dispatchers.IO) {
        val url = "$API_BASE_URL/success-otp"
        val request = Request.Builder()
            .url(url)
            .header("mauthapi", currentApiKey)
            .get()
            .build()

        val results = mutableListOf<VoltxOtpItem>()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                val jsonObj = JSONObject(bodyStr)
                if (jsonObj.optJSONObject("meta")?.optInt("code") == 200) {
                    val otpsArr = jsonObj.optJSONObject("data")?.optJSONArray("otps")
                    if (otpsArr != null) {
                        for (i in 0 until otpsArr.length()) {
                            val item = otpsArr.getJSONObject(i)
                            val number = item.optString("number", "").replace("+", "").trim()
                            val message = item.optString("message", "")
                            if (number.isNotBlank() && message.isNotBlank()) {
                                val otpCode = extractOtpFromText(message)
                                if (otpCode != "N/A") {
                                    results.add(
                                        VoltxOtpItem(
                                            number = number,
                                            message = message,
                                            otp = otpCode,
                                            service = "Facebook"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext results
    }

    private fun extractOtpFromText(text: String): String {
        val patterns = listOf(
            Pattern.compile("(?:code|otp|is|pin|fb|facebook)[:\\s-]*(\\d{3,8})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(\\d{8})\\b"),
            Pattern.compile("\\b(\\d{7})\\b"),
            Pattern.compile("\\b(\\d{6})\\b"),
            Pattern.compile("\\b(\\d{5})\\b"),
            Pattern.compile("\\b(\\d{4})\\b"),
            Pattern.compile("\\b(\\d{3})\\b")
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val group = if (matcher.groupCount() >= 1 && matcher.group(1) != null) matcher.group(1) else matcher.group(0)
                if (!group.isNullOrBlank() && group.length >= 3) {
                    return group
                }
            }
        }

        val cleanText = text.replace("-", "").replace(" ", "")
        for (pattern in patterns) {
            val matcher = pattern.matcher(cleanText)
            if (matcher.find()) {
                val group = if (matcher.groupCount() >= 1 && matcher.group(1) != null) matcher.group(1) else matcher.group(0)
                if (!group.isNullOrBlank() && group.length >= 3) {
                    return group
                }
            }
        }

        val fallbackMatcher = Pattern.compile("(\\d{3,8})").matcher(cleanText)
        if (fallbackMatcher.find()) {
            val g = fallbackMatcher.group(1)
            if (!g.isNullOrBlank()) return g
        }

        return "N/A"
    }
}
