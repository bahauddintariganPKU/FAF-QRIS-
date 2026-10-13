package com.example.util

enum class AppLanguage(val code: String, val label: String, val flag: String) {
    ID("id", "Bahasa Indonesia", "🇮🇩"),
    EN("en", "English", "🇬🇧")
}

object AuditTranslations {

    // App Bar & Global
    fun appTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Audit Forensik QRIS"
        AppLanguage.EN -> "QRIS Forensic Audit"
    }

    fun appSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Deteksi Anomali & Fraud Real-Time"
        AppLanguage.EN -> "Real-Time Anomaly & Fraud Detection"
    }

    // Bottom Navigation
    fun tabDashboard(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Dashboard"
        AppLanguage.EN -> "Dashboard"
    }

    fun tabTransactions(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Transaksi"
        AppLanguage.EN -> "Transactions"
    }

    fun tabReport(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Laporan"
        AppLanguage.EN -> "Reports"
    }

    fun tabSyncServer(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Server & Sync"
        AppLanguage.EN -> "Server & Sync"
    }

    fun tabAuditor(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Auditor"
        AppLanguage.EN -> "Auditor"
    }

    // Dashboard Metrics
    fun totalTransactions(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Total Transaksi"
        AppLanguage.EN -> "Total Transactions"
    }

    fun totalFraudScore(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Total Skor Fraud"
        AppLanguage.EN -> "Total Fraud Score"
    }

    fun highRisk(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Risiko Tinggi"
        AppLanguage.EN -> "High Risk"
    }

    fun lowRisk(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Risiko Rendah"
        AppLanguage.EN -> "Low Risk"
    }

    fun safe(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Aman"
        AppLanguage.EN -> "Safe"
    }

    fun totalTurnover(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Total Perputaran Uang"
        AppLanguage.EN -> "Total Turnover"
    }

    fun suspiciousTurnover(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Perputaran Mencurigakan"
        AppLanguage.EN -> "Suspicious Volume"
    }

    fun anomalyRate(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Tingkat Anomali"
        AppLanguage.EN -> "Anomaly Rate"
    }

    fun anomalyDistribution(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Distribusi 5 Parameter Anomali Fraud"
        AppLanguage.EN -> "5 Fraud Anomaly Parameters Distribution"
    }

    fun benfordAnalysis(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Analisis Distribusi Angka Benford"
        AppLanguage.EN -> "Benford's Law Digit Analysis"
    }

    // Indicator Names
    fun indicatorJamTutup(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Di Luar Jam Buka (23:00-06:00)"
        AppLanguage.EN -> "Outside Business Hours (23:00-06:00)"
    }

    fun indicatorSplitTx(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Split Transaksi (<= 5 mnt)"
        AppLanguage.EN -> "Split Transaction (<= 5 mins)"
    }

    fun indicatorNominalBulat(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Nominal Bulat Besar (Gestun)"
        AppLanguage.EN -> "Large Round Amount (Cash-out)"
    }

    fun indicatorSelfTx(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Kasir Transaksi Akun Sendiri"
        AppLanguage.EN -> "Cashier Self-Transaction"
    }

    fun indicatorBenford(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Anomali Hukum Benford (Digit 9)"
        AppLanguage.EN -> "Benford's Law Anomaly (Digit 9)"
    }

    // Actions & Buttons
    fun addTransaction(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Tambah Transaksi"
        AppLanguage.EN -> "Add Transaction"
    }

    fun addDemoData(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "+ Simulasi Transaksi"
        AppLanguage.EN -> "+ Simulate Transaction"
    }

    fun resetData(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Reset Data Contoh"
        AppLanguage.EN -> "Reset Sample Data"
    }

    fun exportCsv(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Ekspor CSV"
        AppLanguage.EN -> "Export CSV"
    }

    fun save(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Simpan"
        AppLanguage.EN -> "Save"
    }

    fun cancel(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Batal"
        AppLanguage.EN -> "Cancel"
    }

    fun close(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Tutup"
        AppLanguage.EN -> "Close"
    }

    fun searchPlaceholder(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Cari RRN, ID Kasir, Konsumen..."
        AppLanguage.EN -> "Search RRN, Cashier, Customer..."
    }

    fun filterAll(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Semua"
        AppLanguage.EN -> "All"
    }

    fun alertsTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Pusat Peringatan & Log Real-Time"
        AppLanguage.EN -> "Real-Time Alerts & Log Center"
    }

    // Server Database Connection Strings
    fun serverDatabaseTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Koneksi Server Database"
        AppLanguage.EN -> "Database Server Connection"
    }

    fun serverDatabaseSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Konfigurasi tautan URL server database API yang dapat diedit langsung."
        AppLanguage.EN -> "Configure editable direct database server API link."
    }

    fun serverUrlLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Tautan / URL Server Database"
        AppLanguage.EN -> "Database Server Link / URL"
    }

    fun editServerLink(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Ubah Link Server"
        AppLanguage.EN -> "Edit Server Link"
    }

    fun apiKeyLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Kunci API / Bearer Token (Opsional)"
        AppLanguage.EN -> "API Key / Bearer Token (Optional)"
    }

    fun databaseNameLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Nama Database / Koleksi"
        AppLanguage.EN -> "Database / Collection Name"
    }

    fun testConnection(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Uji Koneksi Server"
        AppLanguage.EN -> "Test Server Connection"
    }

    fun syncNow(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Sinkronisasi ke Database Server"
        AppLanguage.EN -> "Sync to Database Server"
    }

    fun autoSyncLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Sinkronisasi Otomatis saat Ada Transaksi Baru"
        AppLanguage.EN -> "Auto-Sync on New Transactions"
    }

    fun connectionStatus(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Status Koneksi Server"
        AppLanguage.EN -> "Server Connection Status"
    }

    fun lastSyncTime(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Terakhir Disinkronkan"
        AppLanguage.EN -> "Last Synchronized"
    }

    // Language Selector
    fun languageLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Pilihan Bahasa"
        AppLanguage.EN -> "Language Settings"
    }

    fun languageSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Ganti bahasa antarmuka antara Bahasa Indonesia dan Inggris"
        AppLanguage.EN -> "Switch UI language between Indonesian and English"
    }

    // Transaction Details & Investigation
    fun detailTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Detail Forensik Transaksi"
        AppLanguage.EN -> "Transaction Forensic Detail"
    }

    fun investigationStatus(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Status Investigasi"
        AppLanguage.EN -> "Investigation Status"
    }

    fun auditorNotes(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Catatan Investigasi Auditor"
        AppLanguage.EN -> "Auditor Investigation Notes"
    }

    fun updateInvestigation(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Perbarui Investigasi"
        AppLanguage.EN -> "Update Investigation"
    }

    fun deleteTransaction(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Hapus Transaksi"
        AppLanguage.EN -> "Delete Transaction"
    }

    // Auditor Profile
    fun auditorProfileTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Profil Auditor & Otorisasi"
        AppLanguage.EN -> "Auditor Profile & Authorization"
    }

    fun switchAuditor(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Ganti Akun Auditor"
        AppLanguage.EN -> "Switch Auditor Account"
    }

    fun auditTrail(lang: AppLanguage) = when (lang) {
        AppLanguage.ID -> "Jejak Audit Forensik (Audit Trail)"
        AppLanguage.EN -> "Forensic Audit Trail"
    }
}
