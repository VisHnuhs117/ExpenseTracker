package com.vishnuhs.expensetracker.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnuhs.expensetracker.ml.ReceiptData
import com.vishnuhs.expensetracker.ml.ReceiptParser
import com.vishnuhs.expensetracker.ml.ReceiptTextExtractor
import com.vishnuhs.expensetracker.utils.FileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CameraUiState(
    val isProcessing: Boolean = false,
    val capturedImageUri: Uri? = null,
    val extractedText: String? = null,
    val receiptData: ReceiptData? = null,
    val error: String? = null
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val textExtractor: ReceiptTextExtractor,
    private val receiptParser: ReceiptParser,
    private val fileManager: FileManager
) : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"
    }

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onImageCaptured(imageUri: Uri) {
        Log.d(TAG, "Image captured: $imageUri")
        _uiState.update { it.copy(capturedImageUri = imageUri, isProcessing = true, error = null) }
        processReceipt(imageUri)
    }

    private fun processReceipt(imageUri: Uri) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting receipt processing...")

                val textResult = textExtractor.extractTextFromImage(imageUri)

                textResult.fold(
                    onSuccess = { extractedText ->
                        Log.d(TAG, "Text extracted successfully: $extractedText")

                        if (extractedText.isBlank()) {
                            Log.w(TAG, "Extracted text is empty")
                            _uiState.update {
                                it.copy(
                                    extractedText = "No text found",
                                    receiptData = ReceiptData(confidence = 0f),
                                    isProcessing = false,
                                    error = "No text could be extracted from the image"
                                )
                            }
                            return@fold
                        }

                        // Parse the extracted text
                        val receiptData = receiptParser.parseReceiptText(extractedText)
                        Log.d(TAG, "Receipt data parsed: $receiptData")

                        _uiState.update {
                            it.copy(
                                extractedText = extractedText,
                                receiptData = receiptData,
                                isProcessing = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Text extraction failed", exception)
                        _uiState.update {
                            it.copy(
                                error = "Failed to extract text: ${exception.message}",
                                isProcessing = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error processing receipt", e)
                _uiState.update {
                    it.copy(
                        error = "Error processing receipt: ${e.message}",
                        isProcessing = false
                    )
                }
            }
        }
    }

    fun retakePhoto() {
        Log.d(TAG, "Retaking photo")
        // Delete the current image file if it exists
        _uiState.value.capturedImageUri?.let { uri ->
            fileManager.deleteFile(uri.path ?: "")
        }

        _uiState.update {
            CameraUiState() // Reset to initial state
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Add this method for better error handling
    fun updateError(errorMessage: String) {
        _uiState.update { it.copy(error = errorMessage, isProcessing = false) }
    }
}