package com.fahad.spendlens.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fahad.spendlens.data.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val monthlyTotal by viewModel.monthlyTotal.collectAsState()
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TotalSpendingCard(total = monthlyTotal ?: 0.0)
        }

        if (categoryTotals.isNotEmpty()) {
            item {
                Text("By Category", style = MaterialTheme.typography.titleLarge)
            }
            items(categoryTotals) { catTotal ->
                CategorySummaryRow(catTotal.category, catTotal.total)
            }
        }

        if (recentTransactions.isNotEmpty()) {
            item {
                Text("Recent Transactions", style = MaterialTheme.typography.titleLarge)
            }
            items(recentTransactions) { txn ->
                RecentTransactionRow(txn)
            }
        }
    }
}

@Composable
fun TotalSpendingCard(total: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Total Spent This Month", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Rs ${"%.2f".format(total)}",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CategorySummaryRow(category: String, total: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(category, style = MaterialTheme.typography.bodyLarge)
        Text("Rs ${"%.2f".format(total)}", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun RecentTransactionRow(txn: TransactionEntity) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(txn.merchant, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${txn.category} • ${dateFormat.format(Date(txn.dateMillis))}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text("Rs ${"%.2f".format(txn.amount)}", style = MaterialTheme.typography.bodyLarge)
    }
}
