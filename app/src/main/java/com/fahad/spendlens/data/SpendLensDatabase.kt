package com.fahad.spendlens.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class SpendLensDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: SpendLensDatabase? = null

        fun getInstance(context: Context): SpendLensDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SpendLensDatabase::class.java,
                    "spendlens.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}