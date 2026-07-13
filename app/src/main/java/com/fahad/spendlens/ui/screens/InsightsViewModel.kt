package com.fahad.spendlens.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fahad.spendlens.data.SpendLensDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class InsightsViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = SpendLensDatabase.getInstance(app).transactionDao()

    private val calendar = Calendar.getInstance()

    private val monthStartMillis: Long = (calendar.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val dayOfMonth: Int = calendar.get(Calendar.DAY_OF_MONTH)
    val daysInMonth: Int = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val categoryTotals = dao.getCategoryTotalsSince(monthStartMillis)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyTotals = dao.getMonthlyTotals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthTotal = dao.getTotalSince(monthStartMillis)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}