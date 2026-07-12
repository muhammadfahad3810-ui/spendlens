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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val monthlyTotal by viewModel.monthlyTotal.collectAsState()
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val recent by viewModel.recentTransactions.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---- Monthly total card ----
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Spent this month", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Rs ${"%,.0f".format(monthlyTotal ?: 0.0)}",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
        }

        // ---- Category breakdown ----
        if (categoryTotals.isNotEmpty()) {
            item {
                Text("By category", style = MaterialTheme.typography.titleMedium)
            }
            val grandTotal = categoryTotals.sumOf { it.total }
            items(categoryTotals) { cat ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(cat.category, style = MaterialTheme.typography.bodyMedium)
                        Text("Rs ${"%,.0f".format(cat.total)}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (cat.total / grandTotal).toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ---- Recent transactions ----
        if (recent.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text("Recent", style = MaterialTheme.typography.titleMedium)
            }
            items(recent) { txn ->
                val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(txn.merchant, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${txn.category} • ${dateFormat.format(Date(txn.dateMillis))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text("Rs ${"%,.0f".format(txn.amount)}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        // ---- Empty state ----
        if (categoryTotals.isEmpty() && recent.isEmpty()) {
            item {
                Text(
                    "No spending recorded yet — add transactions from the History tab",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}