package com.swx.xizhou.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageSaver {
    private const val IMAGE_FOLDER = "XIZHOU_QR"
    private const val FILENAME_PREFIX = "QR_"

    /**
     * 保存二维码图片到相册
     */
    fun saveToGallery(context: Context, bitmap: Bitmap): SaveResult {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStoreModern(context, bitmap)
        } else {
            saveViaMediaStoreLegacy(context, bitmap)
        }
    }

    /**
     * 使用现代MediaStore API保存图片
     */
    private fun saveViaMediaStoreModern(context: Context, bitmap: Bitmap): SaveResult {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, generateFileName())
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/$IMAGE_FOLDER")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        return uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(it, contentValues, null, null)
                SaveResult.Success(it.toString())
            } catch (e: Exception) {
                SaveResult.Error(e.message ?: "Unknown error")
            }
        } ?: SaveResult.Error("Failed to create media entry")
    }

    /**
     * 使用传统方式保存图片
     */
    private fun saveViaMediaStoreLegacy(context: Context, bitmap: Bitmap): SaveResult {
        // 检查权限
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PermissionChecker.PERMISSION_GRANTED) {
            return SaveResult.PermissionRequired
        }

        val imagesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ).toString() + "/$IMAGE_FOLDER"

        val folder = File(imagesDir)
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val file = File(folder, generateFileName())
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            // 通知媒体扫描器
            val mediaScanIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = android.net.Uri.fromFile(file)
            }
            context.sendBroadcast(mediaScanIntent)
            SaveResult.Success(file.absolutePath)
        } catch (e: Exception) {
            SaveResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * 生成带时间戳的文件名
     */
    private fun generateFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "$FILENAME_PREFIX$timestamp.png"
    }

    /**
     * 创建分享QR码图片的Intent
     */
    fun getShareIntent(context: Context, bitmap: Bitmap): Intent? {
        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(shareDir, "qr_share.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    sealed class SaveResult {
        data class Success(val path: String) : SaveResult()
        data class Error(val message: String) : SaveResult()
        object PermissionRequired : SaveResult()
    }
}
