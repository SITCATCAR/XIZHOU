package com.swx.xizhou.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.swx.xizhou.event.Event

object PermissionHelper {

    val onPermissionResult = Event<PermissionResult>()

    enum class PermissionType {
        CAMERA,
        STORAGE
    }

    data class PermissionResult(
        val type: PermissionType,
        val granted: Boolean
    )

    const val REQUEST_CODE_CAMERA = 1001
    const val REQUEST_CODE_STORAGE = 1002

    fun isGranted(context: Context, type: PermissionType): Boolean {
        return when (type) {
            PermissionType.CAMERA -> ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            PermissionType.STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) true
                else ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun request(activity: Activity, type: PermissionType) {
        val permissions = when (type) {
            PermissionType.CAMERA -> arrayOf(Manifest.permission.CAMERA)
            PermissionType.STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) emptyArray()
                else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions, getRequestCode(type))
        }
    }

    fun request(fragment: Fragment,type: PermissionType){
        val permissions = when (type) {
            PermissionType.CAMERA -> arrayOf(Manifest.permission.CAMERA)
            PermissionType.STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) emptyArray()
                else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if (permissions.isNotEmpty()) {
            fragment.requestPermissions(permissions,getRequestCode(type))
        }
    }

    fun handleResult(requestCode: Int, grantResults: IntArray) {
        val type = getPermissionType(requestCode) ?: return
        val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        onPermissionResult.invoke(PermissionResult(type, granted))
    }

    private fun getRequestCode(type: PermissionType) = when (type) {
        PermissionType.CAMERA -> REQUEST_CODE_CAMERA
        PermissionType.STORAGE -> REQUEST_CODE_STORAGE
    }

    private fun getPermissionType(code: Int) = when (code) {
        REQUEST_CODE_CAMERA -> PermissionType.CAMERA
        REQUEST_CODE_STORAGE -> PermissionType.STORAGE
        else -> null
    }
}