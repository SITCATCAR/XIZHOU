package com.swx.xizhou.activity

import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.viewbinding.ViewBinding
import com.swx.xizhou.BaseActivity
import com.swx.xizhou.R
import com.swx.xizhou.database.HistoryDBHelper
import com.swx.xizhou.database.HistoryItemDTO
import com.swx.xizhou.database.HistoryMapper
import com.swx.xizhou.database.HistoryType
import com.swx.xizhou.pages.historyPage.HistoryPagerFragment
import com.swx.xizhou.util.ImageSaver
import com.swx.xizhou.util.PermissionHelper
import com.swx.xizhou.util.QRCodeGenerator

abstract class BaseQRCodeCreateActivity<VB : ViewBinding>(
    inflate: (LayoutInflater) -> VB
) : BaseActivity<VB>(inflate) {

    protected lateinit var historyMapper: HistoryMapper
    protected var currentQRBitmap: Bitmap? = null

    protected abstract val historyType: HistoryType

    override fun initData() {
        historyMapper = HistoryMapper(this)
        PermissionHelper.onPermissionResult += ::onPermissionResult
    }

    protected fun generateQRCode(
        content: String,
        title: String,
        qrImageView: ImageView,
        saveButton: AppCompatButton,
        shareButton: AppCompatButton,
        successMessage: Int = R.string.toast_insert_success
    ): Boolean {
        currentQRBitmap = QRCodeGenerator.generateQRCode(content)
        val bitmap = currentQRBitmap ?: run {
            Toast.makeText(this, getString(R.string.error_generation_failed), Toast.LENGTH_SHORT).show()
            return false
        }

        qrImageView.setImageBitmap(bitmap)
        saveButton.visibility = View.VISIBLE
        shareButton.visibility = View.VISIBLE
        saveToHistory(content, title)
        Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
        return true
    }

    protected fun saveQRCodeToGallery(bitmap: Bitmap) {
        when (val result = ImageSaver.saveToGallery(this, bitmap)) {
            is ImageSaver.SaveResult.Success -> {
                Toast.makeText(this, getString(R.string.saved_to_gallery), Toast.LENGTH_SHORT).show()
            }
            is ImageSaver.SaveResult.PermissionRequired -> {
                PermissionHelper.request(this, PermissionHelper.PermissionType.STORAGE)
            }
            is ImageSaver.SaveResult.Error -> {
                Toast.makeText(this, getString(R.string.save_failed, result.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    protected fun shareQRCode() {
        currentQRBitmap?.let { bitmap ->
            val shareIntent = ImageSaver.getShareIntent(this, bitmap) ?: return
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
        }
    }

    private fun saveToHistory(content: String, title: String) {
        val dto = HistoryItemDTO(
            content = content,
            format = historyType,
            title = title,
            timestamp = System.currentTimeMillis()
        )
        historyMapper.insert(dto, HistoryDBHelper.C_TABLE_NAME)
        HistoryPagerFragment.onItemChangeEvent.invoke(Unit)
    }

    private fun onPermissionResult(result: PermissionHelper.PermissionResult) {
        if (result.type == PermissionHelper.PermissionType.STORAGE && result.granted) {
            currentQRBitmap?.let { saveQRCodeToGallery(it) }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.handleResult(requestCode, grantResults)
    }

    override fun onDestroy() {
        PermissionHelper.onPermissionResult -= ::onPermissionResult
        super.onDestroy()
    }
}
