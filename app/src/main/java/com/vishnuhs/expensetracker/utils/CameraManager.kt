package com.vishnuhs.expensetracker.utils

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CameraManager @Inject constructor(
    private val context: Context
) {
    private var cameraExecutor: ExecutorService? = null

    fun getCameraExecutor(): ExecutorService {
        if (cameraExecutor == null) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }
        return cameraExecutor!!
    }

    suspend fun getCameraProvider(): ProcessCameraProvider =
        suspendCancellableCoroutine { continuation ->
            ProcessCameraProvider.getInstance(context).also { cameraProvider ->
                cameraProvider.addListener({
                    continuation.resume(cameraProvider.get())
                }, ContextCompat.getMainExecutor(context))
            }
        }

    fun bindCamera(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        preview: Preview,
        imageCapture: ImageCapture
    ): Camera {
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        return cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    }

    fun shutdown() {
        cameraExecutor?.shutdown()
        cameraExecutor = null
    }
}