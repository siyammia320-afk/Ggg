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

    fun createAccountOfficial(
        phoneInput: String,
        country: Country,
        proxyServer: String = "",
        proxyPort: String = "",
        proxyUsername: String = "",
        proxyPassword: String = "",
        customUserAgent: String = "",
        isCustomUserAgentEnabled: Boolean = false
    ): AccountResult {
        val (fname, lname) = country.getRandomFirstAndLastName()
        val day = Random.nextInt(1, 29)
        val month = Random.nextInt(1, 13)
        val year = Random.nextInt(1980, 2006)
        val phone = phoneInput.replace(Regex("[^0-9]"), "")

        val activeClient = if (proxyServer.isNotBlank() && proxyPort.isNotBlank()) {
            val portInt = proxyPort.trim().toIntOrNull() ?: 8080
            val proxy = java.net.Proxy(java.net.Proxy.Type.HTTP, java.net.InetSocketAddress(proxyServer.trim(), portInt))
            client.newBuilder()
                .proxy(proxy)
                .proxyAuthenticator { _, response ->
                    if (proxyUsername.isNotBlank() && proxyPassword.isNotBlank()) {
                        val credential = okhttp3.Credentials.basic(proxyUsername.trim(), proxyPassword.trim())
                        response.request.newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build()
                    } else {
                        null
                    }
                }
                .build()
        } else {
            client
        }

        val userAgent = if (isCustomUserAgentEnabled && customUserAgent.isNotBlank()) {
            customUserAgent
        } else {
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
        }

        val datrCookie = "z8V_ajxf-8PdZE6c8huwEzqD"
        
        val variables = """{"input":{"actor_id":"0","client_mutation_id":"52bb8933-e8f1-4a77-94bc-69bce03e22b7","machine_id":"","reg_data":{"birthday_day":$day,"birthday_month":$month,"birthday_year":$year,"contactpoint":{"sensitive_string_value":"$phone"},"contactpoint_type":"PHONE","custom_gender":"","did_use_age":false,"firstname":{"sensitive_string_value":"$fname"},"fullname":{"sensitive_string_value":""},"ig_age_block_data":null,"lastname":{"sensitive_string_value":"$lname"},"preferred_pronoun":null,"reg_passwd__":{"sensitive_string_value":"#PWD_BROWSER:5:1786758663:AaxQAHSVITW3xp2G2gyDJ7KQS7OJFFNrrOhJmhVcMzN2Qq9lZIYBf6jQ7bQnWQgym+4SQhjOTzyj3mb915sb4JPvKw5h30Qrlk+WAxVUHCcqdQu8hXvynL8fRi5QabcJD6Wem3mYLktN1LjiEwo="},"sex":"FEMALE","use_custom_gender":false,"username":{"sensitive_string_value":""}},"sk_pipa_consent_given":null,"waterfall_id":"2bcb4664-4742-4d09-b685-c885582f9f4e"}}"""

        val formBodyBuilder = FormBody.Builder()
            .add("av", "0")
            .add("__user", "0")
            .add("__a", "1")
            .add("__req", "1a")
            .add("__hs", "20680.HYP:comet_plat_default_pkg.2.1...0")
            .add("dpr", "2")
            .add("__ccg", "GOOD")
            .add("__rev", "1045253825")
            .add("__s", "ytdlvy:ynho8u:nax7pe")
            .add("__hsi", "7674069822963974850")
            .add("__dyn", "7xeUmwlEnwn8K2Wmh0no6u5U4e0yoW3q32360CEbo1nEhw2nVE4W099w8G1Dz81s8hwGwQw9m1YwBgao6C0Mo2swaOfK0EUjwGzE2ZwNwmE2eUlwhE2Lw6OyES1Tw8W0Lo6-1Fw4mwr86C1nwqU8XwnqwIwtU26wbu0eowRzo")
            .add("__csr", "n24I9qvEgOcj2AgIhCsAVlbCiDWBRXeTSRbkx9pcEx6AXaAhZFWhYGzjlpHX9t5SGH8VuR9GLsx25DnFuYxuqAcbtRQJQgKi8EBqUNG8cLRayky8j8mDal8VHHyV2PA9h92lKm4Hxa9wl49J3E-0z8co-5Ub-2eEswBx60E84q589UhxObCw47wey4k0gK2mi0xE2owho2Ozo6-E4W0hG6U7m2eWwUy43-3q093wiE1CU560H80vqw0csIw019fE02zJw115w0S-w08ueayU5q5E")
            .add("__hsdp", "ge9isoLgB3oG7p64E36w8-m481FA0Dm0FxE04I20bFwxw0PIw0eQu06MU09VE")
            .add("__hblp", "02h80Gu1EwfO08Aw0wNw1zO0j20bFwxw6Gw5Kw47w5Qw10y0tK0n20Bo0Iy0ii03wa020i03Oe03Xa0ui0anw8y0he08Qwmo09VE1N87m1ewmo")
            .add("__sjsp", "ge9mIQGZ2kdyEtAoiwcq0zVogw6Cg2to2C6w")
            .add("__comet_req", "102")
            .add("lsd", "AdRMTZWclMqdQrsz6WGMmZ7_kmI")
            .add("jazoest", "22441")
            .add("__spin_r", "1045253825")
            .add("__spin_b", "trunk")
            .add("__spin_t", "1786758616")
            .add("qpl_active_flow_ids", "250359044,516759801")
            .add("fb_api_caller_class", "RelayModern")
            .add("fb_api_req_friendly_name", "useCAARegistrationFormSubmitMutation")
            .add("server_timestamps", "true")
            .add("variables", variables)
            .add("doc_id", "27029416779977343")
            .add("fb_api_analytics_tags", "[\"qpl_active_flow_ids=250359044,516759801\"]")

        val request = Request.Builder()
            .url("https://www.fbsbx.com/api/graphql/")
            .header("Host", "web.facebook.com")
            .header("User-Agent", userAgent)
            .header("Accept-Encoding", "gzip, deflate, br, zstd")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("sec-ch-ua-full-version-list", "\"Not=A?Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"151.0.7922.83\"")
            .header("sec-ch-ua-platform", "\"macOS\"")
            .header("sec-ch-ua", "\"Not=A?Brand\";v=\"99\", \"Chromium\";v=\"151\"")
            .header("x-fb-friendly-name", "useCAARegistrationFormSubmitMutation")
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-model", "\"itel S665L\"")
            .header("x-asbd-id", "359341")
            .header("x-fb-lsd", "AdRMTZWclMqdQrsz6WGMmZ7_kmI")
            .header("sec-ch-prefers-color-scheme", "light")
            .header("sec-ch-ua-platform-version", "\"12.0.0\"")
            .header("origin", "https://web.facebook.com")
            .header("x-requested-with", "mark.via.gp")
            .header("sec-fetch-site", "same-origin")
            .header("sec-fetch-mode", "cors")
            .header("sec-fetch-dest", "empty")
            .header("referer", "https://web.facebook.com/reg/?entry_point=login&next=")
            .header("accept-language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7")
            .header("priority", "u=1, i")
            .header("Cookie", "datr=$datrCookie; fr=0vyhAt6gpZrRsGnhb..Bqf8XQ..AAA.0.0.Bqf8XQ.AWfMsvOpiMmcD2458vHBO-uB2k0; sb=0MV_ar8A9ecW5cQXAbm4EX9D; wd=1280x2226")
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
                    return AccountResult(
                        success = true,
                        uid = cUser,
                        name = "$fname $lname",
                        cookies = cookieString,
                        password = "arafat@@##",
                        phone = phone
                    )
                } else {
                    return AccountResult(
                        success = false,
                        error = if (responseBody.contains("error") || responseBody.contains("checkpoint")) {
                            "Facebook responded with verification/checkpoint or rate limit. \n$responseBody"
                        } else {
                            "No UID returned. \n$responseBody"
                        }
                    )
                }
            } else {
                return AccountResult(
                    success = false,
                    error = "HTTP error ${response.code}: ${response.message}"
                )
            }
        } catch (e: Exception) {
            return AccountResult(success = false, error = e.message ?: "Unknown Error")
        }
    }
}

