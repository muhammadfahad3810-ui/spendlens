package com.fahad.spendlens.ui.screens

import android.app.Application
import android.net.Uri
import com.fahad.spendlens.data.MerchantClassifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fahad.spendlens.data.ParsedReceipt
import com.fahad.spendlens.data.ReceiptParser
import com.fahad.spendlens.data.SpendLensDatabase
import com.fahad.spendlens.data.TransactionEntity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanUiState(
    val imageUri: Uri? = null,
    val ocrText: String = "",
    val parsed: ParsedReceipt? = null,
    val isProcessing: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class ScanViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState = _uiState.asStateFlow()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val dao = SpendLensDatabase.getInstance(app).transactionDao()

    private val classifier = MerchantClassifier(app)

    fun suggestCategory(merchant: String): String {
        val (category, confidence) = classifier.classify(merchant)
        // Only trust the model when it's reasonably sure
        return if (confidence >= 0.5f) category else "Other"
    }

    fun onImagePicked(uri: Uri) {
        _uiState.value = ScanUiState(imageUri = uri, isProcessing = true)
        viewModelScope.launch {
            try {
                val image = InputImage.fromFilePath(getApplication(), uri)
                recognizer.process(image)
                    .addOnSuccessListener { result ->
                        _uiState.value = _uiState.value.copy(
                            ocrText = result.text,
                            parsed = ReceiptParser.parse(result.text),
                            isProcessing = false
                        )
                    }
                    .addOnFailureListener { e ->
                        _uiState.value = _uiState.value.copy(
                            error = e.message ?: "OCR failed",
                            isProcessing = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Could not load image",
                    isProcessing = false
                )
            }
        }
    }

    fun saveTransaction(merchant: String, amount: Double, category: String) {
        viewModelScope.launch {
            dao.insert(
                TransactionEntity(
                    merchant = merchant,
                    amount = amount,
                    dateMillis = _uiState.value.parsed?.dateMillis
                        ?: System.currentTimeMillis(),
                    category = category,
                    rawOcrText = _uiState.value.ocrText
                )
            )
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }

    fun reset() {
        _uiState.value = ScanUiState()
    }
}