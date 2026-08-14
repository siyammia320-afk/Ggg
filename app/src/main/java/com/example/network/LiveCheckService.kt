package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object LiveCheckService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun checkLiveUids(uids: List<String>): Map<String, Boolean> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, Boolean>()
        if (uids.isEmpty()) return@withContext results

        val url = "https://hitools.pro/api/check-live-uid"

        // Build JSON payload: {"uids": ["uid1", "uid2"]}
        val jsonArray = JSONArray()
        for (uid in uids) {
            if (uid.isNotBlank()) {
                jsonArray.put(uid.trim())
            }
        }
        val requestBodyJson = JSONObject().apply {
            put("uids", jsonArray)
        }.toString()

        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toRequestBody(jsonMediaType))
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 12; itel S665L Build/SP1A.210812.016; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/151.0.7922.83 Mobile Safari/537.36")
            .header("Content-Type", "application/json")
            .header("origin", "https://hitools.pro")
            .header("referer", "https://hitools.pro/check-live-uid")
            .header("Cookie", "server_session_36dd6933=67c8c6beee613a1968b0f646c3d972d7; __Host-next-auth.csrf-token=01aedc2a5d44606243e9931a337ab72416cf8e40542520ee8d6ecb55c8c98c85%7C9a6220dd98360d7d7d4e1cdb0f65eaa3da61b5499f5a32a953c641e5543f26da; __Secure-next-auth.callback-url=https%3A%2F%2Fhitools.pro; _ga=GA1.1.1875832987.1786600513; _ga_7V7WRGRP2B=GS2.1.s1786600513\$o1\$g1\$t1786600517\$j56\$l0\$h0")
            .header("sec-ch-ua-platform", "\"Android\"")
            .header("sec-ch-ua", "\"Not=A?Brand\";v=\"99\", \"Android WebView\";v=\"151\", \"Chromium\";v=\"151\"")
            .header("sec-ch-ua-mobile", "?1")
            .header("x-requested-with", "mark.via.gp")
            .header("sec-fetch-site", "same-origin")
            .header("sec-fetch-mode", "cors")
            .header("sec-fetch-dest", "empty")
            .header("accept-language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7")
            .header("priority", "u=1, i")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                // The API can return multiple lines, each being a JSON object:
                // {"uid":"61592789572179","live":true}
                // {"uid":"61592019706404","live":false}
                val lines = responseBody.split("\n")
                for (line in lines) {
                    val trimmedLine = line.trim()
                    if (trimmedLine.isNotEmpty()) {
                        try {
                            if (trimmedLine.startsWith("{")) {
                                val obj = JSONObject(trimmedLine)
                                val uid = obj.optString("uid")
                                if (uid.isNotEmpty()) {
                                    val isLive = obj.optBoolean("live")
                                    results[uid] = isLive
                                }
                            } else if (trimmedLine.startsWith("[")) {
                                val arr = JSONArray(trimmedLine)
                                for (i in 0 until arr.length()) {
                                    val obj = arr.getJSONObject(i)
                                    val uid = obj.optString("uid")
                                    if (uid.isNotEmpty()) {
                                        val isLive = obj.optBoolean("live")
                                        results[uid] = isLive
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext results
    }
}
