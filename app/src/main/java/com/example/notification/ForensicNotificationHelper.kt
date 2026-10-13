package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.model.QrisTransaction
import java.text.NumberFormat
import java.util.Locale

data class RealtimeAlertItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val rrn: String,
    val title: String,
    val message: String,
    val isHighRisk: Boolean,
    val score: Int
)

object ForensicNotificationHelper {

    private const val CHANNEL_FRAUD_ALERT_ID = "qris_fraud_alerts"
    private const val CHANNEL_FRAUD_ALERT_NAME = "Peringatan Fraud QRIS Real-Time"

    private const val CHANNEL_UPDATES_ID = "qris_tx_updates"
    private const val CHANNEL_UPDATES_NAME = "Pembaruan Transaksi & Audit"

    private const val CHANNEL_SYNC_ID = "qris_github_sync"
    private const val CHANNEL_SYNC_NAME = "Sinkronisasi GitHub"

    private var channelInitialized = false

    fun initNotificationChannels(context: Context) {
        if (channelInitialized) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val fraudChannel = NotificationChannel(
                CHANNEL_FRAUD_ALERT_ID,
                CHANNEL_FRAUD_ALERT_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi real-time jika terdeteksi anomali kecurangan atau risiko tinggi pada transaksi QRIS"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }

            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES_ID,
                CHANNEL_UPDATES_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pembaruan dan perubahan data transaksi audit QRIS"
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC_ID,
                CHANNEL_SYNC_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Status sinkronisasi data audit ke repositori GitHub"
            }

            notificationManager.createNotificationChannels(listOf(fraudChannel, updatesChannel, syncChannel))
            channelInitialized = true
        }
    }

    private fun formatCurrency(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    fun sendTransactionNotification(
        context: Context,
        transaction: QrisTransaction,
        actionType: String = "Perubahan Data"
    ) {
        initNotificationChannels(context)

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val isHighRisk = transaction.totalFraudScore >= 2
        val channelId = if (isHighRisk) CHANNEL_FRAUD_ALERT_ID else CHANNEL_UPDATES_ID
        val notificationId = (transaction.rrn.hashCode() + System.currentTimeMillis().toInt() % 1000)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isHighRisk) {
            "🚨 FRAUD ALERT: ${transaction.rrn} (${transaction.kesimpulanAudit})"
        } else if (transaction.totalFraudScore == 1) {
            "⚠️ Pantau Transaksi: ${transaction.rrn} (Skor 1)"
        } else {
            "✅ Transaksi Aman: ${transaction.rrn}"
        }

        val indicators = mutableListOf<String>()
        if (transaction.flagJamTutup == 1) indicators.add("Jam Tutup")
        if (transaction.flagSplitTx == 1) indicators.add("Split Tx")
        if (transaction.flagNominalBulat == 1) indicators.add("Nominal Bulat/Gestun")
        if (transaction.flagSelfTx == 1) indicators.add("Self-Tx")
        if (transaction.flagBenfordAnomali == 1) indicators.add("Anomali Benford")

        val indicatorText = if (indicators.isNotEmpty()) " [Indikator: ${indicators.joinToString(", ")}]" else ""
        val content = "$actionType: Kasir ${transaction.idKasir} | ${formatCurrency(transaction.nominal)}$indicatorText"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$content\nStatus: ${transaction.kesimpulanAudit}\nWaktu: ${transaction.waktu}"))
            .setPriority(if (isHighRisk) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Handled gracefully
        }
    }

    fun sendSyncNotification(context: Context, title: String, message: String, isSuccess: Boolean) {
        initNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != PackageManager.PERMISSION_GRANTED) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SYNC_ID)
            .setSmallIcon(if (isSuccess) android.R.drawable.stat_sys_upload_done else android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(2001, builder.build())
        } catch (_: SecurityException) {
            // Handled
        }
    }
}
