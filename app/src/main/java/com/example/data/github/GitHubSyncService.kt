package com.example.data.github

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GitHubSyncService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    /**
     * Checks if the repo and token are valid.
     */
    suspend fun testConnection(config: GitHubConfig): Result<String> = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext Result.failure(IllegalArgumentException("Konfigurasi GitHub belum lengkap"))
        }

        try {
            val url = "https://api.github.com/repos/${config.owner.trim()}/${config.repo.trim()}"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${config.token.trim()}")
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "Audit-QRIS-Android")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val fullName = json.optString("full_name", "${config.owner}/${config.repo}")
                    val defaultBranch = json.optString("default_branch", "main")
                    Result.success("Terhubung ke repositori: $fullName (Branch default: $defaultBranch)")
                } else if (response.code == 401) {
                    Result.failure(Exception("Autentikasi gagal (401): Periksa Personal Access Token GitHub Anda"))
                } else if (response.code == 404) {
                    Result.failure(Exception("Repositori tidak ditemukan (404): Periksa username/organisasi dan nama repo"))
                } else {
                    Result.failure(Exception("Gagal terhubung (${response.code}): $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Koneksi gagal: ${e.localizedMessage ?: "Cek jaringan internet"}", e))
        }
    }

    /**
     * Retrieves the current SHA of the file if it already exists in the repo.
     */
    private suspend fun getExistingFileSha(config: GitHubConfig): String? = withContext(Dispatchers.IO) {
        val cleanPath = config.filePath.trim().trimStart('/')
        val branch = if (config.branch.isNotBlank()) config.branch.trim() else "main"
        val url = "https://api.github.com/repos/${config.owner.trim()}/${config.repo.trim()}/contents/$cleanPath?ref=$branch"

        try {
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${config.token.trim()}")
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "Audit-QRIS-Android")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@use null
                    val json = JSONObject(body)
                    return@use json.optString("sha").takeIf { it.isNotBlank() }
                }
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Uploads / syncs the audit CSV content to GitHub repo.
     */
    suspend fun syncAuditCsv(
        config: GitHubConfig,
        csvContent: String,
        commitMessage: String
    ): SyncResult = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext SyncResult(
                success = false,
                message = "Konfigurasi GitHub belum lengkap. Masukkan token dan nama repo."
            )
        }

        try {
            val cleanPath = config.filePath.trim().trimStart('/')
            val branch = if (config.branch.isNotBlank()) config.branch.trim() else "main"
            val existingSha = getExistingFileSha(config)

            val base64Content = Base64.encodeToString(
                csvContent.toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )

            val payload = JSONObject().apply {
                put("message", commitMessage)
                put("content", base64Content)
                put("branch", branch)
                if (existingSha != null) {
                    put("sha", existingSha)
                }
            }

            val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "https://api.github.com/repos/${config.owner.trim()}/${config.repo.trim()}/contents/$cleanPath"

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${config.token.trim()}")
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "Audit-QRIS-Android")
                .put(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val commitObj = json.optJSONObject("commit")
                    val commitSha = commitObj?.optString("sha")?.take(7) ?: "success"
                    val htmlUrl = commitObj?.optString("html_url")

                    SyncResult(
                        success = true,
                        commitSha = commitSha,
                        commitUrl = htmlUrl,
                        message = "Sinkronisasi berhasil! File $cleanPath di-commit ke branch $branch (SHA: $commitSha)"
                    )
                } else {
                    val errorMsg = try {
                        val json = JSONObject(body)
                        json.optString("message", body)
                    } catch (_: Exception) {
                        body
                    }

                    val userFriendly = when (response.code) {
                        401 -> "Token akses GitHub tidak valid atau kedaluwarsa."
                        404 -> "Repositori '${config.owner}/${config.repo}' tidak ditemukan."
                        409 -> "Konflik commit (SHA mismatch). Silakan coba sync ulang."
                        422 -> "Validasi gagal: $errorMsg"
                        else -> "Gagal sync (HTTP ${response.code}): $errorMsg"
                    }

                    SyncResult(
                        success = false,
                        message = userFriendly
                    )
                }
            }
        } catch (e: Exception) {
            SyncResult(
                success = false,
                message = "Kesalahan koneksi saat sinkronisasi GitHub: ${e.localizedMessage ?: "Periksa koneksi internet"}"
            )
        }
    }
}
