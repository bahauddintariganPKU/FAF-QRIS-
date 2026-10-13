package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ForensicSummary
import com.example.ui.theme.CodeFontFamily
import com.example.util.AppLanguage
import com.example.util.AuditTranslations
import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace("Rp", "Rp ")
}

@Composable
fun SummaryMetricGrid(
    summary: ForensicSummary,
    language: AppLanguage = AppLanguage.ID,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Main Total Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("summary_main_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = AuditTranslations.totalTransactions(language),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${summary.totalTransaksi} ${if (language == AppLanguage.EN) "Transactions" else "Transaksi"}",
                        fontSize = 22.sp,
                        fontFamily = CodeFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${AuditTranslations.totalTurnover(language)}: ${formatRupiah(summary.totalNominal)}",
                        fontSize = 12.sp,
                        fontFamily = CodeFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Transactions",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Three Risk Level Cards in a Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RiskMetricSmallCard(
                title = AuditTranslations.highRisk(language),
                count = summary.countRisikoTinggi,
                icon = Icons.Default.Error,
                accentColor = Color(0xFFEF4444),
                bgColor = Color(0xFFFEE2E2),
                modifier = Modifier.weight(1f)
            )

            RiskMetricSmallCard(
                title = AuditTranslations.lowRisk(language),
                count = summary.countRisikoRendah,
                icon = Icons.Default.Warning,
                accentColor = Color(0xFFF59E0B),
                bgColor = Color(0xFFFEF3C7),
                modifier = Modifier.weight(1f)
            )

            RiskMetricSmallCard(
                title = AuditTranslations.safe(language),
                count = summary.countAman,
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF10B981),
                bgColor = Color(0xFFD1FAE5),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RiskMetricSmallCard(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("metric_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$count",
                fontSize = 20.sp,
                fontFamily = CodeFontFamily,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
fun IndicatorBreakdownSection(
    summary: ForensicSummary,
    language: AppLanguage = AppLanguage.ID,
    onIndicatorClick: (String?) -> Unit,
    selectedIndicator: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("indicator_breakdown_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = AuditTranslations.anomalyDistribution(language),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (language == AppLanguage.EN) "Tap any indicator to filter transactions list" else "Ketuk indikator untuk menyaring daftar transaksi",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            IndicatorRowItem(
                label = AuditTranslations.indicatorJamTutup(language),
                count = summary.flagJamTutupCount,
                icon = Icons.Default.AccessTime,
                isSelected = selectedIndicator == "Jam Tutup" || selectedIndicator == "After Hours",
                onClick = { onIndicatorClick(if (selectedIndicator == "Jam Tutup") null else "Jam Tutup") }
            )

            IndicatorRowItem(
                label = AuditTranslations.indicatorSplitTx(language),
                count = summary.flagSplitTxCount,
                icon = Icons.Default.CallSplit,
                isSelected = selectedIndicator == "Split Tx",
                onClick = { onIndicatorClick(if (selectedIndicator == "Split Tx") null else "Split Tx") }
            )

            IndicatorRowItem(
                label = AuditTranslations.indicatorNominalBulat(language),
                count = summary.flagNominalBulatCount,
                icon = Icons.Default.MonetizationOn,
                isSelected = selectedIndicator == "Nominal Bulat" || selectedIndicator == "Round Amount",
                onClick = { onIndicatorClick(if (selectedIndicator == "Nominal Bulat") null else "Nominal Bulat") }
            )

            IndicatorRowItem(
                label = AuditTranslations.indicatorSelfTx(language),
                count = summary.flagSelfTxCount,
                icon = Icons.Default.AccountCircle,
                isSelected = selectedIndicator == "Self-Tx",
                onClick = { onIndicatorClick(if (selectedIndicator == "Self-Tx") null else "Self-Tx") }
            )

            IndicatorRowItem(
                label = AuditTranslations.indicatorBenford(language),
                count = summary.flagBenfordAnomaliCount,
                icon = Icons.Default.Numbers,
                isSelected = selectedIndicator == "Benford Anomali" || selectedIndicator == "Benford Anomaly",
                onClick = { onIndicatorClick(if (selectedIndicator == "Benford Anomali") null else "Benford Anomali") }
            )
        }
    }
}

@Composable
fun IndicatorRowItem(
    label: String,
    count: Int,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (count > 0) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count",
                fontSize = 11.sp,
                fontFamily = CodeFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
