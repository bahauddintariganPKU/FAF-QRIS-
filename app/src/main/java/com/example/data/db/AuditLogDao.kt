package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllLogsFlow(): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insert(log: AuditLogEntity): Long

    @Query("DELETE FROM audit_logs")
    suspend fun clearLogs()
}
