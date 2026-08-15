package com.example.network

import com.example.data.Country
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class AccountResult(
    val success: Boolean,
    val uid: String = "",
    val name: String = "",
    val cookies: String = "",
    val password: String = "",
    val phone: String = "",
    val error: String = ""
)

object FbAccountService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun generateDatrCookie(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-"
        return (1..24)
            .map { allowedChars.random() }
            .joinToString("")
    }

    suspend fun createAccount(
        phoneInput: String,
        passwordInput: String,
        country: Country = Country.BANGLADESH,
        proxyServer: String = "",
        proxyPort: String = "",
        proxyUsername: String = "",
        proxyPassword: String = "",
        customUserAgent: String = "",
        isCustomUserAgentEnabled: Boolean = false
    ): AccountResult = withContext(Dispatchers.IO) {
        val datrCookie = generateDatrCookie()
        val (fname, lname) = country.getRandomFirstAndLastName()
        val day = Random.nextInt(1, 29)
        val month = Random.nextInt(1, 13)
        val year = Random.nextInt(1980, 2006)
        val phone = if (phoneInput.contains("@")) phoneInput.trim() else phoneInput.replace(Regex("[^0-9]"), "")

        // Configure OkHttpClient with Proxy if provided
        val activeClient = if (proxyServer.isNotBlank() && proxyPort.isNotBlank()) {
            val portInt = proxyPort.trim().toIntOrNull() ?: 8080
            val proxy = java.net.Proxy(
                java.net.Proxy.Type.HTTP,
                java.net.InetSocketAddress(proxyServer.trim(), portInt)
            )
            val builder = client.newBuilder().proxy(proxy)
            if (proxyUsername.isNotBlank() && proxyPassword.isNotBlank()) {
                builder.proxyAuthenticator { _, response ->
                    val credential = okhttp3.Credentials.basic(proxyUsername.trim(), proxyPassword.trim())
                    response.request.newBuilder().header("Proxy-Authorization", credential).build()
                }
            }
            builder.build()
        } else {
            client
        }

        val defaultUserAgent = "Mozilla/5.0 (Linux; Android 12; itel S665L Build/SP1A.210812.016; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/151.0.7922.83 Mobile Safari/537.36"
        val userAgent = if (isCustomUserAgentEnabled && customUserAgent.isNotBlank()) {
            customUserAgent.trim()
        } else {
            defaultUserAgent
        }

        val isEmail = phoneInput.contains("@")

        val lsdToken = if (isEmail) "AdTI-lqiNWIq7cVOXUYxUA0bMiw" else "AdTQZqV6VK31DOyCLpwies_uymI"
        val fbDtsg = if (isEmail) "NAfyHkf7eKKhCRzylwuqVnI1ZxHyqN4am8snPlFVbfUMBA0kpU_PZ8Q:0:0" else "NAfye6xUkCgg4lZlvPQsJP2BxJ-DJ9PQjiBxj9cxwBrVbxlD57WdLpA:0:0"
        val jazoest = if (isEmail) "25160" else "25045"
        val dyn = "1Z3pawlEnwm8_Bg9ppoW5UdE4a2i5U4e0C86u7E39x60zU3ex608ewk9E4W0pKq0FE6S0x81vohw73wGwcq1GwqU2YwbK0oi0zE1jU1soG0hi0Lo6-0Co1kU1UU3jwea"
        val aParam = if (isEmail) "AYymxStjpu3pao708AsEj5Lg-ajnyITjlI2TTHb_0YeflPDd_af6ECxJ_PMYwJRGvyNZwhYvgYVV78_CYCmXaWMiflVCUSNunvo" else "AYx1_Idlp-hfWNPJTfQ4-esKhzaqHCpdg0Rv8FjrlYPOv62aLdqnRnx7Y-Vb2FitdYUgUX6rSVUOigyOCOuJp5LpQ4gtxN-2nRM"
        val reqParam = if (isEmail) "g" else "8"

        val formBodyBuilder = FormBody.Builder()
            .add("ccp", "2")
            .add("reg_instance", datrCookie)
            .add("submission_request", "true")
            .add("helper", "")
            .add("reg_impression_id", UUID.randomUUID().toString())
            .add("ns", "1")
            .add("zero_header_af_client", "")
            .add("app_id", "103")
            .add("logger_id", UUID.randomUUID().toString())
            .add("field_names[0]", "firstname")
            .add("firstname", fname)
            .add("lastname", lname)
            .add("field_names[1]", "birthday_wrapper")
            .add("birthday_day", day.toString())
            .add("birthday_month", month.toString())
            .add("birthday_year", year.toString())
            .add("age_step_input", "")
            .add("did_use_age", "false")
            .add("field_names[2]", "reg_email__")
            .add("reg_email__", phone)
            .add("field_names[3]", "sex")
            .add("sex", "2")
            .add("preferred_pronoun", "")
            .add("custom_gender", "")
            .add("field_names[4]", "reg_passwd__")
            .add("reg_passwd__", passwordInput)
            .add("was_shown_name_suggestions", "false")
            .add("did_use_suggested_name", "false")
            .add("use_custom_gender", "false")
            .add("guid", "")
            .add("pre_form_step", "")
            .add("fb_dtsg", fbDtsg)
            .add("jazoest", jazoest)
            .add("lsd", lsdToken)
            .add("__dyn", dyn)
            .add("__csr", "")
            .add("__hsdp", "")
            .add("__hblp", "")
            .add("__sjsp", "")
            .add("__req", reqParam)
            .add("__fmt", "1")
            .add("__a", aParam)
            .add("__user", "0")

        val url = if (isEmail) {
            "https://www.fbsbx.com/reg/submit/?app_id=103&multi_step_form=1&skip_suma=0&shouldForceMTouch=1"
        } else {
            "https://limited.facebook.com/reg/submit/?privacy_mutation_token=eyJ0eXBlIjowLCJjcmVhdGlvbl90aW1lIjoxNzg2NjY3NDkyLCJjYWxsc2l0ZV9pZCI6OTA3OTI0NDAyOTQ4MDU4fQ%3D%3D&app_id=103&multi_step_form=1&skip_suma=0&shouldForceMTouch=1"
        }

        val requestBuilder = Request.Builder()
            .url(url)

        if (isEmail) {
            requestBuilder.header("Host", "limited.facebook.com")
            requestBuilder.header("priority", "u=1, i")
        }

        val request = requestBuilder
            .header("User-Agent", userAgent)
            .header("Accept-Encoding", "gzip, deflate, br, zstd")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("sec-ch-ua-platform", "\"Android\"")
            .header("sec-ch-ua", "\"Not=A?Brand\";v=\"99\", \"Android WebView\";v=\"151\", \"Chromium\";v=\"151\"")
            .header("x-response-format", "JSONStream")
            .header("sec-ch-ua-mobile", "?1")
            .header("x-asbd-id", "359341")
            .header("x-fb-lsd", lsdToken)
            .header("x-requested-with", "XMLHttpRequest")
            .header("origin", "https://limited.facebook.com")
            .header("sec-fetch-site", "same-origin")
            .header("sec-fetch-mode", "cors")
            .header("sec-fetch-dest", "empty")
            .header("referer", "https://limited.facebook.com/reg/?is_two_steps_login=0&cid=103&refsrc=deprecated&soft=hjk")
            .header("accept-language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7")
            .header("Cookie", "datr=$datrCookie")
            .post(formBodyBuilder.build())
            .build()

        try {
            val response = activeClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val rawCookies = response.headers.values("Set-Cookie")
                val cookieMap = mutableMapOf<String, String>()

                for (header in rawCookies) {
                    val parts = header.split(";")[0].split("=", limit = 2)
                    if (parts.size == 2) {
                        cookieMap[parts[0].trim()] = parts[1].trim()
                    }
                }

                var cUser = cookieMap["c_user"]
                if (cUser.isNullOrEmpty()) {
                    // Try parsing from response body
                    val cUserRegex = Regex("\"(?:c_user|userID|uid|account_id)\"\\s*:\\s*\"?(\\d+)\"?")
                    val match = cUserRegex.find(responseBody)
                    if (match != null) {
                        cUser = match.groupValues[1]
                        cookieMap["c_user"] = cUser
                    }
                }

                if (!cUser.isNullOrEmpty()) {
                    val requiredKeys = listOf("datr", "sb", "ps_l", "ps_n", "m_pixel_ratio", "wd", "c_user", "fr", "xs")
                    val cookieParts = mutableListOf<String>()
                    for (k in requiredKeys) {
                        if (cookieMap.containsKey(k)) {
                            cookieParts.add("$k=${cookieMap[k]?.replace(" ", "")}")
                        } else if (k == "datr") {
                            cookieParts.add("datr=$datrCookie")
                        }
                    }
                    val cookieString = cookieParts.joinToString("; ")
                    AccountResult(
                        success = true,
                        uid = cUser,
                        name = "$fname $lname",
                        cookies = cookieString,
                        password = passwordInput,
                        phone = phone
                    )
                } else {
                    AccountResult(
                        success = false,
                        error = if (responseBody.contains("error") || responseBody.contains("checkpoint")) {
                            "Facebook responded with verification/checkpoint or rate limit."
                        } else {
                            "No UID returned in registration response."
                        }
                    )
                }
            } else {
                AccountResult(
                    success = false,
                    error = "HTTP error ${response.code}: ${response.message}"
                )
            }
        } catch (e: Exception) {
            AccountResult(
                success = false,
                error = e.localizedMessage ?: "Network connection error"
            )
        }
    }
}
