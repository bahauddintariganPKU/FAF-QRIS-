package com.example.data.server

import android.os.SystemClock
import com.example.data.github.SyncResult
import com.example.model.QrisTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ServerDatabaseSyncService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    /**
     * Tests the connectivity to the configured Server Database Link.
     */
    suspend fun testConnection(config: ServerDatabaseConfig): Result<ServerTestResult> = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext Result.failure(
                IllegalArgumentException("URL Server Database tidak valid. Format harus diawali http:// atau https://")
            )
        }

        val startTime = SystemClock.elapsedRealtime()
        try {
            val requestBuilder = Request.Builder()
                .url(config.serverUrl.trim())
                .header("Accept", "application/json")
                .header("User-Agent", "Audit-QRIS-Android-Client/2.0")

            if (config.apiKey.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer ${config.apiKey.trim()}")
            }

            // Simple GET / HEAD ping to verify host and endpoint responsiveness
            val request = requestBuilder.get().build()

            client.newCall(request).execute().use { response ->
                val latency = SystemClock.elapsedRealtime() - startTime
                val code = response.code
                val isSuccess = response.isSuccessful || code == 405 // 405 Method Not Allowed means host is alive and listening

                val msg = when {
                    response.isSuccessful -> "Server Database aktif dan merespon! (HTTP $code, latency: ${latency}ms)"
                    code == 401 || code == 403 -> "Server merespon (HTTP $code), otentikasi API Key diperlukan."
                    code == 404 -> "Server terhubung namun endpoint '${config.serverUrl}' mengembalikan 404 Not Found."
                    code == 405 -> "Endpoint server aktif (HTTP 405 - Siap menerima data via POST sinkronisasi)."
                    else -> "Server merespon dengan status HTTP $code (${response.message})"
                }

                Result.success(
                    ServerTestResult(
                        success = isSuccess,
                        statusCode = code,
                        message = msg,
                        latencyMs = latency
                    )
                )
            }
        } catch (e: IOException) {
            val latency = SystemClock.elapsedRealtime() - startTime
            Result.failure(Exception("Gagal menghubungi server database: ${e.localizedMessage ?: "Timeout / Masalah DNS/Koneksi"}", e))
        } catch (e: Exception) {
            Result.failure(Exception("Kesalahan konfigurasi: ${e.message}", e))
        }
    }

    /**
     * Pushes transactions and forensic audit assessment to the server database link.
     */
    suspend fun syncTransactionsToServer(
        config: ServerDatabaseConfig,
        transactions: List<QrisTransaction>,
        auditorName: String
    ): SyncResult = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext SyncResult(
                success = false,
                message = "URL Server Database belum dikonfigurasi dengan benar."
            )
        }

        try {
            val jsonArray = JSONArray()
            transactions.forEach { tx ->
                val item = JSONObject().apply {
                    put("id", tx.id)
                    put("rrn", tx.rrn)
                    put("waktu", tx.waktu)
                    put("jam", tx.jam)
                    put("id_kasir", tx.idKasir)
                    put("user_id_konsumen", tx.userIdKonsumen)
                    put("nominal", tx.nominal)
                    put("digit_pertama", tx.digitPertama)
                    put("flag_jam_tutup", tx.flagJamTutup)
                    put("flag_split_tx", tx.flagSplitTx)
                    put("flag_nominal_bulat", tx.flagNominalBulat)
                    put("flag_self_tx", tx.flagSelfTx)
                    put("flag_benford_anomali", tx.flagBenfordAnomali)
                    put("total_fraud_score", tx.totalFraudScore)
                    put("kesimpulan_audit", tx.kesimpulanAudit)
                    put("status_investigasi", tx.statusInvestigasi)
                    put("catatan_auditor", tx.catatanAuditor)
                }
                jsonArray.put(item)
            }

            val payload = JSONObject().apply {
                put("database", config.databaseName)
                put("auditor", auditorName)
                put("synced_at", System.currentTimeMillis())
                put("total_records", transactions.size)
                put("records", jsonArray)
            }

            val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val requestBuilder = Request.Builder()
                .url(config.serverUrl.trim())
                .post(body)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "Audit-QRIS-Android-Client/2.0")

            if (config.apiKey.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer ${config.apiKey.trim()}")
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val code = response.code
                val responseBodyStr = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    val batchId = "BATCH-${System.currentTimeMillis() % 100000}"
                    SyncResult(
                        success = true,
                        commitSha = batchId,
                        commitUrl = config.serverUrl,
                        message = "Berhasil mengirim ${transactions.size} transaksi ke database '${config.databaseName}' (HTTP $code)"
                    )
                } else {
                    SyncResult(
                        success = false,
                        message = "Server menolak data (HTTP $code): ${responseBodyStr.take(150).ifEmpty { response.message }}"
                    )
                }
            }
        } catch (e: Exception) {
            SyncResult(
                success = false,
                message = "Kesalahan koneksi saat sinkronisasi server: ${e.localizedMessage ?: "Cek jaringan internet"}"
            )
        }
    }
}
