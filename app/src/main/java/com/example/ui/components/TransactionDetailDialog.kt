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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.QrisTransaction
import com.example.ui.theme.CodeFontFamily
import com.example.util.AppLanguage
import com.example.util.AuditTranslations

@Composable
fun TransactionDetailDialog(
    transaction: QrisTransaction,
    language: AppLanguage = AppLanguage.ID,
    onDismiss: () -> Unit,
    onUpdateStatus: (status: String, notes: String) -> Unit,
    onDelete: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(transaction.statusInvestigasi) }
    var notesText by remember { mutableStateOf(transaction.catatanAuditor) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("dialog_transaction_detail"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AuditTranslations.detailTitle(language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "RRN: ${transaction.rrn} • ${transaction.waktu}",
                            fontFamily = CodeFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_detail")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Badge & Nominal Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.EN) "Transaction Amount" else "Nominal Transaksi",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            RiskBadge(
                                conclusion = transaction.kesimpulanAudit,
                                score = transaction.totalFraudScore,
                                language = language
                            )
                        }

                        Text(
                            text = formatRupiah(transaction.nominal),
                            fontSize = 24.sp,
                            fontFamily = CodeFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (transaction.totalFraudScore >= 2) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Transaction Attributes
                Text(
                    text = if (language == AppLanguage.EN) "Transaction Parameters" else "Parameter Transaksi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        DetailAttrRow(if (language == AppLanguage.EN) "Cashier ID" else "ID Kasir", transaction.idKasir)
                        DetailAttrRow(if (language == AppLanguage.EN) "Customer User ID" else "User ID Konsumen", transaction.userIdKonsumen)
                        DetailAttrRow(if (language == AppLanguage.EN) "Transaction Time" else "Waktu Transaksi", transaction.waktu)
                        DetailAttrRow(if (language == AppLanguage.EN) "Hour (24H)" else "Jam (24H)", "${transaction.jam}:00 WIB")
                        DetailAttrRow(if (language == AppLanguage.EN) "First Digit (Benford)" else "Digit Pertama (Benford)", "${transaction.digitPertama}")
                        DetailAttrRow(
                            if (language == AppLanguage.EN) "Self-Transaction Collusion" else "Kasir = Konsumen (Self-Tx)",
                            if (transaction.idKasir == transaction.userIdKonsumen) {
                                if (language == AppLanguage.EN) "YES (SUSPICIOUS)" else "YA (MENCURIGAKAN)"
                            } else {
                                if (language == AppLanguage.EN) "No (Normal)" else "Tidak (Normal)"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5 Forensic Formula Audit Rules Breakdown
                Text(
                    text = if (language == AppLanguage.EN) "5 Forensic Audit Detection Rules" else "Hasil Evaluasi 5 Aturan Forensik",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                FormulaInspectionRow(
                    ruleName = if (language == AppLanguage.EN) "1. Business Hours (23:00 - 06:00)" else "1. Jam Tutup Toko (23:00 - 06:00)",
                    isTriggered = transaction.flagJamTutup == 1,
                    detail = if (transaction.flagJamTutup == 1)
                        if (language == AppLanguage.EN) "Transaction executed at ${transaction.jam}:00 outside standard operational hours."
                        else "Transaksi terjadi pada jam ${transaction.jam}:00 saat toko tutup."
                    else
                        if (language == AppLanguage.EN) "Within standard operating hours." else "Dalam jam operasional wajar."
                )

                FormulaInspectionRow(
                    ruleName = if (language == AppLanguage.EN) "2. Split Transaction (<= 5 mins)" else "2. Split Transaction (<= 5 Menit)",
                    isTriggered = transaction.flagSplitTx == 1,
                    detail = if (transaction.flagSplitTx == 1)
                        if (language == AppLanguage.EN) "Identical amount repeated by customer within 5 minutes."
                        else "Pengulangan nominal sama oleh konsumen dalam jeda waktu <= 5 menit."
                    else
                        if (language == AppLanguage.EN) "No split transaction pattern detected." else "Tidak ada pola pemecahan transaksi."
                )

                FormulaInspectionRow(
                    ruleName = if (language == AppLanguage.EN) "3. Large Round Amount Cash-Out" else "3. Nominal Bulat Besar (Gestun)",
                    isTriggered = transaction.flagNominalBulat == 1,
                    detail = if (transaction.flagNominalBulat == 1)
                        if (language == AppLanguage.EN) "Large round amount (${formatRupiah(transaction.nominal)} >= Rp 1.000.000) indicating cash-out."
                        else "Nominal bulat besar (${formatRupiah(transaction.nominal)} >= Rp 1.000.000) indikasi gestun."
                    else
                        if (language == AppLanguage.EN) "Retail purchase amount pattern." else "Nominal ritel belanja normal."
                )

                FormulaInspectionRow(
                    ruleName = if (language == AppLanguage.EN) "4. Cashier Self-Transaction" else "4. Kasir Pakai Akun Sendiri",
                    isTriggered = transaction.flagSelfTx == 1,
                    detail = if (transaction.flagSelfTx == 1)
                        if (language == AppLanguage.EN) "Cashier '${transaction.idKasir}' processed transaction for own consumer account."
                        else "Kasir '${transaction.idKasir}' bertransaksi sebagai konsumen akunnya sendiri."
                    else
                        if (language == AppLanguage.EN) "Distinct cashier and consumer identities." else "Kasir dan konsumen terpisah wajar."
                )

                FormulaInspectionRow(
                    ruleName = if (language == AppLanguage.EN) "5. Benford's Law Anomaly" else "5. Anomali Hukum Benford",
                    isTriggered = transaction.flagBenfordAnomali == 1,
                    detail = if (transaction.flagBenfordAnomali == 1)
                        if (language == AppLanguage.EN) "Amount starts with digit ${transaction.digitPertama} (> Rp 500.000), violating natural frequency curve."
                        else "Nominal diawali digit ${transaction.digitPertama} (> 500rb), menyimpang dari kurva distribusi alami."
                    else
                        if (language == AppLanguage.EN) "First digit conforms to statistical frequency curve." else "Digit pertama sesuai distribusi wajar."
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Investigation Status & Notes
                Text(
                    text = AuditTranslations.investigationStatus(language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                val statusOptions = listOf(
                    QrisTransaction.STATUS_BELUM_DITINJAU,
                    QrisTransaction.STATUS_SEDANG_DIINVESTIGASI,
                    QrisTransaction.STATUS_FRAUD,
                    QrisTransaction.STATUS_SELESAI
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    statusOptions.forEach { status ->
                        val isSelected = selectedStatus == status
                        OutlinedButton(
                            onClick = { selectedStatus = status },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Text(
                                text = status.split(" ").first(),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text(AuditTranslations.auditorNotes(language)) },
                    placeholder = { Text(if (language == AppLanguage.EN) "Enter forensic findings, interviews or evidence..." else "Masukkan bukti temuan, hasil wawancara kasir, atau tindak lanjut...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auditor_notes"),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showConfirmDelete = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("btn_delete_tx")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AuditTranslations.deleteTransaction(language), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onUpdateStatus(selectedStatus, notesText)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("btn_save_audit_status")
                    ) {
                        Icon(imageVector = Icons.Default.EditNote, contentDescription = "Simpan", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AuditTranslations.save(language))
                    }
                }

                if (showConfirmDelete) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (language == AppLanguage.EN) "Confirm Delete Transaction" else "Konfirmasi Hapus Transaksi",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (language == AppLanguage.EN)
                                    "Are you sure you want to delete transaction ${transaction.rrn} from database?"
                                else
                                    "Apakah Anda yakin ingin menghapus transaksi ${transaction.rrn} dari basis data?",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        onDelete()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    modifier = Modifier.testTag("btn_confirm_delete")
                                ) {
                                    Text(if (language == AppLanguage.EN) "Yes, Delete" else "Ya, Hapus", fontSize = 12.sp)
                                }
                                OutlinedButton(onClick = { showConfirmDelete = false }) {
                                    Text(AuditTranslations.cancel(language), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailAttrRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontFamily = CodeFontFamily, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun FormulaInspectionRow(
    ruleName: String,
    isTriggered: Boolean,
    detail: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTriggered) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isTriggered) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isTriggered) Color(0xFFEF4444) else Color(0xFF10B981),
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ruleName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTriggered) Color(0xFF991B1B) else Color(0xFF065F46)
                    )
                    Text(
                        text = if (isTriggered) "FLAG: 1" else "FLAG: 0",
                        fontSize = 11.sp,
                        fontFamily = CodeFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isTriggered) Color(0xFFEF4444) else Color(0xFF10B981)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = detail,
                    fontSize = 11.sp,
                    color = if (isTriggered) Color(0xFFB91C1C) else Color(0xFF047857),
                    lineHeight = 15.sp
                )
            }
        }
    }
}
