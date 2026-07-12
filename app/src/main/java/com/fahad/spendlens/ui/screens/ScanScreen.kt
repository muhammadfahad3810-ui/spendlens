package com.fahad.spendlens.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

val categories = listOf("Groceries", "Food", "Transport", "Fuel", "Shopping", "Utilities", "Health", "Other")

@Composable
fun ScanScreen(viewModel: ScanViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onImagePicked(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            state.saved -> {
                Text("✅ Saved!", style = MaterialTheme.typography.headlineMedium)
                Text("Transaction added — check History and Home")
                Button(onClick = { viewModel.reset() }) {
                    Text("Scan another receipt")
                }
            }

            state.parsed != null -> {
                ConfirmationForm(
                    parsed = state.parsed!!,
                    onSave = { merchant, amount, category ->
                        viewModel.saveTransaction(merchant, amount, category)
                    },
                    onRetry = { viewModel.reset() }
                )
            }

            else -> {
                Button(onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text("Pick receipt from gallery")
                }

                state.imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected receipt",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }

                if (state.isProcessing) {
                    CircularProgressIndicator()
                    Text("Reading receipt…")
                }

                state.error?.let { err ->
                    Text("Error: $err", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmationForm(
    parsed: com.fahad.spendlens.data.ParsedReceipt,
    onSave: (String, Double, String) -> Unit,
    onRetry: () -> Unit
) {
    var merchant by remember { mutableStateOf(parsed.merchant) }
    var amountText by remember { mutableStateOf(parsed.total?.toString() ?: "") }
    var category by remember { mutableStateOf(categories.first()) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Confirm details", style = MaterialTheme.typography.titleLarge)
            Text(
                "Check what was read from the receipt and fix anything that's wrong.",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (Rs)") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            val parsedAmount = amountText.toDoubleOrNull()
            Button(
                onClick = { onSave(merchant, parsedAmount ?: 0.0, category) },
                enabled = merchant.isNotBlank() && parsedAmount != null && parsedAmount > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save transaction")
            }

            OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Text("Try a different image")
            }
        }
    }
}