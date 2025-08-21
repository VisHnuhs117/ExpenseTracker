package com.vishnuhs.expensetracker.presentation.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.vishnuhs.expensetracker.ml.ReceiptData
import com.vishnuhs.expensetracker.presentation.ui.components.CameraCapture
import com.vishnuhs.expensetracker.presentation.ui.components.ReceiptPreview
import com.vishnuhs.expensetracker.presentation.viewmodel.CameraViewModel
import com.vishnuhs.expensetracker.utils.PermissionHelper

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onNavigateToAddExpense: (ReceiptData?, String?) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(PermissionHelper.CAMERA_PERMISSION)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // Handle errors with snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Log.e("CameraScreen", "Error: $error")
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Receipt") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !cameraPermissionState.status.isGranted -> {
                    PermissionRationaleContent(
                        shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }

                uiState.capturedImageUri != null -> {
                    ReceiptPreview(
                        imageUri = uiState.capturedImageUri!!,
                        receiptData = uiState.receiptData,
                        isProcessing = uiState.isProcessing,
                        extractedText = uiState.extractedText,
                        onRetakePhoto = {
                            Log.d("CameraScreen", "Retaking photo")
                            viewModel.retakePhoto()
                        },
                        onConfirm = {
                            Log.d("CameraScreen", "Confirming receipt with data: ${uiState.receiptData}")
                            try {
                                onNavigateToAddExpense(uiState.receiptData, uiState.capturedImageUri?.path)
                            } catch (e: Exception) {
                                Log.e("CameraScreen", "Error navigating to add expense", e)
                                viewModel.updateError("Navigation error: ${e.message}")
                            }
                        }
                    )
                }

                else -> {
                    CameraCapture(
                        onImageCaptured = { imageUri ->
                            Log.d("CameraScreen", "Image captured: $imageUri")
                            try {
                                viewModel.onImageCaptured(imageUri)
                            } catch (e: Exception) {
                                Log.e("CameraScreen", "Error processing captured image", e)
                                viewModel.updateError("Failed to process image: ${e.message}")
                            }
                        },
                        onError = { error ->
                            Log.e("CameraScreen", "Camera error: ${error.message}", error)
                            viewModel.updateError(error.message ?: "Camera error occurred")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRationaleContent(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Camera,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (shouldShowRationale) {
                "Camera permission is required to scan receipts. Please grant permission to continue."
            } else {
                "Camera permission is needed to scan receipts."
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}