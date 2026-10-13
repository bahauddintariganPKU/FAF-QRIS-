package com.example.model

data class QrisTransaction(
    val id: Long = 0,
    val rrn: String,
    val waktu: String, // Format: yyyy-MM-dd HH:mm:ss
    val idKasir: String,
    val userIdKonsumen: String,
    val nominal: Long,
    val jam: Int = 0,
    // Indikator Audit Forensik
    val flagJamTutup: Int = 0,
    val selisihWaktuMenit: Double? = null,
    val flagSplitTx: Int = 0,
    val flagNominalBulat: Int = 0,
    val flagSelfTx: Int = 0,
    val digitPertama: Int = 0,
    val flagBenfordAnomali: Int = 0,
    val totalFraudScore: Int = 0,
    val kesimpulanAudit: String = KESIMPULAN_AMAN,
    val statusInvestigasi: String = STATUS_BELUM_DITINJAU,
    val catatanAuditor: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val KESIMPULAN_RISIKO_TINGGI = "RISIKO TINGGI (Investigasi Segera)"
        const val KESIMPULAN_RISIKO_RENDAH = "Risiko Rendah (Pantau)"
        const val KESIMPULAN_AMAN = "Aman / Valid"

        const val STATUS_BELUM_DITINJAU = "Belum Ditinjau"
        const val STATUS_SEDANG_DIINVESTIGASI = "Sedang Diinvestigasi"
        const val STATUS_SELESAI = "Selesai Ditinjau"
        const val STATUS_FRAUD = "Dikonfirmasi Fraud"
    }
}

data class BenfordDistribution(
    val digit: Int,
    val expectedPercent: Double,
    val actualCount: Int,
    val actualPercent: Double
)

data class ForensicSummary(
    val totalTransaksi: Int = 0,
    val totalNominal: Long = 0,
    val countRisikoTinggi: Int = 0,
    val countRisikoRendah: Int = 0,
    val countAman: Int = 0,
    val nominalRisikoTinggi: Long = 0,
    val flagJamTutupCount: Int = 0,
    val flagSplitTxCount: Int = 0,
    val flagNominalBulatCount: Int = 0,
    val flagSelfTxCount: Int = 0,
    val flagBenfordAnomaliCount: Int = 0,
    val benfordDistributions: List<BenfordDistribution> = emptyList()
)
