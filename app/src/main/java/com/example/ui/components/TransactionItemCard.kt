package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QrisTransaction
import com.example.ui.theme.CodeFontFamily
import com.example.util.AppLanguage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionItemCard(
    transaction: QrisTransaction,
    language: AppLanguage = AppLanguage.ID,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighRisk = transaction.totalFraudScore >= 2
    val isLowRisk = transaction.totalFraudScore == 1

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("tx_card_${transaction.rrn}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: RRN and Risk Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.rrn,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CodeFontFamily,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = transaction.waktu.takeLast(8), // HH:mm:ss
                        fontFamily = CodeFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                RiskBadge(
                    conclusion = transaction.kesimpulanAudit,
                    score = transaction.totalFraudScore,
                    language = language
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Parties & Nominal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = "Kasir",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = transaction.idKasir,
                            fontSize = 12.sp,
                            fontFamily = CodeFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Konsumen",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = transaction.userIdKonsumen,
                            fontSize = 12.sp,
                            fontFamily = CodeFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (transaction.statusInvestigasi != QrisTransaction.STATUS_BELUM_DITINJAU) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Status: ${transaction.statusInvestigasi}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatRupiah(transaction.nominal),
                        fontSize = 15.sp,
                        fontFamily = CodeFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighRisk) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Detail",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Indicator Flags list if any
            if (transaction.totalFraudScore > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (transaction.flagJamTutup == 1) {
                        IndicatorChip(type = "jam_tutup", language = language)
                    }
                    if (transaction.flagSplitTx == 1) {
                        IndicatorChip(type = "split_tx", language = language)
                    }
                    if (transaction.flagNominalBulat == 1) {
                        IndicatorChip(type = "nominal_bulat", language = language)
                    }
                    if (transaction.flagSelfTx == 1) {
                        IndicatorChip(type = "self_tx", language = language)
                    }
                    if (transaction.flagBenfordAnomali == 1) {
                        IndicatorChip(type = "benford", language = language)
                    }
                }
            }
        }
    }
}
