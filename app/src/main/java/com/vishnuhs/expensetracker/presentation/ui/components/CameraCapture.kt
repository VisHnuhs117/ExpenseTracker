package com.vishnuhs.expensetracker.presentation.ui.components

import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.vishnuhs.expensetracker.utils.FileManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraCapture(
    onImageCaptured: (Uri) -> Unit,
    onError: (Exception) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fileManager = remember { FileManager(context) }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                // Initialize camera provider
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val provider = cameraProviderFuture.get()
                        cameraProvider = provider

                        // Set up camera
                        setupCamera(provider, previewView, lifecycleOwner) { capture ->
                            imageCapture = capture
                        }

                    } catch (exc: Exception) {
                        onError(exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Camera overlay - Receipt frame guide
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(0.7f)
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        // Capture button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            FloatingActionButton(
                onClick = {
                    takePhoto(imageCapture, fileManager, cameraExecutor, onImageCaptured, onError)
                },
                modifier = Modifier.size(80.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Capture",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        }

        // Instructions
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            Text(
                text = "Position the receipt within the frame",
                modifier = Modifier.padding(12.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    // Cleanup when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

private fun setupCamera(
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    try {
        // Unbind use cases before rebinding
        cameraProvider.unbindAll()

        // Preview use case
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // ImageCapture use case
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        // FIXED: Only use back camera if available, fallback to any available
        val cameraSelector = try {
            if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                // Build a custom selector for the first available camera
                CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
            }
        } catch (e: Exception) {
            // If all else fails, use default
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        // Bind use cases to camera with proper error handling
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )

        onImageCaptureReady(imageCapture)

    } catch (exc: Exception) {
        throw exc
    }
}

private fun takePhoto(
    imageCapture: ImageCapture?,
    fileManager: FileManager,
    executor: ExecutorService,
    onImageCaptured: (Uri) -> Unit,
    onError: (Exception) -> Unit
) {
    val capture = imageCapture ?: run {
        onError(Exception("Camera not initialized"))
        return
    }

    try {
        val outputFile = fileManager.createImageFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        capture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val imageUri = fileManager.getImageUri(outputFile)
                    onImageCaptured(imageUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    } catch (e: Exception) {
        onError(e)
    }
}