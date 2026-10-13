package com.example

import com.example.engine.ForensicAuditEngine
import com.example.model.QrisTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForensicAuditUnitTest {

    @Test
    fun testInitialSeedForensicAuditMatchesPythonScript() {
        val rawData = ForensicAuditEngine.getInitialSeedData()
        val audited = ForensicAuditEngine.auditTransactions(rawData)

        assertEquals(8, audited.size)

        // RRN_001: Wajar
        val rrn1 = audited.first { it.rrn == "RRN_001" }
        assertEquals(0, rrn1.totalFraudScore)
        assertEquals(QrisTransaction.KESIMPULAN_AMAN, rrn1.kesimpulanAudit)

        // RRN_002: Kasus 1: Di luar jam operasional toko (23:55)
        val rrn2 = audited.first { it.rrn == "RRN_002" }
        assertEquals(1, rrn2.flagJamTutup)
        assertEquals(1, rrn2.totalFraudScore)
        assertEquals(QrisTransaction.KESIMPULAN_RISIKO_RENDAH, rrn2.kesimpulanAudit)

        // RRN_004: Kasus 2: Split transaction (CUST_55, diff <= 5 menit, nominal 600000 sama)
        val rrn4 = audited.first { it.rrn == "RRN_004" }
        assertEquals(1, rrn4.flagSplitTx)
        assertEquals(1, rrn4.totalFraudScore)

        // RRN_005: Kasus 3: Gestun / Angka bulat besar tak wajar (5.000.000)
        val rrn5 = audited.first { it.rrn == "RRN_005" }
        assertEquals(1, rrn5.flagNominalBulat)
        assertEquals(1, rrn5.totalFraudScore)

        // RRN_006: Kasus 4: Kasir pakai akun sendiri (KASIR_02 == KASIR_02)
        val rrn6 = audited.first { it.rrn == "RRN_006" }
        assertEquals(1, rrn6.flagSelfTx)
        assertEquals(1, rrn6.totalFraudScore)

        // RRN_007: Kasus 5: Angka rekayasa manual (Melanggar Benford's Law, awalan 9, > 500rb)
        val rrn7 = audited.first { it.rrn == "RRN_007" }
        assertEquals(9, rrn7.digitPertama)
        assertEquals(1, rrn7.flagBenfordAnomali)
        assertEquals(1, rrn7.totalFraudScore)

        // RRN_008: Wajar
        val rrn8 = audited.first { it.rrn == "RRN_008" }
        assertEquals(0, rrn8.totalFraudScore)
        assertEquals(QrisTransaction.KESIMPULAN_AMAN, rrn8.kesimpulanAudit)
    }

    @Test
    fun testHighRiskClassification() {
        val highRiskTx = QrisTransaction(
            rrn = "RRN_TEST_HIGH",
            waktu = "2026-10-04 23:50:00", // Flag Jam Tutup = 1
            idKasir = "KASIR_01",
            userIdKonsumen = "KASIR_01",    // Flag Self Tx = 1
            nominal = 5000000L              // Flag Nominal Bulat = 1
        )

        val audited = ForensicAuditEngine.auditTransactions(listOf(highRiskTx)).first()
        assertTrue(audited.totalFraudScore >= 2)
        assertEquals(QrisTransaction.KESIMPULAN_RISIKO_TINGGI, audited.kesimpulanAudit)
    }

    @Test
    fun testCsvExportFormat() {
        val rawData = ForensicAuditEngine.getInitialSeedData()
        val csv = ForensicAuditEngine.exportToCsv(rawData)

        assertTrue(csv.contains("RRN,Waktu,Jam,ID_Kasir,User_ID_Konsumen,Nominal"))
        assertTrue(csv.contains("RRN_001"))
        assertTrue(csv.contains("RRN_007"))
    }
}
