package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QrisTransaction
import com.example.ui.components.formatRupiah
import com.example.ui.theme.CodeFontFamily
import com.example.util.AppLanguage
import com.example.util.AuditTranslations

@Composable
fun ReportScreen(
    transactions: List<QrisTransaction>,
    csvContent: String,
    onPushToGitHubClick: () -> Unit,
    language: AppLanguage = AppLanguage.ID,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Laporan Utama, 1: Rincian Indikator
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copyNotice by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_report")
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title
        Text(
            text = if (language == AppLanguage.EN) "Forensic QRIS Audit Report" else "Laporan Forensik QRIS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (language == AppLanguage.EN)
                "Formatted according to hasil_audit_forensik_qris.csv specification"
            else
                "Output Sesuai Format Skrip hasil_audit_forensik_qris.csv",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Export & Push Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { shareCsvFile(context, csvContent) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_export_csv_share"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(AuditTranslations.exportCsv(language), fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = onPushToGitHubClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_report_push_github"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (language == AppLanguage.EN) "Server / Git" else "Server / Git", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(csvContent))
                    copyNotice = true
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_copy_csv_clipboard"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (copyNotice) {
                        if (language == AppLanguage.EN) "Copied!" else "Disalin!"
                    } else {
                        if (language == AppLanguage.EN) "Copy CSV" else "Salin CSV"
                    },
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = if (language == AppLanguage.EN) "Main Audit Report" else "Laporan Utama",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.testTag("tab_laporan_utama")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = if (language == AppLanguage.EN) "Indicator Breakdown" else "Rincian Indikator",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.testTag("tab_rincian_indikator")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Table Content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            val horizontalScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .horizontalScroll(horizontalScrollState)
            ) {
                if (selectedTab == 0) {
                    // TAB 1: LAPORAN UTAMA AUDIT FORENSIK QRIS
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell("RRN", width = 85.dp, isHeader = true)
                        TableCell(if (language == AppLanguage.EN) "Cashier ID" else "ID Kasir", width = 85.dp, isHeader = true)
                        TableCell(if (language == AppLanguage.EN) "Customer ID" else "User ID Konsumen", width = 120.dp, isHeader = true)
                        TableCell(if (language == AppLanguage.EN) "Amount" else "Nominal", width = 110.dp, isHeader = true)
                        TableCell(if (language == AppLanguage.EN) "Score" else "Skor", width = 55.dp, isHeader = true)
                        TableCell(if (language == AppLanguage.EN) "Audit Conclusion" else "Kesimpulan Audit", width = 210.dp, isHeader = true)
                    }

                    HorizontalDivider(color = DividerDefaults.color)

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(transactions) { index, tx ->
                            val isEven = index % 2 == 0
                            val rowBg = if (isEven) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            val isHighRisk = tx.totalFraudScore >= 2

                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(tx.rrn, width = 85.dp, isBold = true, isMonospace = true)
                                TableCell(tx.idKasir, width = 85.dp, isMonospace = true)
                                TableCell(tx.userIdKonsumen, width = 120.dp, isMonospace = true)
                                TableCell(formatRupiah(tx.nominal), width = 110.dp, isBold = true, isMonospace = true)
                                TableCell(
                                    "${tx.totalFraudScore}",
                                    width = 55.dp,
                                    textColor = if (isHighRisk) Color(0xFFEF4444) else if (tx.totalFraudScore == 1) Color(0xFFF59E0B) else Color(0xFF10B981),
                                    isBold = true,
                                    isMonospace = true
                                )
                                TableCell(
                                    tx.kesimpulanAudit,
                                    width = 210.dp,
                                    textColor = if (isHighRisk) Color(0xFFEF4444) else if (tx.totalFraudScore == 1) Color(0xFFF59E0B) else Color(0xFF10B981),
                                    isBold = isHighRisk
                                )
                            }
                        }
                    }
                } else {
                    // TAB 2: RINCIAN 5 PARAMETER INDIKATOR FRAUD
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell("RRN", width = 80.dp, isHeader = true)
                        TableCell(if (language == AppLanguage.EN) "After Hours" else "Jam Tutup", width = 85.dp, isHeader = true)
                        TableCell("Split Tx", width = 75.dp, isHeader = true)
                        TableCell(if (language == AppLanguage.EN) "Round Amt" else "Nom. Bulat", width = 85.dp, isHeader = true)
                        TableCell("Self-Tx", width = 75.dp, isHeader = true)
                        TableCell(if (language == AppLanguage.EN) "Benford" else "Benford", width = 75.dp, isHeader = true)
                        TableCell(if (language == AppLanguage.EN) "Digit" else "Digit 1", width = 60.dp, isHeader = true)
                        TableCell(if (language == AppLanguage.EN) "Score" else "Skor", width = 55.dp, isHeader = true)
                    }

                    HorizontalDivider(color = DividerDefaults.color)

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(transactions) { index, tx ->
                            val isEven = index % 2 == 0
                            val rowBg = if (isEven) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)

                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(tx.rrn, width = 80.dp, isBold = true, isMonospace = true)
                                TableFlagCell(tx.flagJamTutup, width = 85.dp)
                                TableFlagCell(tx.flagSplitTx, width = 75.dp)
                                TableFlagCell(tx.flagNominalBulat, width = 85.dp)
                                TableFlagCell(tx.flagSelfTx, width = 75.dp)
                                TableFlagCell(tx.flagBenfordAnomali, width = 75.dp)
                                TableCell("${tx.digitPertama}", width = 60.dp, isMonospace = true)
                                TableCell(
                                    "${tx.totalFraudScore}",
                                    width = 55.dp,
                                    textColor = if (tx.totalFraudScore >= 2) Color(0xFFEF4444) else Color(0xFF10B981),
                                    isBold = true,
                                    isMonospace = true
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TableCell(
    text: String,
    width: Dp,
    isHeader: Boolean = false,
    isBold: Boolean = false,
    isMonospace: Boolean = false,
    textColor: Color? = null
) {
    Text(
        text = text,
        fontSize = if (isHeader) 11.sp else 11.sp,
        fontFamily = if (isMonospace) CodeFontFamily else null,
        fontWeight = if (isHeader) FontWeight.Bold else if (isBold) FontWeight.SemiBold else FontWeight.Normal,
        color = textColor ?: if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp)
    )
}

@Composable
fun TableFlagCell(flag: Int, width: Dp) {
    val isFlagged = flag == 1
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = if (isFlagged) "1 (ALERT)" else "0",
            fontSize = 10.sp,
            fontFamily = CodeFontFamily,
            fontWeight = if (isFlagged) FontWeight.Bold else FontWeight.Normal,
            color = if (isFlagged) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun shareCsvFile(context: Context, csvContent: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, csvContent)
        type = "text/csv"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Ekspor / Bagikan CSV Hasil Audit Forensik QRIS")
    context.startActivity(shareIntent)
}
