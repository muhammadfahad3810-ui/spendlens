package com.fahad.spendlens.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchant: String,
    val amount: Double,
    val dateMillis: Long,          // store dates as timestamps
    val category: String,          // "Food", "Transport", etc. — ML will fill this later
    val rawOcrText: String? = null // the original scanned text, useful for debugging the parser
)