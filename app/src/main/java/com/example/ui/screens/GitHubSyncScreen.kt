package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.github.GitHubConfig
import com.example.data.server.ServerDatabaseConfig
import com.example.data.server.ServerTestResult
import com.example.ui.components.EditServerLinkDialog
import com.example.ui.theme.CodeFontFamily
import com.example.util.AppLanguage
import com.example.util.AuditTranslations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GitHubSyncScreen(
    gitHubConfig: GitHubConfig,
    serverConfig: ServerDatabaseConfig,
    isSyncing: Boolean,
    language: AppLanguage = AppLanguage.ID,
    onSaveGitHubConfig: (GitHubConfig) -> Unit,
    onSyncGitHubNow: (String) -> Unit,
    onTestGitHubConnection: ((Result<String>) -> Unit) -> Unit,
    onSaveServerConfig: (ServerDatabaseConfig) -> Unit,
    onSyncServerNow: () -> Unit,
    onTestServerConnection: ((Result<ServerTestResult>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Server Database Link, 1: GitHub Repo
    var showEditDialog by remember { mutableStateOf(false) }

    val dateFormat = remember(language) {
        if (language == AppLanguage.EN) {
            SimpleDateFormat("MMM dd, yyyy, HH:mm:ss", Locale.US)
        } else {
            SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id", "ID"))
        }
    }

    if (showEditDialog) {
        EditServerLinkDialog(
            config = serverConfig,
            language = language,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                onSaveServerConfig(updated)
                showEditDialog = false
            },
            onTestConnection = { tempConfig, callback ->
                onTestServerConnection(callback)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("screen_sync_hub")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = if (language == AppLanguage.EN) "Database & Cloud Synchronization" else "Sinkronisasi Database & Server Cloud",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (language == AppLanguage.EN)
                "Manage live server database API links and automated GitHub repository push"
            else
                "Kelola tautan server database API real-time dan ekspor repositori GitHub",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Tabs: [Server Database Link | GitHub Repo]
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AuditTranslations.serverDatabaseTitle(language),
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                modifier = Modifier.testTag("tab_server_database")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GitHub Repo",
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                modifier = Modifier.testTag("tab_github_repo")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedSubTab == 0) {
            // =========================================================================
            // SUB-TAB 0: SERVER DATABASE LINK (CONFIGURABLE & EDITABLE)
            // =========================================================================
            ServerDatabaseLinkSection(
                config = serverConfig,
                isSyncing = isSyncing,
                language = language,
                dateFormat = dateFormat,
                onEditClick = { showEditDialog = true },
                onSave = onSaveServerConfig,
                onSyncNow = onSyncServerNow,
                onTestConnection = onTestServerConnection
            )
        } else {
            // =========================================================================
            // SUB-TAB 1: GITHUB REPO SYNC
            // =========================================================================
            GitHubRepoSection(
                config = gitHubConfig,
                isSyncing = isSyncing,
                language = language,
                dateFormat = dateFormat,
                onSave = onSaveGitHubConfig,
                onSyncNow = onSyncGitHubNow,
                onTestConnection = onTestGitHubConnection
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun ServerDatabaseLinkSection(
    config: ServerDatabaseConfig,
    isSyncing: Boolean,
    language: AppLanguage,
    dateFormat: SimpleDateFormat,
    onEditClick: () -> Unit,
    onSave: (ServerDatabaseConfig) -> Unit,
    onSyncNow: () -> Unit,
    onTestConnection: ((Result<ServerTestResult>) -> Unit) -> Unit
) {
    var urlInput by remember(config.serverUrl) { mutableStateOf(config.serverUrl) }
    var apiKeyInput by remember(config.apiKey) { mutableStateOf(config.apiKey) }
    var dbNameInput by remember(config.databaseName) { mutableStateOf(config.databaseName) }
    var autoSyncEnabled by remember(config.autoSyncEnabled) { mutableStateOf(config.autoSyncEnabled) }
    var showApiKey by remember { mutableStateOf(false) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(false) }

    // Live Server Link Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_server_link_status"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (config.lastSyncSuccess) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (config.lastSyncSuccess) Color(0xFF10B981) else MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = AuditTranslations.serverDatabaseTitle(language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = config.databaseName,
                            fontFamily = CodeFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Quick Edit Button
                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_quick_edit_link")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AuditTranslations.editServerLink(language), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            // Current Server Link Display
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = config.serverUrl,
                    fontFamily = CodeFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${AuditTranslations.lastSyncTime(language)}: ${if (config.lastSyncTime > 0) dateFormat.format(Date(config.lastSyncTime)) else if (language == AppLanguage.EN) "Never" else "Belum pernah"}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${AuditTranslations.connectionStatus(language)}: ${config.lastSyncStatus}",
                fontSize = 11.sp,
                color = if (config.lastSyncSuccess) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Form: Edit & Configure Server Link
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (language == AppLanguage.EN) "Direct Server Endpoint Configuration" else "Konfigurasi Endpoint Server Database",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (language == AppLanguage.EN)
                    "You can edit the link below and test live ping before saving."
                else
                    "Anda dapat mengedit link URL di bawah ini dan menguji koneksi langsung sebelum menyimpan.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // URL input
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text(AuditTranslations.serverUrlLabel(language)) },
                placeholder = { Text("https://api.yourserver.com/v1/database") },
                leadingIcon = { Icon(imageVector = Icons.Default.Link, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_form_server_url")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Database / Collection Name
            OutlinedTextField(
                value = dbNameInput,
                onValueChange = { dbNameInput = it },
                label = { Text(AuditTranslations.databaseNameLabel(language)) },
                placeholder = { Text("qris_forensic_db") },
                leadingIcon = { Icon(imageVector = Icons.Default.Storage, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_form_db_name")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // API Key
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text(AuditTranslations.apiKeyLabel(language)) },
                placeholder = { Text("Bearer secret_key_...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Key, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_form_server_key")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-Sync Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AuditTranslations.autoSyncLabel(language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = { autoSyncEnabled = it },
                    modifier = Modifier.testTag("switch_form_server_autosync")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Test Connection Button
            OutlinedButton(
                onClick = {
                    isTestingConnection = true
                    testResultText = null
                    val tempConfig = config.copy(
                        serverUrl = urlInput.trim(),
                        apiKey = apiKeyInput.trim(),
                        databaseName = dbNameInput.trim()
                    )
                    onSave(tempConfig) // save temporarily to test
                    onTestConnection { result ->
                        isTestingConnection = false
                        result.onSuccess {
                            isTestSuccess = it.success
                            testResultText = it.message
                        }.onFailure {
                            isTestSuccess = false
                            testResultText = it.message ?: "Koneksi gagal"
                        }
                    }
                },
                enabled = !isTestingConnection && urlInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_form_test_server")
            ) {
                if (isTestingConnection) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(AuditTranslations.testConnection(language), fontSize = 13.sp)
            }

            testResultText?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTestSuccess) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isTestSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isTestSuccess) Color(0xFF047857) else Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            fontSize = 11.sp,
                            color = if (isTestSuccess) Color(0xFF065F46) else Color(0xFF991B1B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Save Config Button
            Button(
                onClick = {
                    val updated = config.copy(
                        serverUrl = urlInput.trim(),
                        apiKey = apiKeyInput.trim(),
                        databaseName = dbNameInput.trim(),
                        autoSyncEnabled = autoSyncEnabled
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_save_server_config")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(AuditTranslations.save(language), fontSize = 13.sp)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Action Card: Push / Sync Now to Server Database
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AuditTranslations.syncNow(language),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (language == AppLanguage.EN)
                    "Transmit all audited QRIS transactions with forensic fraud flags to the remote database link."
                else
                    "Kirim seluruh data transaksi audit QRIS beserta skor fraud forensik ke link database server.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSyncNow,
                enabled = !isSyncing && config.isConfigured,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_sync_server_now")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(if (language == AppLanguage.EN) "Syncing..." else "Mengirim Data...")
                } else {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(AuditTranslations.syncNow(language), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun GitHubRepoSection(
    config: GitHubConfig,
    isSyncing: Boolean,
    language: AppLanguage,
    dateFormat: SimpleDateFormat,
    onSave: (GitHubConfig) -> Unit,
    onSyncNow: (String) -> Unit,
    onTestConnection: ((Result<String>) -> Unit) -> Unit
) {
    var tokenInput by remember(config.token) { mutableStateOf(config.token) }
    var ownerInput by remember(config.owner) { mutableStateOf(config.owner) }
    var repoInput by remember(config.repo) { mutableStateOf(config.repo) }
    var branchInput by remember(config.branch) { mutableStateOf(config.branch) }
    var filePathInput by remember(config.filePath) { mutableStateOf(config.filePath) }
    var autoSyncEnabled by remember(config.autoSyncEnabled) { mutableStateOf(config.autoSyncEnabled) }

    var isTokenVisible by remember { mutableStateOf(false) }
    var commitMessageInput by remember {
        mutableStateOf(if (language == AppLanguage.EN) "QRIS Forensic Audit: Export latest audited records" else "Audit Forensik QRIS: Ekspor transaksi terbaru")
    }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    // Status Banner
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_github_status"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (config.lastSyncSuccess) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (config.lastSyncSuccess) Icons.Default.CheckCircle else Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = if (config.lastSyncSuccess) Color(0xFF047857) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (config.isConfigured) "${config.owner}/${config.repo}" else if (language == AppLanguage.EN) "Repository Not Linked" else "Belum Terhubung Repositori",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (config.lastSyncTime > 0)
                            "${AuditTranslations.lastSyncTime(language)}: ${dateFormat.format(Date(config.lastSyncTime))}"
                        else
                            config.lastSyncStatus,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Form: GitHub Settings
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "GitHub Personal Access Token (PAT)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = tokenInput,
                onValueChange = { tokenInput = it },
                label = { Text("Token (ghp_... / github_pat_...)") },
                trailingIcon = {
                    IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                        Icon(
                            imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = ownerInput,
                    onValueChange = { ownerInput = it },
                    label = { Text("Owner / Username") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = repoInput,
                    onValueChange = { repoInput = it },
                    label = { Text("Repository") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = branchInput,
                    onValueChange = { branchInput = it },
                    label = { Text("Branch") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = filePathInput,
                    onValueChange = { filePathInput = it },
                    label = { Text("CSV File Path") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Test connection
            OutlinedButton(
                onClick = {
                    isTestingConnection = true
                    testResultText = null
                    val tempConfig = config.copy(
                        token = tokenInput.trim(),
                        owner = ownerInput.trim(),
                        repo = repoInput.trim(),
                        branch = branchInput.trim(),
                        filePath = filePathInput.trim()
                    )
                    onSave(tempConfig)
                    onTestConnection { result ->
                        isTestingConnection = false
                        result.onSuccess {
                            testResultText = "✅ $it"
                        }.onFailure {
                            testResultText = "❌ ${it.message ?: "Koneksi gagal"}"
                        }
                    }
                },
                enabled = !isTestingConnection && tokenInput.isNotBlank() && ownerInput.isNotBlank() && repoInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTestingConnection) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Test Connection", fontSize = 13.sp)
            }

            testResultText?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val updated = config.copy(
                        token = tokenInput.trim(),
                        owner = ownerInput.trim(),
                        repo = repoInput.trim(),
                        branch = branchInput.trim(),
                        filePath = filePathInput.trim(),
                        autoSyncEnabled = autoSyncEnabled
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(AuditTranslations.save(language), fontSize = 13.sp)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Commit & Sync Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (language == AppLanguage.EN) "Commit & Push Audit CSV" else "Commit & Push CSV Audit",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = commitMessageInput,
                onValueChange = { commitMessageInput = it },
                label = { Text("Commit Message") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onSyncNow(commitMessageInput.trim()) },
                enabled = !isSyncing && config.isConfigured,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == AppLanguage.EN) "Committing..." else "Mengunggah...")
                } else {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == AppLanguage.EN) "Push to GitHub" else "Kirim ke GitHub", fontSize = 13.sp)
                }
            }
        }
    }
}
