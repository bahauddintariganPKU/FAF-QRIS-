package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.model.ForensicSummary
import com.example.model.QrisTransaction
import com.example.ui.components.BenfordAnalysisCard
import com.example.ui.components.IndicatorBreakdownSection
import com.example.ui.components.SummaryMetricGrid
import com.example.ui.components.formatRupiah
import com.example.ui.theme.CodeFontFamily
import com.example.util.AppLanguage
import com.example.util.AuditTranslations

@Composable
fun DashboardScreen(
    summary: ForensicSummary,
    highRiskTransactions: List<QrisTransaction>,
    language: AppLanguage = AppLanguage.ID,
    onNavigateToTransactions: (String?) -> Unit,
    onNavigateToGitHub: () -> Unit,
    onNavigateToReport: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onSimulateScenario: (Int) -> Unit,
    onResetData: () -> Unit,
    selectedIndicator: String?,
    onSelectIndicator: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSimulateMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_dashboard"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Dashboard Title Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (language == AppLanguage.EN) "Forensic Audit Overview" else "Ringkasan Audit Forensik",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = AuditTranslations.appSubtitle(language),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // High Risk Alert Banner (if any)
        if (summary.countRisikoTinggi > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alert_high_risk_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.EN)
                                    "⚠️ ALERT: ${summary.countRisikoTinggi} High-Risk Transactions"
                                else
                                    "⚠️ PERINGATAN: ${summary.countRisikoTinggi} Transaksi Berisiko Tinggi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = if (language == AppLanguage.EN)
                                    "Potential Fraud Exposure: ${formatRupiah(summary.nominalRisikoTinggi)}. Immediate forensic investigation required."
                                else
                                    "Potensi Paparan Fraud: ${formatRupiah(summary.nominalRisikoTinggi)}. Perlu investigasi forensik segera.",
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C),
                                lineHeight = 15.sp
                            )
                        }

                        Button(
                            onClick = { onNavigateToTransactions(if (language == AppLanguage.EN) "High Risk" else "Risiko Tinggi") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_investigate_now")
                        ) {
                            Text(if (language == AppLanguage.EN) "Review" else "Tinjau", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Metrics Grid (Total, High Risk, Low Risk, Safe)
        item {
            SummaryMetricGrid(summary = summary, language = language)
        }

        // Quick Actions Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == AppLanguage.EN) "Quick Audit Actions" else "Aksi Audit Cepat",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAddTransactionClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_add_tx"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (language == AppLanguage.EN) "Add Trx" else "Tambah Trx", fontSize = 11.sp)
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showSimulateMenu = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_quick_simulate_dropdown"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (language == AppLanguage.EN) "Simulate" else "Simulasi", fontSize = 11.sp)
                            }

                            DropdownMenu(
                                expanded = showSimulateMenu,
                                onDismissRequest = { showSimulateMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (language == AppLanguage.EN) "Normal Valid Trx" else "Trx Wajar (Normal)") },
                                    onClick = {
                                        showSimulateMenu = false
                                        onSimulateScenario(0)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (language == AppLanguage.EN) "Anomaly: After Hours (23:40)" else "Anomali: Jam Tutup (23:40)") },
                                    onClick = {
                                        showSimulateMenu = false
                                        onSimulateScenario(1)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (language == AppLanguage.EN) "Anomaly: Split Transaction" else "Anomali: Split Transaction (< 5 mnt)") },
                                    onClick = {
                                        showSimulateMenu = false
                                        onSimulateScenario(2)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (language == AppLanguage.EN) "Anomaly: Cash-out (Round Rp 1M+)" else "Anomali: Gestun / Bulat (>= 1 Juta)") },
                                    onClick = {
                                        showSimulateMenu = false
                                        onSimulateScenario(3)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (language == AppLanguage.EN) "Anomaly: Cashier Self-Tx" else "Anomali: Self-Tx (Kasir Sendiri)") },
                                    onClick = {
                                        showSimulateMenu = false
                                        onSimulateScenario(4)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (language == AppLanguage.EN) "Anomaly: Benford Digits (7/8/9)" else "Anomali: Hukum Benford (Digit 7/8/9)") },
                                    onClick = {
                                        showSimulateMenu = false
                                        onSimulateScenario(5)
                                    }
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateToGitHub,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_sync_github"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (language == AppLanguage.EN) "Server / Git" else "Server / Git", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Indicators Breakdown Section
        item {
            IndicatorBreakdownSection(
                summary = summary,
                language = language,
                selectedIndicator = selectedIndicator,
                onIndicatorClick = { indicator ->
                    onSelectIndicator(indicator)
                    if (indicator != null) {
                        onNavigateToTransactions(null)
                    }
                }
            )
        }

        // Benford's Law Chart
        item {
            BenfordAnalysisCard(
                distributions = summary.benfordDistributions,
                language = language
            )
        }

        // Reset Data option
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = onResetData,
                    modifier = Modifier.testTag("btn_reset_sample_data")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.EN) "Reset to 8-Transaction Seed Dataset" else "Reset ke Dataset 8 Transaksi Awal Skrip",
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
