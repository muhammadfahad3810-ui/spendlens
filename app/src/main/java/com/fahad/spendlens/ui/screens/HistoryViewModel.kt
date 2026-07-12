package com.fahad.spendlens.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fahad.spendlens.data.SpendLensDatabase
import com.fahad.spendlens.data.TransactionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class HistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = SpendLensDatabase.getInstance(app).transactionDao()

    val transactions = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Temporary — just to test the pipeline. The scanner will replace this.
    fun addTestTransaction() {
        viewModelScope.launch {
            val merchants = listOf("Al-Fatah Store", "Careem", "KFC", "Metro Cash&Carry", "Shell")
            val categories = listOf("Groceries", "Transport", "Food", "Groceries", "Fuel")
            val i = Random.nextInt(merchants.size)
            dao.insert(
                TransactionEntity(
                    merchant = merchants[i],
                    amount = Random.nextDouble(100.0, 5000.0),
                    dateMillis = System.currentTimeMillis(),
                    category = categories[i]
                )
            )
        }
    }
}