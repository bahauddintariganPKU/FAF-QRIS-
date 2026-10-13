package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import com.example.data.server.ServerDatabaseConfig
import com.example.data.server.ServerTestResult
import com.example.ui.theme.CodeFontFamily
import com.example.util.AppLanguage
import com.example.util.AuditTranslations

@Composable
fun EditServerLinkDialog(
    config: ServerDatabaseConfig,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (ServerDatabaseConfig) -> Unit,
    onTestConnection: (ServerDatabaseConfig, (Result<ServerTestResult>) -> Unit) -> Unit
) {
    var urlInput by remember(config.serverUrl) { mutableStateOf(config.serverUrl) }
    var apiKeyInput by remember(config.apiKey) { mutableStateOf(config.apiKey) }
    var dbNameInput by remember(config.databaseName) { mutableStateOf(config.databaseName) }
    var autoSyncInput by remember(config.autoSyncEnabled) { mutableStateOf(config.autoSyncEnabled) }
    var showApiKey by remember { mutableStateOf(false) }

    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<ServerTestResult?>(null) }
    var testError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_edit_server_link")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = AuditTranslations.editServerLink(language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (language == AppLanguage.EN) "Configure live database API link" else "Konfigurasi tautan database aktif",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Server URL input
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text(AuditTranslations.serverUrlLabel(language)) },
                    placeholder = { Text("https://api.yourdomain.com/v1/qris") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_server_url")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Database name
                OutlinedTextField(
                    value = dbNameInput,
                    onValueChange = { dbNameInput = it },
                    label = { Text(AuditTranslations.databaseNameLabel(language)) },
                    placeholder = { Text("qris_audit_db") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_server_db_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // API Key / Token input
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text(AuditTranslations.apiKeyLabel(language)) },
                    placeholder = { Text("Bearer eyJhbGciOi...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null)
                    },
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
                        .testTag("input_server_api_key")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Auto Sync Toggle
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AuditTranslations.autoSyncLabel(language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (language == AppLanguage.EN) "Push audit logs on every transaction" else "Kirim log audit setiap ada perubahan",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoSyncInput,
                            onCheckedChange = { autoSyncInput = it },
                            modifier = Modifier.testTag("switch_server_auto_sync")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Test Connection Button & Status
                OutlinedButton(
                    onClick = {
                        isTesting = true
                        testResult = null
                        testError = null
                        val tempConfig = config.copy(
                            serverUrl = urlInput.trim(),
                            apiKey = apiKeyInput.trim(),
                            databaseName = dbNameInput.trim()
                        )
                        onTestConnection(tempConfig) { result ->
                            isTesting = false
                            result.onSuccess {
                                testResult = it
                            }.onFailure {
                                testError = it.message ?: "Koneksi gagal"
                            }
                        }
                    },
                    enabled = !isTesting && urlInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_test_server_link")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(AuditTranslations.testConnection(language), fontSize = 13.sp)
                }

                // Test Result Feedback
                testResult?.let { res ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (res.success) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (res.success) Color(0xFF047857) else Color(0xFFB45309),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = res.message,
                                fontSize = 12.sp,
                                color = if (res.success) Color(0xFF065F46) else Color(0xFF92400E)
                            )
                        }
                    }
                }

                testError?.let { err ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = err,
                                fontSize = 12.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_cancel_server_dialog")
                    ) {
                        Text(AuditTranslations.cancel(language))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val updated = config.copy(
                                serverUrl = urlInput.trim(),
                                apiKey = apiKeyInput.trim(),
                                databaseName = dbNameInput.trim(),
                                autoSyncEnabled = autoSyncInput
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("btn_save_server_dialog")
                    ) {
                        Text(AuditTranslations.save(language))
                    }
                }
            }
        }
    }
}
