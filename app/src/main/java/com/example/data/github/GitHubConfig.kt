package com.example.data.github

data class GitHubConfig(
    val token: String = "",
    val owner: String = "",
    val repo: String = "",
    val filePath: String = "hasil_audit_forensik_qris.csv",
    val branch: String = "main",
    val autoSyncEnabled: Boolean = true,
    val lastSyncTime: Long = 0,
    val lastCommitSha: String = "",
    val lastSyncStatus: String = "Belum terhubung",
    val lastSyncSuccess: Boolean = false
) {
    val isConfigured: Boolean
        get() = token.isNotBlank() && owner.isNotBlank() && repo.isNotBlank()
}

data class SyncResult(
    val success: Boolean,
    val commitSha: String? = null,
    val commitUrl: String? = null,
    val message: String
)
