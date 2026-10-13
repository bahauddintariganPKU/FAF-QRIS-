package com.example.engine

import com.example.model.BenfordDistribution
import com.example.model.ForensicSummary
import com.example.model.QrisTransaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.log10

object ForensicAuditEngine {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun parseTimestamp(waktu: String): Calendar? {
        return try {
            val date = dateFormat.parse(waktu) ?: return null
            Calendar.getInstance().apply { time = date }
        } catch (_: Exception) {
            null
        }
    }

    fun getHourFromTimestamp(waktu: String): Int {
        val cal = parseTimestamp(waktu) ?: return 12
        return cal.get(Calendar.HOUR_OF_DAY)
    }

    fun getFirstDigit(nominal: Long): Int {
        if (nominal <= 0) return 0
        val s = nominal.toString()
        return s.firstOrNull()?.digitToIntOrNull() ?: 0
    }

    /**
     * Executes the digital forensic audit formulas corresponding to the Python script.
     * Evaluates:
     * 1. Flag_Jam_Tutup (23:00 - 05:00)
     * 2. Flag_Split_Tx (Same User, diff time <= 5 mins, identical nominal)
     * 3. Flag_Nominal_Bulat (Nominal >= 1,000,000 and multiple of 100,000)
     * 4. Flag_Self_Tx (ID_Kasir == User_ID_Konsumen)
     * 5. Flag_Benford_Anomali (Nominal > 500,000 and first digit in 7, 8, 9)
     */
    fun auditTransactions(rawList: List<QrisTransaction>): List<QrisTransaction> {
        if (rawList.isEmpty()) return emptyList()

        // 1. Group by userIdKonsumen and sort by Waktu to evaluate Split Transaction
        val sortedForSplit = rawList.sortedWith(
            compareBy<QrisTransaction> { it.userIdKonsumen }
                .thenBy { parseTimestamp(it.waktu)?.timeInMillis ?: 0L }
        )

        val splitTxMap = mutableMapOf<String, Pair<Int, Double?>>() // key: rrn -> (flagSplit, selisihMenit)

        val groupedByUser = sortedForSplit.groupBy { it.userIdKonsumen }
        for ((_, userTransactions) in groupedByUser) {
            for (i in userTransactions.indices) {
                val current = userTransactions[i]
                if (i > 0) {
                    val prev = userTransactions[i - 1]
                    val prevTime = parseTimestamp(prev.waktu)?.timeInMillis
                    val currTime = parseTimestamp(current.waktu)?.timeInMillis

                    if (prevTime != null && currTime != null) {
                        val diffMillis = currTime - prevTime
                        val diffMinutes = diffMillis / (1000.0 * 60.0)

                        val isSplit = (diffMinutes <= 5.0) && (current.nominal == prev.nominal)
                        splitTxMap[current.rrn] = Pair(if (isSplit) 1 else 0, diffMinutes)
                    } else {
                        splitTxMap[current.rrn] = Pair(0, null)
                    }
                } else {
                    splitTxMap[current.rrn] = Pair(0, null)
                }
            }
        }

        // 2. Evaluate all rules for each transaction
        return rawList.map { tx ->
            val hour = getHourFromTimestamp(tx.waktu)

            // Rule 1: Flag_Jam_Tutup: Waktu di luar jam operasional (23:00 - 05:00)
            val flagJamTutup = if (hour >= 23 || hour <= 5) 1 else 0

            // Rule 2: Flag_Split_Tx
            val splitData = splitTxMap[tx.rrn]
            val flagSplitTx = splitData?.first ?: 0
            val selisihMenit = splitData?.second

            // Rule 3: Flag_Nominal_Bulat: >= 1.000.000 dan kelipatan 100.000 (Gestun / Cashout)
            val flagNominalBulat = if (tx.nominal >= 1_000_000L && tx.nominal % 100_000L == 0L) 1 else 0

            // Rule 4: Flag_Self_Tx: Kasir pakai akun sendiri (ID_Kasir == User_ID_Konsumen)
            val flagSelfTx = if (tx.idKasir.trim().equals(tx.userIdKonsumen.trim(), ignoreCase = true)) 1 else 0

            // Rule 5: Flag_Benford_Anomali: Nominal > 500.000 dan digit pertama 7, 8, atau 9
            val digitPertama = getFirstDigit(tx.nominal)
            val flagBenford = if (tx.nominal > 500_000L && (digitPertama in listOf(7, 8, 9))) 1 else 0

            // Total Score
            val totalScore = flagJamTutup + flagSplitTx + flagNominalBulat + flagSelfTx + flagBenford

            // Kesimpulan Audit
            val kesimpulan = when {
                totalScore >= 2 -> QrisTransaction.KESIMPULAN_RISIKO_TINGGI
                totalScore == 1 -> QrisTransaction.KESIMPULAN_RISIKO_RENDAH
                else -> QrisTransaction.KESIMPULAN_AMAN
            }

            tx.copy(
                jam = hour,
                flagJamTutup = flagJamTutup,
                selisihWaktuMenit = selisihMenit,
                flagSplitTx = flagSplitTx,
                flagNominalBulat = flagNominalBulat,
                flagSelfTx = flagSelfTx,
                digitPertama = digitPertama,
                flagBenfordAnomali = flagBenford,
                totalFraudScore = totalScore,
                kesimpulanAudit = kesimpulan
            )
        }
    }

