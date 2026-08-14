package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object FbIdentifyService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun checkAccountExists(phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val cleanNumber = phoneNumber.trim().replace(Regex("[^0-9]"), "")
        if (cleanNumber.isEmpty()) return@withContext false

        val url = "https://limited.facebook.com/login/identify/?ctx=recover&c=%2Flogin%2F&search_attempts=1&ars=facebook_login&alternate_search=0&show_friend_search_filtered_list=0&birth_month_search=0&city_search=0"

        val formBody = FormBody.Builder()
            .add("lsd", "AdTRKkoA8wsySxIE2TdD6iU_QJ0")
            .add("jazoest", "22289")
            .add("email", cleanNumber)
            .add("did_submit", "Search")
            .build()

        val cookieStr = "datr=SLF9aj9Wt7tNcIW-9Gb9wCqL; sb=SLF9akg-mPAPOcSBFfndONP3; m_pixel_ratio=2; wd=360x806; sfiu=AYiFCKBtAd56lLrUVm6PmSYlezA8PhzU_dAGz5Kwxhhq2DE1bwrGxrPEzc8bpGhA6iVKg5klDyMJeZdEM0PhXBb3V4hnEPhczH5cywYyji4TP7ZmkY_dcM_UZesz66y6ivpMND_j260gZsDz7o2KSmfPWDAx7I3iqqpVZ0Qc436AHTClquvIqtVQPrAJM6I7tOqoKakWdDGZuGEyGuh4Oh8GK7cgspCDbIK5xOX55SeCVw; ps_l=1; ps_n=1; fr=0VzXLlYqAKyDKioPc..BqfbFI..AAA.0.0.BqfbF6.AWeiVS5N9ncxKvNEkfzDeHA_Afs"

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 12; itel S665L Build/SP1A.210812.016; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/151.0.7922.83 Mobile Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("cache-control", "max-age=0")
            .header("sec-ch-ua", "\"Not=A?Brand\";v=\"99\", \"Android WebView\";v=\"151\", \"Chromium\";v=\"151\"")
            .header("sec-ch-ua-mobile", "?1")
            .header("sec-ch-ua-platform", "\"Android\"")
            .header("upgrade-insecure-requests", "1")
            .header("origin", "https://limited.facebook.com")
            .header("x-requested-with", "mark.via.gp")
            .header("sec-fetch-site", "same-origin")
            .header("sec-fetch-mode", "navigate")
            .header("sec-fetch-user", "?1")
            .header("sec-fetch-dest", "document")
            .header("referer", "https://limited.facebook.com/login/identify/?ctx=recover&c=https%3A%2F%2Flimited.facebook.com%2F&multiple_results=0&ars=facebook_login&from_login_screen=0&lwv=100&wtsid=rdr_0yccqQmGRTKdjuIFq&_rdr")
            .header("accept-language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7")
            .header("priority", "u=0, i")
            .header("Cookie", cookieStr)
            .build()

        try {
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""

            // Check if HTML indicates an account was found
            val hasAccount = html.contains("cuid_selected") ||
                    html.contains("contact_point_selector_form") ||
                    html.contains("Choose your account") ||
                    html.contains("We'll send it to") ||
                    html.contains("account_recovery_initiate_view_label") ||
                    html.contains("recover_method") ||
                    html.contains("identifier=") ||
                    html.contains("itemWithAction") ||
                    html.contains("profile/pic.php")

            return@withContext hasAccount
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
