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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.ForensicAuditEngine
import com.example.ui.theme.CodeFontFamily
import com.example.util.AppLanguage
import com.example.util.AuditTranslations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddTransactionDialog(
    suggestedRrn: String,
    language: AppLanguage = AppLanguage.ID,
    onDismiss: () -> Unit,
    onSave: (rrn: String, waktu: String, idKasir: String, userIdKonsumen: String, nominal: Long) -> Unit
) {
    var rrn by remember { mutableStateOf(suggestedRrn) }
    var waktu by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
    }
    var idKasir by remember { mutableStateOf("KASIR_01") }
    var userIdKonsumen by remember { mutableStateOf("CUST_88") }
    var nominalInput by remember { mutableStateOf("500000") }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Live preview of the transaction rules
    val parsedNominal by remember(nominalInput) {
        derivedStateOf { nominalInput.toLongOrNull() ?: 0L }
    }

    val previewFlags by remember(waktu, idKasir, userIdKonsumen, parsedNominal) {
        derivedStateOf {
            val hour = ForensicAuditEngine.getHourFromTimestamp(waktu)
            val flagJam = if (hour >= 23 || hour <= 5) 1 else 0
            val flagBulat = if (parsedNominal >= 1_000_000L && parsedNominal % 100_000L == 0L) 1 else 0
            val flagSelf = if (idKasir.trim().equals(userIdKonsumen.trim(), ignoreCase = true)) 1 else 0
            val firstDigit = ForensicAuditEngine.getFirstDigit(parsedNominal)
            val flagBenford = if (parsedNominal > 500_000L && firstDigit in listOf(7, 8, 9)) 1 else 0
            val score = flagJam + flagBulat + flagSelf + flagBenford
            Triple(score, firstDigit, listOf(flagJam, flagBulat, flagSelf, flagBenford))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("dialog_add_transaction"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == AppLanguage.EN) "Input New QRIS Transaction" else "Input Transaksi QRIS Baru",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (language == AppLanguage.EN)
                                "Instantly evaluated by real-time forensic engine"
                            else
                                "Akan langsung diaudit oleh mesin forensik real-time",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // RRN & Waktu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = rrn,
                        onValueChange = { rrn = it },
                        label = { Text("Kode RRN") },
                        placeholder = { Text("RRN_009") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_rrn"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = waktu,
                        onValueChange = { waktu = it },
                        label = { Text(if (language == AppLanguage.EN) "Timestamp" else "Waktu") },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("input_waktu"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Kasir & Konsumen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = idKasir,
                        onValueChange = { idKasir = it },
                        label = { Text(if (language == AppLanguage.EN) "Cashier ID" else "ID Kasir") },
                        placeholder = { Text("KASIR_01") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_id_kasir"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = userIdKonsumen,
                        onValueChange = { userIdKonsumen = it },
                        label = { Text(if (language == AppLanguage.EN) "Customer ID" else "User ID Konsumen") },
                        placeholder = { Text("CUST_88") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_user_id_konsumen"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Nominal
                OutlinedTextField(
                    value = nominalInput,
                    onValueChange = {
                        nominalInput = it.filter { ch -> ch.isDigit() }
                    },
                    label = { Text(if (language == AppLanguage.EN) "Amount (Rp)" else "Nominal Transaksi (Rp)") },
                    placeholder = { Text("1500000") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_nominal"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        if (parsedNominal > 0) {
                            Text(text = "Format: ${formatRupiah(parsedNominal)}", fontFamily = CodeFontFamily)
                        }
                    }
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real-time Forensic Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.EN) "Automated Audit Preview" else "Pratinjau Audit Forensik Otomatis",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val score = previewFlags.first
                            val conclusion = when {
                                score >= 2 -> "RISIKO TINGGI (Investigasi Segera)"
                                score == 1 -> "Risiko Rendah (Pantau)"
                                else -> "Aman / Valid"
                            }
                            RiskBadge(conclusion = conclusion, score = score, language = language)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${if (language == AppLanguage.EN) "First Digit" else "Digit Pertama"}: ${previewFlags.second} • ${if (language == AppLanguage.EN) "Triggered Flags" else "Indikator Aktif"}: " +
                                listOfNotNull(
                                    if (previewFlags.third[0] == 1) (if (language == AppLanguage.EN) "After Hours" else "Jam Tutup") else null,
                                    if (previewFlags.third[1] == 1) (if (language == AppLanguage.EN) "Round Amt" else "Gestun Bulat") else null,
                                    if (previewFlags.third[2] == 1) "Self-Tx" else null,
                                    if (previewFlags.third[3] == 1) (if (language == AppLanguage.EN) "Benford Anomaly" else "Anomali Benford") else null
                                ).ifEmpty { listOf(if (language == AppLanguage.EN) "None (Normal)" else "Tidak ada anomali mandiri") }.joinToString(", "),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(AuditTranslations.cancel(language))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (rrn.isBlank()) {
                                validationError = if (language == AppLanguage.EN) "RRN cannot be empty" else "Kode RRN tidak boleh kosong"
                                return@Button
                            }
                            if (parsedNominal <= 0) {
                                validationError = if (language == AppLanguage.EN) "Amount must be greater than 0" else "Nominal harus lebih besar dari 0"
                                return@Button
                            }
                            onSave(rrn, waktu, idKasir, userIdKonsumen, parsedNominal)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("btn_save_new_tx")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Simpan", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == AppLanguage.EN) "Audit & Save" else "Audit & Simpan")
                    }
                }
            }
        }
    }
}
