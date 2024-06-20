package com.yucox.pillpulse.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yucox.pillpulse.view.MainActivity

class PermissionUtils {
    fun hasPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SET_ALARM
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SCHEDULE_EXACT_ALARM
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.USE_EXACT_ALARM
                ) == PackageManager.PERMISSION_GRANTED

    }

    fun showPermissionsRequest(context: Context): ActivityResultLauncher<Array<String>> {
        val requestPermissionLauncher = (context as MainActivity).registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            ActivityResultCallback { permissions ->
                if (permissions[Manifest.permission.POST_NOTIFICATIONS] == true
                    &&
                    permissions[Manifest.permission.SET_ALARM] == true
                    &&
                    permissions[Manifest.permission.USE_EXACT_ALARM] == true
                    &&
                    permissions[Manifest.permission.SCHEDULE_EXACT_ALARM] == true
                ) {
                } else {
                    val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                context as MainActivity,
                                Manifest.permission.SET_ALARM
                            ) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                context as MainActivity,
                                Manifest.permission.USE_EXACT_ALARM
                            ) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                context as MainActivity,
                                Manifest.permission.SCHEDULE_EXACT_ALARM
                            )
                    if (rationaleRequired) {
                        Toast.makeText(
                            context,
                            "Programın düzgün çalışması için izinleri aktif etmeniz gerekli",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
            }
        )
        return requestPermissionLauncher
    }
}