package com.fahad.spendlens.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fahad.spendlens.data.SpendLensDatabase
import com.fahad.spendlens.data.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class HistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = SpendLensDatabase.getInstance(app).transactionDao()

    val transactions = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Holds the last deleted transaction so Undo can restore it
    private val _recentlyDeleted = MutableStateFlow<TransactionEntity?>(null)
    val recentlyDeleted = _recentlyDeleted.asStateFlow()

    fun delete(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.delete(transaction)
            _recentlyDeleted.value = transaction
        }
    }

    fun undoDelete() {
        val txn = _recentlyDeleted.value ?: return
        viewModelScope.launch {
            // Re-insert with id = 0 so Room assigns a fresh id
            dao.insert(txn.copy(id = 0))
            _recentlyDeleted.value = null
        }
    }

    fun clearUndo() {
        _recentlyDeleted.value = null
    }

    fun update(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.update(transaction)
        }
    }

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
