package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.AuditLogEntity
import com.example.model.AuditorProfile
import com.example.ui.theme.CodeFontFamily
import com.example.util.AppLanguage
import com.example.util.AuditTranslations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditorProfileScreen(
    activeAuditor: AuditorProfile,
    auditLogs: List<AuditLogEntity>,
    language: AppLanguage = AppLanguage.ID,
    onSwitchAuditor: (AuditorProfile) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSwitchDialog by remember { mutableStateOf(false) }
    val timeFormat = remember(language) {
        if (language == AppLanguage.EN) {
            SimpleDateFormat("MM/dd HH:mm:ss", Locale.US)
        } else {
            SimpleDateFormat("dd/MM HH:mm:ss", Locale("id", "ID"))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_auditor_profile")
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title
        Text(
            text = AuditTranslations.auditorProfileTitle(language),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (language == AppLanguage.EN)
                "Forensic Access Control, Language & Audit Trail"
            else
                "Akses Kontrol Forensik, Bahasa & Rekam Jejak Audit",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Active Auditor Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_active_auditor"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(activeAuditor.avatarColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeAuditor.avatarInitials,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeAuditor.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = activeAuditor.role,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "NIP: ${activeAuditor.nip}",
                            fontSize = 11.sp,
                            fontFamily = CodeFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = activeAuditor.email,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.EN) "Active Session (PIN Verified)" else "Sesi Otentikasi Aktif (PIN Terverifikasi)",
                            fontSize = 11.sp,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedButton(
                        onClick = { showSwitchDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_open_switch_auditor")
                    ) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AuditTranslations.switchAuditor(language), fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // =========================================================================
        // LANGUAGE SETTING CARD (BILINGUAL TOGGLE: ID / EN)
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_language_selector"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = AuditTranslations.languageLabel(language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = AuditTranslations.languageSubtitle(language),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Indonesian Button
                    val isId = language == AppLanguage.ID
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isId) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = if (isId) 2.dp else 1.dp,
                                color = if (isId) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSetLanguage(AppLanguage.ID) }
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                            .testTag("btn_lang_id"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🇮🇩", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Indonesia",
                                fontSize = 12.sp,
                                fontWeight = if (isId) FontWeight.Bold else FontWeight.Medium,
                                color = if (isId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // English Button
                    val isEn = language == AppLanguage.EN
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isEn) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = if (isEn) 2.dp else 1.dp,
                                color = if (isEn) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSetLanguage(AppLanguage.EN) }
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                            .testTag("btn_lang_en"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🇬🇧", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "English",
                                fontSize = 12.sp,
                                fontWeight = if (isEn) FontWeight.Bold else FontWeight.Medium,
                                color = if (isEn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Audit Trail Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AuditTranslations.auditTrail(language),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${auditLogs.size} ${if (language == AppLanguage.EN) "Activities" else "Aktivitas"}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Audit Trail List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(auditLogs, key = { it.id }) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audit_log_item_${log.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (log.isAlert) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = log.auditorName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (log.actionType) {
                                                "ADD_TX" -> Color(0xFFE0F2FE)
                                                "EDIT_TX" -> Color(0xFFFEF3C7)
                                                "DELETE_TX" -> Color(0xFFFEE2E2)
                                                "SYNC_SERVER" -> Color(0xFFDCFCE7)
                                                "SYNC_GITHUB" -> Color(0xFFE0E7FF)
                                                "INVESTIGATE" -> Color(0xFFEDE9FE)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = log.actionType,
                                        fontSize = 9.sp,
                                        fontFamily = CodeFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = when (log.actionType) {
                                            "ADD_TX" -> Color(0xFF0369A1)
                                            "EDIT_TX" -> Color(0xFFB45309)
                                            "DELETE_TX" -> Color(0xFFB91C1C)
                                            "SYNC_SERVER" -> Color(0xFF15803D)
                                            "SYNC_GITHUB" -> Color(0xFF4338CA)
                                            "INVESTIGATE" -> Color(0xFF6D28D9)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }

                            Text(
                                text = timeFormat.format(Date(log.timestamp)),
                                fontSize = 10.sp,
                                fontFamily = CodeFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showSwitchDialog) {
        SwitchAuditorDialog(
            currentAuditor = activeAuditor,
            language = language,
            onDismiss = { showSwitchDialog = false },
            onSelectAuditor = { selected ->
                onSwitchAuditor(selected)
                showSwitchDialog = false
            }
        )
    }
}

@Composable
private fun SwitchAuditorDialog(
    currentAuditor: AuditorProfile,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSelectAuditor: (AuditorProfile) -> Unit
) {
    var selectedToSwitch by remember { mutableStateOf<AuditorProfile?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = AuditTranslations.switchAuditor(language),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (language == AppLanguage.EN)
                        "Select auditor account and enter 4-digit PIN."
                    else
                        "Pilih akun auditor dan masukkan PIN 4-digit.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                AuditorProfile.DEFAULT_AUDITORS.forEach { auditor ->
                    val isCurrent = auditor.id == currentAuditor.id
                    val isSelected = selectedToSwitch?.id == auditor.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedToSwitch = auditor
                                pinError = null
                                pinInput = ""
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                isCurrent -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(auditor.avatarColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = auditor.avatarInitials,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = auditor.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = auditor.role,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.EN) "Active" else "Aktif",
                                        fontSize = 10.sp,
                                        color = Color(0xFF166534),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedToSwitch != null && selectedToSwitch?.id != currentAuditor.id) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("PIN (${selectedToSwitch?.name})") },
                        placeholder = { Text("Default: 1234") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_auditor_pin"),
                        singleLine = true
                    )

                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = pinError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(AuditTranslations.cancel(language))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val target = selectedToSwitch
                            if (target == null || target.id == currentAuditor.id) {
                                onDismiss()
                                return@Button
                            }
                            if (pinInput != target.pin && pinInput != "1234") {
                                pinError = if (language == AppLanguage.EN) "Incorrect PIN (Default: 1234)" else "PIN keamanan salah (Gunakan default: 1234)"
                                return@Button
                            }
                            onSelectAuditor(target)
                        },
                        enabled = selectedToSwitch != null && selectedToSwitch?.id != currentAuditor.id,
                        modifier = Modifier.testTag("btn_confirm_switch_auditor")
                    ) {
                        Text(if (language == AppLanguage.EN) "Log in as Auditor" else "Masuk Sebagai Auditor")
                    }
                }
            }
        }
    }
}
