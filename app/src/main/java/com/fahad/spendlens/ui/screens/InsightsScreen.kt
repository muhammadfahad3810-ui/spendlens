package com.fahad.spendlens.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

private val chartColors = listOf(
    Color(0xFF4E79A7), Color(0xFFF28E2B), Color(0xFFE15759), Color(0xFF76B7B2),
    Color(0xFF59A14F), Color(0xFFEDC948), Color(0xFFB07AA1), Color(0xFF9C755F)
)

@Composable
fun InsightsScreen(viewModel: InsightsViewModel = viewModel()) {
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val monthlyTotals by viewModel.monthlyTotals.collectAsState()
    val monthTotal by viewModel.monthTotal.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---- Forecast card ----
        item {
            val spent = monthTotal ?: 0.0
            val projected = if (viewModel.dayOfMonth > 0)
                spent / viewModel.dayOfMonth * viewModel.daysInMonth else 0.0

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Month forecast", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Rs ${"%,.0f".format(projected)}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Based on Rs ${"%,.0f".format(spent)} spent in the first ${viewModel.dayOfMonth} days",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // ---- Donut chart: this month by category ----
        if (categoryTotals.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("This month by category", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DonutChart(
                                values = categoryTotals.map { it.total },
                                modifier = Modifier.size(140.dp)
                            )
                            Spacer(Modifier.width(20.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                categoryTotals.forEachIndexed { i, cat ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                        ) {
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                drawCircle(chartColors[i % chartColors.size])
                                            }
                                        }
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "${cat.category} — Rs ${"%,.0f".format(cat.total)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ---- Bar chart: last months ----
        if (monthlyTotals.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Spending by month", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        BarChart(
                            labels = monthlyTotals.map { it.month.takeLast(2) },
                            values = monthlyTotals.map { it.total },
                            barColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().height(160.dp)
                        )
                    }
                }
            }
        }

        if (categoryTotals.isEmpty() && monthlyTotals.isEmpty()) {
            item {
                Text("No data yet — scan some receipts first!")
            }
        }
    }
}

@Composable
private fun DonutChart(values: List<Double>, modifier: Modifier = Modifier) {
    val total = values.sum().takeIf { it > 0 } ?: return

    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.18f)
        val inset = stroke.width / 2
        val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
        var startAngle = -90f

        values.forEachIndexed { i, value ->
            val sweep = (value / total * 360f).toFloat()
            drawArc(
                color = chartColors[i % chartColors.size],
                startAngle = startAngle,
                sweepAngle = sweep - 2f,   // small gap between segments
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun BarChart(
    labels: List<String>,
    values: List<Double>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = values.maxOrNull()?.takeIf { it > 0 } ?: return

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            values.forEach { value ->
                val heightFraction = (value / maxValue).toFloat()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val barHeight = size.height * heightFraction
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(0f, size.height - barHeight),
                            size = Size(size.width, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            labels.forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}