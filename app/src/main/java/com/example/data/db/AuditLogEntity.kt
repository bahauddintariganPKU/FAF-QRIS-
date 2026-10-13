package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val auditorName: String,
    val actionType: String, // "ADD_TX", "EDIT_TX", "DELETE_TX", "INVESTIGATE", "SYNC_GITHUB", "EXPORT_CSV"
    val description: String,
    val rrn: String? = null,
    val isAlert: Boolean = false
)
