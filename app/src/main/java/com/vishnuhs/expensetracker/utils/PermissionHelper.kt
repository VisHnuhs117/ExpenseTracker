package com.vishnuhs.expensetracker.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionHelper {

    const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    const val READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE

    val REQUIRED_PERMISSIONS = arrayOf(
        CAMERA_PERMISSION,
        READ_STORAGE_PERMISSION
    )

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            READ_STORAGE_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasAllPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}