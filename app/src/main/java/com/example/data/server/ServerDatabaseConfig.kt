package com.example.data.server

data class ServerDatabaseConfig(
    val serverUrl: String = "https://api.qris-audit.internal/v1/database",
    val apiKey: String = "",
    val databaseName: String = "qris_forensic_db",
    val autoSyncEnabled: Boolean = false,
    val lastSyncTime: Long = 0,
    val lastSyncStatus: String = "Belum terhubung ke server",
    val lastSyncSuccess: Boolean = false,
    val lastResponseCode: Int = 0
) {
    val isConfigured: Boolean
        get() = serverUrl.isNotBlank() && (serverUrl.startsWith("http://") || serverUrl.startsWith("https://"))
}

data class ServerTestResult(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val latencyMs: Long
)
