package com.fahad.spendlens.ui.screens

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fahad.spendlens.data.MerchantClassifier
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

    private val classifier: MerchantClassifier? = try {
        MerchantClassifier(app)
    } catch (e: Exception) {
        null   // model failed to load — fall back to manual category selection
    }

    fun suggestCategory(merchant: String): String {
        val c = classifier ?: return "Other"
        return try {
            val (category, confidence) = c.classify(merchant)
            if (confidence >= 0.5f) category else "Other"
        } catch (e: Exception) {
            "Other"
        }
    }

    fun onImagePicked(uri: Uri) {
        _uiState.value = ScanUiState(imageUri = uri, isProcessing = true)
        viewModelScope.launch {
            try {
                val image = InputImage.fromFilePath(getApplication(), uri)
                recognizer.process(image)
                    .addOnSuccessListener { result ->
                        val text = result.text
                        if (text.isBlank() || text.lines().count { it.isNotBlank() } < 2) {
                            // Nothing readable — probably not a receipt
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                error = "Couldn't read any text in that image. " +
                                        "Try a clearer photo with the receipt filling the frame."
                            )
                        } else if (!ReceiptParser.looksLikeReceipt(text)) {
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                error = "That doesn't look like a receipt. " +
                                        "If it is one, try a clearer, closer photo."
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                ocrText = text,
                                parsed = ReceiptParser.parse(text),
                                isProcessing = false
                            )
                        }
                    }
                    .addOnFailureListener { e ->
                        _uiState.value = _uiState.value.copy(
                            error = "Text recognition failed: ${e.message ?: "unknown error"}",
                            isProcessing = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Could not open that image. Please pick a different one.",
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