package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM qris_transactions ORDER BY id ASC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM qris_transactions ORDER BY id ASC")
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    @Query("SELECT * FROM qris_transactions WHERE rrn = :rrn LIMIT 1")
    suspend fun getTransactionByRrn(rrn: String): TransactionEntity?

    @Query("SELECT COUNT(*) FROM qris_transactions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM qris_transactions")
    suspend fun deleteAll()
}
