package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.OnRiskHighRedContainer
import com.example.ui.theme.OnRiskMediumAmberContainer
import com.example.ui.theme.OnRiskSafeGreenContainer
import com.example.ui.theme.RiskHighRedContainer
import com.example.ui.theme.RiskMediumAmberContainer
import com.example.ui.theme.RiskSafeGreenContainer
import com.example.util.AppLanguage

@Composable
fun RiskBadge(
    conclusion: String,
    score: Int? = null,
    language: AppLanguage = AppLanguage.ID,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when {
        conclusion.contains("TINGGI", ignoreCase = true) -> {
            Triple(RiskHighRedContainer, OnRiskHighRedContainer, Icons.Default.Error)
        }
        conclusion.contains("Rendah", ignoreCase = true) || conclusion.contains("Pantau", ignoreCase = true) -> {
            Triple(RiskMediumAmberContainer, OnRiskMediumAmberContainer, Icons.Default.Warning)
        }
        else -> {
            Triple(RiskSafeGreenContainer, OnRiskSafeGreenContainer, Icons.Default.CheckCircle)
        }
    }

    val label = when {
        conclusion.contains("TINGGI", ignoreCase = true) -> {
            if (language == AppLanguage.EN) "HIGH RISK" else "RISIKO TINGGI"
        }
        conclusion.contains("Rendah", ignoreCase = true) -> {
            if (language == AppLanguage.EN) "LOW RISK" else "RISIKO RENDAH"
        }
        else -> {
            if (language == AppLanguage.EN) "SAFE / VALID" else "AMAN / VALID"
        }
    }

    val scoreLabel = if (language == AppLanguage.EN) "Score" else "Skor"

    Row(
        modifier = modifier
            .testTag("risk_badge_${label.lowercase().replace(" ", "_")}")
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (score != null) "$label ($scoreLabel $score)" else label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun IndicatorChip(
    type: String,
    language: AppLanguage = AppLanguage.ID,
    modifier: Modifier = Modifier
) {
    val (label, icon, color) = when (type) {
        "jam_tutup" -> Triple(
            if (language == AppLanguage.EN) "After Hours" else "Jam Tutup",
            Icons.Default.AccessTime,
            Color(0xFFEF4444)
        )
        "split_tx" -> Triple(
            "Split Tx",
            Icons.Default.CallSplit,
            Color(0xFFF97316)
        )
        "nominal_bulat" -> Triple(
            if (language == AppLanguage.EN) "Cash-Out Round" else "Gestun Bulat",
            Icons.Default.MonetizationOn,
            Color(0xFFEAB308)
        )
        "self_tx" -> Triple(
            if (language == AppLanguage.EN) "Cashier Self-Tx" else "Self-Tx Kasir",
            Icons.Default.AccountCircle,
            Color(0xFF8B5CF6)
        )
        "benford" -> Triple(
            if (language == AppLanguage.EN) "Benford Anomaly" else "Anomali Benford",
            Icons.Default.Numbers,
            Color(0xFFEC4899)
        )
        else -> Triple(type, Icons.Default.Warning, MaterialTheme.colorScheme.primary)
    }

    Row(
        modifier = modifier
            .testTag("indicator_chip_$type")
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