    /**
     * Generates Benford's Law distribution table comparing theoretical percentage with actual.
     */
    fun calculateBenfordDistribution(transactions: List<QrisTransaction>): List<BenfordDistribution> {
        val totalValidDigits = transactions.count { it.digitPertama in 1..9 }

        return (1..9).map { d ->
            // Benford's theoretical probability: P(d) = log10(1 + 1/d)
            val expectedProb = log10(1.0 + (1.0 / d.toDouble())) * 100.0
            val actualCount = transactions.count { it.digitPertama == d }
            val actualPercent = if (totalValidDigits > 0) (actualCount.toDouble() / totalValidDigits) * 100.0 else 0.0

            BenfordDistribution(
                digit = d,
                expectedPercent = expectedProb,
                actualCount = actualCount,
                actualPercent = actualPercent
            )
        }
    }

    /**
     * Calculates the forensic summary statistics for dashboard metrics.
     */
    fun calculateSummary(transactions: List<QrisTransaction>): ForensicSummary {
        val audited = auditTransactions(transactions)
        val highRisk = audited.filter { it.totalFraudScore >= 2 }
        val lowRisk = audited.filter { it.totalFraudScore == 1 }
        val safe = audited.filter { it.totalFraudScore == 0 }

        return ForensicSummary(
            totalTransaksi = audited.size,
            totalNominal = audited.sumOf { it.nominal },
            countRisikoTinggi = highRisk.size,
            countRisikoRendah = lowRisk.size,
            countAman = safe.size,
            nominalRisikoTinggi = highRisk.sumOf { it.nominal },
            flagJamTutupCount = audited.count { it.flagJamTutup == 1 },
            flagSplitTxCount = audited.count { it.flagSplitTx == 1 },
            flagNominalBulatCount = audited.count { it.flagNominalBulat == 1 },
            flagSelfTxCount = audited.count { it.flagSelfTx == 1 },
            flagBenfordAnomaliCount = audited.count { it.flagBenfordAnomali == 1 },
            benfordDistributions = calculateBenfordDistribution(audited)
        )
    }

    /**
     * Generates CSV format identical to `hasil_audit_forensik_qris.csv` from Python script.
     */
    fun exportToCsv(transactions: List<QrisTransaction>): String {
        val audited = auditTransactions(transactions)
        val sb = StringBuilder()
        // Header
        sb.append("RRN,Waktu,Jam,ID_Kasir,User_ID_Konsumen,Nominal,Flag_Jam_Tutup,Selisih_Waktu_Menit,Flag_Split_Tx,Flag_Nominal_Bulat,Flag_Self_Tx,Digit_Pertama,Flag_Benford_Anomali,Total_Fraud_Score,Kesimpulan_Audit,Status_Investigasi\n")

        for (tx in audited) {
            val selisihStr = tx.selisihWaktuMenit?.let { String.format(Locale.US, "%.2f", it) } ?: ""
            sb.append("\"${tx.rrn}\",")
            sb.append("\"${tx.waktu}\",")
            sb.append("${tx.jam},")
            sb.append("\"${tx.idKasir}\",")
            sb.append("\"${tx.userIdKonsumen}\",")
            sb.append("${tx.nominal},")
            sb.append("${tx.flagJamTutup},")
            sb.append("\"$selisihStr\",")
            sb.append("${tx.flagSplitTx},")
            sb.append("${tx.flagNominalBulat},")
            sb.append("${tx.flagSelfTx},")
            sb.append("${tx.digitPertama},")
            sb.append("${tx.flagBenfordAnomali},")
            sb.append("${tx.totalFraudScore},")
            sb.append("\"${tx.kesimpulanAudit}\",")
            sb.append("\"${tx.statusInvestigasi}\"\n")
        }
        return sb.toString()
    }

    /**
     * Seed initial transactions matching the user's Python script.
     */
    fun getInitialSeedData(): List<QrisTransaction> {
        return listOf(
            QrisTransaction(
                rrn = "RRN_001",
                waktu = "2026-10-04 10:15:00",
                idKasir = "KASIR_01",
                userIdKonsumen = "CUST_99",
                nominal = 45500L
            ),
            QrisTransaction(
                rrn = "RRN_002",
                waktu = "2026-10-04 23:55:00",
                idKasir = "KASIR_02",
                userIdKonsumen = "CUST_12",
                nominal = 150000L
            ),
            QrisTransaction(
                rrn = "RRN_003",
                waktu = "2026-10-04 11:20:00",
                idKasir = "KASIR_01",
                userIdKonsumen = "CUST_55",
                nominal = 600000L
            ),
            QrisTransaction(
                rrn = "RRN_004",
                waktu = "2026-10-04 11:22:00",
                idKasir = "KASIR_01",
                userIdKonsumen = "CUST_55",
                nominal = 600000L
            ),
            QrisTransaction(
                rrn = "RRN_005",
                waktu = "2026-10-04 14:00:00",
                idKasir = "KASIR_03",
                userIdKonsumen = "CUST_77",
                nominal = 5000000L
            ),
            QrisTransaction(
                rrn = "RRN_006",
                waktu = "2026-10-04 15:30:00",
                idKasir = "KASIR_02",
                userIdKonsumen = "KASIR_02",
                nominal = 250000L
            ),
            QrisTransaction(
                rrn = "RRN_007",
                waktu = "2026-10-04 15:45:00",
                idKasir = "KASIR_01",
                userIdKonsumen = "CUST_88",
                nominal = 988770L
            ),
            QrisTransaction(
                rrn = "RRN_008",
                waktu = "2026-10-04 16:10:00",
                idKasir = "KASIR_03",
                userIdKonsumen = "CUST_44",
                nominal = 32500L
            )
        )
    }
}
