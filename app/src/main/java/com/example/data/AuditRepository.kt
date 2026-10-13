package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.AppDatabase
import com.example.data.db.AuditLogEntity
import com.example.data.db.TransactionEntity
import com.example.data.github.GitHubConfig
import com.example.data.github.GitHubSyncService
import com.example.data.github.SyncResult
import com.example.data.server.ServerDatabaseConfig
import com.example.data.server.ServerDatabaseSyncService
import com.example.data.server.ServerTestResult
import com.example.engine.ForensicAuditEngine
import com.example.model.AuditorProfile
import com.example.model.QrisTransaction
import com.example.notification.ForensicNotificationHelper
import com.example.notification.RealtimeAlertItem
import com.example.util.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuditRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context),
    private val gitHubService: GitHubSyncService = GitHubSyncService(),
    private val serverDatabaseService: ServerDatabaseSyncService = ServerDatabaseSyncService()
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("qris_audit_prefs", Context.MODE_PRIVATE)

    private val transactionDao = database.transactionDao()
    private val auditLogDao = database.auditLogDao()

    // Realtime alert feed for in-app display
    private val _realtimeAlerts = MutableStateFlow<List<RealtimeAlertItem>>(emptyList())
    val realtimeAlerts: StateFlow<List<RealtimeAlertItem>> = _realtimeAlerts.asStateFlow()

    // Current GitHub configuration
    private val _gitHubConfig = MutableStateFlow(loadGitHubConfig())
    val gitHubConfig: StateFlow<GitHubConfig> = _gitHubConfig.asStateFlow()

    // Current Server Database Link configuration
    private val _serverConfig = MutableStateFlow(loadServerConfig())
    val serverConfig: StateFlow<ServerDatabaseConfig> = _serverConfig.asStateFlow()

    // Current App Language (ID / EN)
    private val _appLanguage = MutableStateFlow(loadLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // Current Auditor profile
    private val _activeAuditor = MutableStateFlow(loadAuditor())
    val activeAuditor: StateFlow<AuditorProfile> = _activeAuditor.asStateFlow()

    // Syncing state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Expose all transactions audited reactively
    val auditedTransactionsFlow: Flow<List<QrisTransaction>> =
        transactionDao.getAllTransactionsFlow().map { entities ->
            val domainList = entities.map { it.toDomain() }
            ForensicAuditEngine.auditTransactions(domainList)
        }

    val auditLogsFlow: Flow<List<AuditLogEntity>> = auditLogDao.getAllLogsFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            if (transactionDao.getCount() == 0) {
                seedInitialData()
            }
        }
    }

    private suspend fun seedInitialData() = withContext(Dispatchers.IO) {
        val seed = ForensicAuditEngine.getInitialSeedData()
        val entities = seed.map { TransactionEntity.fromDomain(it) }
        transactionDao.insertAll(entities)

        auditLogDao.insert(
            AuditLogEntity(
                auditorName = _activeAuditor.value.name,
                actionType = "INIT_DATA",
                description = "Inisialisasi 8 transaksi contoh audit forensik QRIS sesuai dataset skrip forensik."
            )
        )
    }

    suspend fun resetToDefaultSeed() = withContext(Dispatchers.IO) {
        transactionDao.deleteAll()
        seedInitialData()
    }

    suspend fun insertTransaction(transaction: QrisTransaction): QrisTransaction = withContext(Dispatchers.IO) {
        val entity = TransactionEntity.fromDomain(transaction)
        val id = transactionDao.insert(entity)
        val inserted = transaction.copy(id = id)

        // Evaluate with existing dataset to get accurate flags
        val currentAll = transactionDao.getAllTransactionsList().map { it.toDomain() }
        val auditedList = ForensicAuditEngine.auditTransactions(currentAll)
        val audited = auditedList.find { it.rrn == inserted.rrn } ?: inserted

        val isHighRisk = audited.totalFraudScore >= 2
        val alertDesc = "Transaksi baru ${audited.rrn} (${audited.idKasir}) ditambahkan. Hasil: ${audited.kesimpulanAudit} (Skor: ${audited.totalFraudScore})"

        auditLogDao.insert(
            AuditLogEntity(
                auditorName = _activeAuditor.value.name,
                actionType = "ADD_TX",
                description = alertDesc,
                rrn = audited.rrn,
                isAlert = isHighRisk
            )
        )

        // Emit real-time alert
        addRealtimeAlert(
            RealtimeAlertItem(
                rrn = audited.rrn,
                title = if (isHighRisk) "🚨 FRAUD ALERT BARU" else "Data Transaksi Masuk",
                message = alertDesc,
                isHighRisk = isHighRisk,
                score = audited.totalFraudScore
            )
        )

        // Trigger system notification
        ForensicNotificationHelper.sendTransactionNotification(
            context = context,
            transaction = audited,
            actionType = "Transaksi Baru Masuk"
        )

        // Auto-sync to GitHub if configured
        if (_gitHubConfig.value.isConfigured && _gitHubConfig.value.autoSyncEnabled) {
            triggerBackgroundGitHubSync("Auto-sync: Penambahan transaksi ${audited.rrn} oleh ${_activeAuditor.value.name}")
        }

        // Auto-sync to Database Server if configured
        if (_serverConfig.value.isConfigured && _serverConfig.value.autoSyncEnabled) {
            triggerBackgroundServerSync()
        }

        audited
    }

    suspend fun updateTransaction(transaction: QrisTransaction) = withContext(Dispatchers.IO) {
        val entity = TransactionEntity.fromDomain(transaction)
        transactionDao.update(entity)

        val currentAll = transactionDao.getAllTransactionsList().map { it.toDomain() }
        val auditedList = ForensicAuditEngine.auditTransactions(currentAll)
        val audited = auditedList.find { it.rrn == transaction.rrn } ?: transaction

        val isHighRisk = audited.totalFraudScore >= 2
        val desc = "Pembaruan data transaksi ${audited.rrn}. Kesimpulan: ${audited.kesimpulanAudit}"

        auditLogDao.insert(
            AuditLogEntity(
                auditorName = _activeAuditor.value.name,
                actionType = "EDIT_TX",
                description = desc,
                rrn = audited.rrn,
                isAlert = isHighRisk
            )
        )

        addRealtimeAlert(
            RealtimeAlertItem(
                rrn = audited.rrn,
                title = "Pembaruan Transaksi",
                message = desc,
                isHighRisk = isHighRisk,
                score = audited.totalFraudScore
            )
        )

        ForensicNotificationHelper.sendTransactionNotification(
            context = context,
            transaction = audited,
            actionType = "Pembaruan Transaksi"
        )

        if (_gitHubConfig.value.isConfigured && _gitHubConfig.value.autoSyncEnabled) {
            triggerBackgroundGitHubSync("Auto-sync: Pembaruan transaksi ${audited.rrn} oleh ${_activeAuditor.value.name}")
        }

        if (_serverConfig.value.isConfigured && _serverConfig.value.autoSyncEnabled) {
            triggerBackgroundServerSync()
        }
    }

    suspend fun deleteTransaction(transaction: QrisTransaction) = withContext(Dispatchers.IO) {
        val entity = TransactionEntity.fromDomain(transaction)
        transactionDao.delete(entity)

        val desc = "Transaksi ${transaction.rrn} telah dihapus dari basis data audit."
        auditLogDao.insert(
            AuditLogEntity(
                auditorName = _activeAuditor.value.name,
                actionType = "DELETE_TX",
                description = desc,
                rrn = transaction.rrn
            )
        )

        addRealtimeAlert(
            RealtimeAlertItem(
                rrn = transaction.rrn,
                title = "Transaksi Dihapus",
                message = desc,
                isHighRisk = false,
                score = 0
            )
        )

        if (_gitHubConfig.value.isConfigured && _gitHubConfig.value.autoSyncEnabled) {
            triggerBackgroundGitHubSync("Auto-sync: Penghapusan transaksi ${transaction.rrn} oleh ${_activeAuditor.value.name}")
        }

        if (_serverConfig.value.isConfigured && _serverConfig.value.autoSyncEnabled) {
            triggerBackgroundServerSync()
        }
    }

    suspend fun updateInvestigation(rrn: String, status: String, notes: String) = withContext(Dispatchers.IO) {
        val entity = transactionDao.getTransactionByRrn(rrn) ?: return@withContext
        val updated = entity.copy(
            statusInvestigasi = status,
            catatanAuditor = notes,
            updatedAt = System.currentTimeMillis()
        )
        transactionDao.update(updated)

        val desc = "Status investigasi $rrn diubah menjadi '$status' oleh ${_activeAuditor.value.name}."
        auditLogDao.insert(
            AuditLogEntity(
                auditorName = _activeAuditor.value.name,
                actionType = "INVESTIGATE",
                description = desc,
                rrn = rrn
            )
        )

        if (_gitHubConfig.value.isConfigured && _gitHubConfig.value.autoSyncEnabled) {
            triggerBackgroundGitHubSync("Auto-sync: Pembaruan investigasi $rrn [$status]")
        }

        if (_serverConfig.value.isConfigured && _serverConfig.value.autoSyncEnabled) {
            triggerBackgroundServerSync()
        }
    }

    private fun addRealtimeAlert(alert: RealtimeAlertItem) {
        val current = _realtimeAlerts.value.toMutableList()
        current.add(0, alert)
        if (current.size > 50) {
            _realtimeAlerts.value = current.take(50)
        } else {
            _realtimeAlerts.value = current
        }
    }

    // =========================================================================
    // Server Database Link Operations
    // =========================================================================

    suspend fun testServerConnection(): Result<ServerTestResult> {
        return serverDatabaseService.testConnection(_serverConfig.value)
    }

    suspend fun syncNowToServer(): SyncResult = withContext(Dispatchers.IO) {
        val config = _serverConfig.value
        if (!config.isConfigured) {
            return@withContext SyncResult(false, message = "URL Server Database belum dikonfigurasi.")
        }

        _isSyncing.value = true
        try {
            val entities = transactionDao.getAllTransactionsList()
            val domainList = entities.map { it.toDomain() }
            val auditedList = ForensicAuditEngine.auditTransactions(domainList)

            val result = serverDatabaseService.syncTransactionsToServer(
                config = config,
                transactions = auditedList,
                auditorName = _activeAuditor.value.name
            )

            val updatedConfig = config.copy(
                lastSyncTime = System.currentTimeMillis(),
                lastSyncStatus = result.message,
                lastSyncSuccess = result.success
            )
            saveServerConfig(updatedConfig)

            auditLogDao.insert(
                AuditLogEntity(
                    auditorName = _activeAuditor.value.name,
                    actionType = "SYNC_SERVER",
                    description = if (result.success) "Sinkronisasi berhasil ke ${config.serverUrl}"
                    else "Gagal sinkronisasi server: ${result.message}",
                    isAlert = !result.success
                )
            )

            result
        } finally {
            _isSyncing.value = false
        }
    }

    private fun triggerBackgroundServerSync() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                syncNowToServer()
            } catch (_: Exception) {}
        }
    }

    fun saveServerConfig(config: ServerDatabaseConfig) {
        _serverConfig.value = config
        prefs.edit()
            .putString("server_url", config.serverUrl)
            .putString("server_api_key", config.apiKey)
            .putString("server_db_name", config.databaseName)
            .putBoolean("server_auto_sync", config.autoSyncEnabled)
            .putLong("server_last_sync_time", config.lastSyncTime)
            .putString("server_last_status", config.lastSyncStatus)
            .putBoolean("server_last_success", config.lastSyncSuccess)
            .apply()
    }

    private fun loadServerConfig(): ServerDatabaseConfig {
        return ServerDatabaseConfig(
            serverUrl = prefs.getString("server_url", "https://api.qris-audit.internal/v1/database") ?: "https://api.qris-audit.internal/v1/database",
            apiKey = prefs.getString("server_api_key", "") ?: "",
            databaseName = prefs.getString("server_db_name", "qris_forensic_db") ?: "qris_forensic_db",
            autoSyncEnabled = prefs.getBoolean("server_auto_sync", false),
            lastSyncTime = prefs.getLong("server_last_sync_time", 0L),
            lastSyncStatus = prefs.getString("server_last_status", "Belum disinkronkan") ?: "Belum disinkronkan",
            lastSyncSuccess = prefs.getBoolean("server_last_success", false)
        )
    }

    // =========================================================================
    // GitHub Sync Operations
    // =========================================================================

    suspend fun syncNowToGitHub(customMessage: String? = null): SyncResult = withContext(Dispatchers.IO) {
        val config = _gitHubConfig.value
        if (!config.isConfigured) {
            return@withContext SyncResult(false, message = "Konfigurasi GitHub belum lengkap")
        }

        _isSyncing.value = true
        try {
            val entities = transactionDao.getAllTransactionsList()
            val domainList = entities.map { it.toDomain() }
            val csvContent = ForensicAuditEngine.exportToCsv(domainList)

            val msg = customMessage ?: "Audit Forensik QRIS: Ekspor ${entities.size} transaksi oleh ${_activeAuditor.value.name}"
            val result = gitHubService.syncAuditCsv(config, csvContent, msg)

            val updatedConfig = config.copy(
                lastSyncTime = System.currentTimeMillis(),
                lastCommitSha = result.commitSha ?: config.lastCommitSha,
                lastSyncStatus = result.message,
                lastSyncSuccess = result.success
            )
            saveGitHubConfig(updatedConfig)

            auditLogDao.insert(
                AuditLogEntity(
                    auditorName = _activeAuditor.value.name,
                    actionType = "SYNC_GITHUB",
                    description = if (result.success) "Sinkronisasi berhasil ke ${config.owner}/${config.repo} (SHA: ${result.commitSha})"
                    else "Gagal sinkronisasi ke GitHub: ${result.message}",
                    isAlert = !result.success
                )
            )

            ForensicNotificationHelper.sendSyncNotification(
                context = context,
                title = if (result.success) "GitHub Sync Berhasil" else "GitHub Sync Gagal",
                message = result.message,
                isSuccess = result.success
            )

            result
        } finally {
            _isSyncing.value = false
        }
    }

    private fun triggerBackgroundGitHubSync(commitMessage: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                syncNowToGitHub(commitMessage)
            } catch (_: Exception) {}
        }
    }

    suspend fun testGitHubConnection(): Result<String> {
        return gitHubService.testConnection(_gitHubConfig.value)
    }

    fun saveGitHubConfig(config: GitHubConfig) {
        _gitHubConfig.value = config
        prefs.edit()
            .putString("gh_token", config.token)
            .putString("gh_owner", config.owner)
            .putString("gh_repo", config.repo)
            .putString("gh_file_path", config.filePath)
            .putString("gh_branch", config.branch)
            .putBoolean("gh_auto_sync", config.autoSyncEnabled)
            .putLong("gh_last_sync_time", config.lastSyncTime)
            .putString("gh_last_commit_sha", config.lastCommitSha)
            .putString("gh_last_status", config.lastSyncStatus)
            .putBoolean("gh_last_success", config.lastSyncSuccess)
            .apply()
    }

    private fun loadGitHubConfig(): GitHubConfig {
        return GitHubConfig(
            token = prefs.getString("gh_token", "") ?: "",
            owner = prefs.getString("gh_owner", "") ?: "",
            repo = prefs.getString("gh_repo", "") ?: "",
            filePath = prefs.getString("gh_file_path", "hasil_audit_forensik_qris.csv") ?: "hasil_audit_forensik_qris.csv",
            branch = prefs.getString("gh_branch", "main") ?: "main",
            autoSyncEnabled = prefs.getBoolean("gh_auto_sync", true),
            lastSyncTime = prefs.getLong("gh_last_sync_time", 0L),
            lastCommitSha = prefs.getString("gh_last_commit_sha", "") ?: "",
            lastSyncStatus = prefs.getString("gh_last_status", "Belum disinkronkan") ?: "Belum disinkronkan",
            lastSyncSuccess = prefs.getBoolean("gh_last_success", false)
        )
    }

    // =========================================================================
    // Language Settings (ID / EN)
    // =========================================================================

    fun setLanguage(language: AppLanguage) {
        _appLanguage.value = language
        prefs.edit().putString("app_language", language.code).apply()
    }

    private fun loadLanguage(): AppLanguage {
        val code = prefs.getString("app_language", AppLanguage.ID.code) ?: AppLanguage.ID.code
        return if (code == AppLanguage.EN.code) AppLanguage.EN else AppLanguage.ID
    }

    // =========================================================================
    // Auditor Authentication / Profile
    // =========================================================================

    fun switchAuditor(auditor: AuditorProfile) {
        _activeAuditor.value = auditor
        prefs.edit()
            .putString("active_auditor_id", auditor.id)
            .apply()

        CoroutineScope(Dispatchers.IO).launch {
            auditLogDao.insert(
                AuditLogEntity(
                    auditorName = auditor.name,
                    actionType = "AUTH_LOGIN",
                    description = "Auditor ${auditor.name} (${auditor.role}) berhasil masuk ke sistem audit forensik."
                )
            )
        }
    }

    private fun loadAuditor(): AuditorProfile {
        val savedId = prefs.getString("active_auditor_id", AuditorProfile.DEFAULT_AUDITORS.first().id)
        return AuditorProfile.DEFAULT_AUDITORS.find { it.id == savedId }
            ?: AuditorProfile.DEFAULT_AUDITORS.first()
    }
}
