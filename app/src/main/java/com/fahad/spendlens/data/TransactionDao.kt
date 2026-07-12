package com.fahad.spendlens.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT category, SUM(amount) AS total FROM transactions WHERE dateMillis >= :fromMillis GROUP BY category ORDER BY total DESC")
    fun getCategoryTotalsSince(fromMillis: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC LIMIT 5")
    fun getRecent(): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE dateMillis >= :fromMillis")
    fun getTotalSince(fromMillis: Long): Flow<Double?>
}