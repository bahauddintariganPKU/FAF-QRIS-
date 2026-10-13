package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QrisTransaction
import com.example.ui.components.TransactionItemCard
import com.example.util.AppLanguage
import com.example.util.AuditTranslations

@Composable
fun TransactionsScreen(
    transactions: List<QrisTransaction>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedRiskFilter: String,
    onRiskFilterChange: (String) -> Unit,
    selectedIndicatorFilter: String?,
    onIndicatorFilterChange: (String?) -> Unit,
    onTransactionClick: (QrisTransaction) -> Unit,
    onAddTransactionClick: () -> Unit,
    onSimulateScenario: (Int) -> Unit,
    language: AppLanguage = AppLanguage.ID,
    modifier: Modifier = Modifier
) {
    var showSimMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = AuditTranslations.addTransaction(language))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("screen_transactions")
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Search Bar & Simulation Dropdown Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(AuditTranslations.searchPlaceholder(language), fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Cari", modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_transactions_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Box {
                    IconButton(
                        onClick = { showSimMenu = true },
                        modifier = Modifier.testTag("btn_tx_simulate_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Simulasi Transaksi",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showSimMenu,
                        onDismissRequest = { showSimMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (language == AppLanguage.EN) "Simulate: Normal Trx" else "Simulasi: Trx Wajar") },
                            onClick = {
                                showSimMenu = false
                                onSimulateScenario(0)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (language == AppLanguage.EN) "Simulate: After Hours (23:40)" else "Simulasi: Jam Tutup (23:40)") },
                            onClick = {
                                showSimMenu = false
                                onSimulateScenario(1)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (language == AppLanguage.EN) "Simulate: Split Tx (< 5 mnt)" else "Simulasi: Split Tx (< 5 mnt)") },
                            onClick = {
                                showSimMenu = false
                                onSimulateScenario(2)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (language == AppLanguage.EN) "Simulate: Cash-out / Round (>= 1M)" else "Simulasi: Gestun / Bulat (>= 1jt)") },
                            onClick = {
                                showSimMenu = false
                                onSimulateScenario(3)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (language == AppLanguage.EN) "Simulate: Cashier Self-Tx" else "Simulasi: Self-Tx Kasir") },
                            onClick = {
                                showSimMenu = false
                                onSimulateScenario(4)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (language == AppLanguage.EN) "Simulate: Benford Anomaly" else "Simulasi: Anomali Benford") },
                            onClick = {
                                showSimMenu = false
                                onSimulateScenario(5)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Risk Filter Chips Row
            val riskOptions = if (language == AppLanguage.EN) {
                listOf("All", "High Risk", "Low Risk", "Safe")
            } else {
                listOf("Semua", "Risiko Tinggi", "Risiko Rendah", "Aman")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                riskOptions.forEach { option ->
                    val isSelected = selectedRiskFilter == option ||
                        (selectedRiskFilter == "Semua" && option == "All") ||
                        (selectedRiskFilter == "All" && option == "Semua") ||
                        (selectedRiskFilter == "Risiko Tinggi" && option == "High Risk") ||
                        (selectedRiskFilter == "High Risk" && option == "Risiko Tinggi") ||
                        (selectedRiskFilter == "Risiko Rendah" && option == "Low Risk") ||
                        (selectedRiskFilter == "Low Risk" && option == "Risiko Rendah") ||
                        (selectedRiskFilter == "Aman" && option == "Safe") ||
                        (selectedRiskFilter == "Safe" && option == "Aman")

                    FilterChip(
                        selected = isSelected,
                        onClick = { onRiskFilterChange(option) },
                        label = { Text(option, fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_chip_${option.lowercase().replace(" ", "_")}")
                    )
                }

                // Indicator filter clear chip if active
                if (selectedIndicatorFilter != null) {
                    FilterChip(
                        selected = true,
                        onClick = { onIndicatorFilterChange(null) },
                        label = { Text("Indikator: $selectedIndicatorFilter ✕", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_chip_clear_indicator")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Count summary header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.EN)
                        "Transactions List (${transactions.size})"
                    else
                        "Daftar Transaksi (${transactions.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (language == AppLanguage.EN) "Tap card for forensic audit details" else "Klik kartu untuk rincian formula",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == AppLanguage.EN) "No transactions match current filter" else "Tidak ada transaksi yang cocok dengan filter",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { "${it.id}_${it.rrn}" }) { tx ->
                        TransactionItemCard(
                            transaction = tx,
                            language = language,
                            onClick = { onTransactionClick(tx) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}
