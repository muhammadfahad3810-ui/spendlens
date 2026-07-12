package com.fahad.spendlens.ui.screens

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanUiState(
    val imageUri: Uri? = null,
    val ocrText: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null
)

class ScanViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState = _uiState.asStateFlow()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun onImagePicked(uri: Uri) {
        _uiState.value = ScanUiState(imageUri = uri, isProcessing = true)
        viewModelScope.launch {
            try {
                val image = InputImage.fromFilePath(getApplication(), uri)
                recognizer.process(image)
                    .addOnSuccessListener { result ->
                        _uiState.value = _uiState.value.copy(
                            ocrText = result.text,
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
}