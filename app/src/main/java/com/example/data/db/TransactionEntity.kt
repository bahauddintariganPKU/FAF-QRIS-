package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.QrisTransaction

@Entity(tableName = "qris_transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rrn: String,
    val waktu: String,
    val idKasir: String,
    val userIdKonsumen: String,
    val nominal: Long,
    val statusInvestigasi: String = QrisTransaction.STATUS_BELUM_DITINJAU,
    val catatanAuditor: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): QrisTransaction {
        return QrisTransaction(
            id = id,
            rrn = rrn,
            waktu = waktu,
            idKasir = idKasir,
            userIdKonsumen = userIdKonsumen,
            nominal = nominal,
            statusInvestigasi = statusInvestigasi,
            catatanAuditor = catatanAuditor,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(domain: QrisTransaction): TransactionEntity {
            return TransactionEntity(
                id = domain.id,
                rrn = domain.rrn,
                waktu = domain.waktu,
                idKasir = domain.idKasir,
                userIdKonsumen = domain.userIdKonsumen,
                nominal = domain.nominal,
                statusInvestigasi = domain.statusInvestigasi,
                catatanAuditor = domain.catatanAuditor,
                updatedAt = domain.updatedAt
            )
        }
    }
}
