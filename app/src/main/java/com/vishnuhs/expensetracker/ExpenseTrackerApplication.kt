package com.vishnuhs.expensetracker

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpenseTrackerApplication : Application() {

    companion object {
        private const val TAG = "ExpenseTrackerApp"
    }

    override fun onCreate() {
        super.onCreate()

        // Set up uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", exception)

            // Log the specific error details
            when {
                exception.message?.contains("libimage_processing_util_jni.so") == true -> {
                    Log.e(TAG, "ML Kit native library crash detected - 16KB alignment issue")
                }
                exception.message?.contains("libmlkit_google_ocr_pipeline.so") == true -> {
                    Log.e(TAG, "ML Kit OCR pipeline crash detected - 16KB alignment issue")
                }
                else -> {
                    Log.e(TAG, "General application crash: ${exception.message}")
                }
            }

            // You could also send crash reports to analytics here
            // crashlytics.recordException(exception)

            // Let the system handle the crash
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        Log.d(TAG, "ExpenseTracker Application started")
    }
}