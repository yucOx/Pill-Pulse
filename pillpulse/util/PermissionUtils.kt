package com.yucox.pillpulse.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yucox.pillpulse.util.showToastLong

class PermissionUtils {

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.SET_ALARM,
            Manifest.permission.SCHEDULE_EXACT_ALARM,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.USE_EXACT_ALARM
        )
    }

    fun hasPermission(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun showPermissionsRequest(activity: ComponentActivity): ActivityResultLauncher<Array<String>> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            handlePermissionResult(activity, permissions)
        }
    }

    private fun handlePermissionResult(
        activity: ComponentActivity,
        permissions: Map<String, Boolean>
    ) {
        val allGranted = permissions.all { it.value }

        if (!allGranted) {
            val rationaleRequired = REQUIRED_PERMISSIONS.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permission
                )
            }

            if (rationaleRequired) {
                activity.showToastLong("Programın düzgün çalışması için izinleri aktif etmeniz gerekli")
            }
        }
    }

    fun requestPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }
}