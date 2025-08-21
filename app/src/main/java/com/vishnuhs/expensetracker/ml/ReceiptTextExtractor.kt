package com.vishnuhs.expensetracker.ml

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class ReceiptTextExtractor @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "ReceiptTextExtractor"
        private const val TIMEOUT_MS = 30000L // 30 seconds timeout
    }

    private val recognizer by lazy {
        try {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ML Kit Text Recognizer", e)
            null
        }
    }

    suspend fun extractTextFromImage(imageUri: Uri): Result<String> {
        return try {
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    try {
                        if (recognizer == null) {
                            continuation.resume(Result.failure(Exception("ML Kit Text Recognizer not available")))
                            return@suspendCancellableCoroutine
                        }

                        val image = InputImage.fromFilePath(context, imageUri)

                        recognizer!!.process(image)
                            .addOnSuccessListener { visionText ->
                                val extractedText = visionText.text
                                Log.d(TAG, "Successfully extracted text: ${extractedText.take(100)}...")
                                continuation.resume(Result.success(extractedText))
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Text recognition failed", exception)
                                continuation.resume(Result.failure(exception))
                            }

                    } catch (e: IOException) {
                        Log.e(TAG, "IO Exception during text extraction", e)
                        continuation.resume(Result.failure(e))
                    } catch (e: Exception) {
                        Log.e(TAG, "Unexpected exception during text extraction", e)
                        continuation.resume(Result.failure(e))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Timeout or other error in extractTextFromImage", e)
            Result.failure(e)
        }
    }

    suspend fun extractTextFromBitmap(bitmap: Bitmap): Result<String> {
        return try {
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    try {
                        if (recognizer == null) {
                            continuation.resume(Result.failure(Exception("ML Kit Text Recognizer not available")))
                            return@suspendCancellableCoroutine
                        }

                        val image = InputImage.fromBitmap(bitmap, 0)

                        recognizer!!.process(image)
                            .addOnSuccessListener { visionText ->
                                val extractedText = visionText.text
                                Log.d(TAG, "Successfully extracted text from bitmap: ${extractedText.take(100)}...")
                                continuation.resume(Result.success(extractedText))
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Bitmap text recognition failed", exception)
                                continuation.resume(Result.failure(exception))
                            }

                    } catch (e: Exception) {
                        Log.e(TAG, "Exception during bitmap text extraction", e)
                        continuation.resume(Result.failure(e))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Timeout or other error in extractTextFromBitmap", e)
            Result.failure(e)
        }
    }

    fun cleanup() {
        try {
            recognizer?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing recognizer", e)
        }
    }
}