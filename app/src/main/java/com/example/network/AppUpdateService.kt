package com.example.network

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val hasUpdate: Boolean = false,
    val latestVersionName: String = "",
    val downloadUrl: String = "",
    val releaseNotes: String = "",
    val tagName: String = ""
)

object AppUpdateService {
    private const val TAG = "AppUpdateService"
    private const val GITHUB_REPO = "djpagla12121-web/FB-TOOLS-APK"
    private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdate(currentVersionName: String): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(GITHUB_API_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "FB-Tools-Android-App")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "GitHub release check failed with HTTP code ${response.code}")
                return@withContext UpdateInfo(hasUpdate = false)
            }

            val body = response.body?.string() ?: return@withContext UpdateInfo(hasUpdate = false)
            val json = JSONObject(body)

            val tagName = json.optString("tag_name", "").trim()
            val releaseNotes = json.optString("body", "Bug fixes and performance improvements.")
            val cleanLatestVersion = tagName.removePrefix("v").removePrefix("V").trim()
            val cleanCurrentVersion = currentVersionName.removePrefix("v").removePrefix("V").trim()

            var apkDownloadUrl = ""
            val assets = json.optJSONArray("assets")
            if (assets != null && assets.length() > 0) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
                if (apkDownloadUrl.isEmpty()) {
                    apkDownloadUrl = assets.getJSONObject(0).optString("browser_download_url", "")
                }
            }

            if (apkDownloadUrl.isEmpty()) {
                apkDownloadUrl = "https://github.com/$GITHUB_REPO/releases/latest/download/app-debug.apk"
            }

            val isNewer = isVersionNewer(cleanLatestVersion, cleanCurrentVersion)

            Log.d(TAG, "Checked update: current=$cleanCurrentVersion, latest=$cleanLatestVersion, hasUpdate=$isNewer, url=$apkDownloadUrl")

            UpdateInfo(
                hasUpdate = isNewer,
                latestVersionName = cleanLatestVersion.ifEmpty { tagName },
                downloadUrl = apkDownloadUrl,
                releaseNotes = releaseNotes,
                tagName = tagName
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check update", e)
            UpdateInfo(hasUpdate = false)
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        if (latest.isEmpty() || current.isEmpty()) return false
        if (latest == current) return false

        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until length) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "FB-Tools-Android-App")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Download APK failed with code ${response.code}")
                return@withContext null
            }

            val responseBody = response.body ?: return@withContext null
            val contentLength = responseBody.contentLength()

            val cacheDir = context.externalCacheDir ?: context.cacheDir
            val updateFile = File(cacheDir, "update_app.apk")
            if (updateFile.exists()) {
                updateFile.delete()
            }

            val inputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(updateFile)

            val buffer = ByteArray(8192)
            var totalBytesRead = 0L
            var read: Int

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                totalBytesRead += read
                if (contentLength > 0) {
                    val progress = ((totalBytesRead * 100) / contentLength).toInt()
                    onProgress(progress.coerceIn(0, 100))
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            updateFile
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading APK", e)
            null
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        return try {
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            false
        }
    }
}
