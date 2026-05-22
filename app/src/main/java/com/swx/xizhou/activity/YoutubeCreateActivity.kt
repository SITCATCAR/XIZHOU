package com.swx.xizhou.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.swx.xizhou.BaseActivity
import com.swx.xizhou.R
import com.swx.xizhou.database.HistoryDBHelper
import com.swx.xizhou.database.HistoryItemDTO
import com.swx.xizhou.database.HistoryMapper
import com.swx.xizhou.database.HistoryType
import com.swx.xizhou.databinding.ActivityYoutubeCreateBinding
import com.swx.xizhou.model.YoutubeType
import com.swx.xizhou.model.YoutubeQRModel
import com.swx.xizhou.pages.historyPage.HistoryPagerFragment
import com.swx.xizhou.util.ImageSaver
import com.swx.xizhou.util.PermissionHelper
import com.swx.xizhou.util.QRCodeGenerator

class YoutubeCreateActivity : BaseActivity<ActivityYoutubeCreateBinding>(
    ActivityYoutubeCreateBinding::inflate
) {

    private val model = YoutubeQRModel()
    private lateinit var historyMapper: HistoryMapper
    private var currentQRBitmap: Bitmap? = null

    override fun initView() {
        setupToolbar()
        updateHint()
        binding.btnSave.visibility = View.GONE
        binding.btnShare.visibility = View.GONE
    }

    override fun initData() {
        historyMapper = HistoryMapper(this)
        PermissionHelper.onPermissionResult += ::onPermissionResult
    }

    override fun initAction() {
        setupModeSelection()
        binding.btnGenerate.setOnClickListener {
            generateQRCode()
            val dto = HistoryItemDTO(model.getQRContent(),
                HistoryType.YOUTUBE, model.getID(), System.currentTimeMillis())
            historyMapper.insert(dto, HistoryDBHelper.C_TABLE_NAME)
            HistoryPagerFragment.onItemChangeEvent.invoke(Unit)
            Toast.makeText(this, getString(R.string.toast_insert_success), Toast.LENGTH_SHORT).show()
        }
        binding.btnSave.setOnClickListener {
            currentQRBitmap?.let { saveQRCodeToGallery(it) }
        }
        binding.btnShare.setOnClickListener {
            currentQRBitmap?.let { bitmap ->
                val shareIntent = ImageSaver.getShareIntent(this, bitmap) ?: return@let
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
            }
        }
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun setupModeSelection() {
        binding.tvUrl.setOnClickListener { selectMode(YoutubeType.URL) }
        binding.tvVideo.setOnClickListener { selectMode(YoutubeType.VIDEO) }
        binding.tvChannel.setOnClickListener { selectMode(YoutubeType.CHANNEL) }
    }

    private fun selectMode(type: YoutubeType) {
        model.type = type
        updateHint()
        updateModeSelectionUI()
    }

    private fun updateHint() {
        val hint = when (model.type) {
            YoutubeType.URL -> getString(R.string.hint_youtube_url)
            YoutubeType.VIDEO -> getString(R.string.hint_youtube_video_id)
            YoutubeType.CHANNEL -> getString(R.string.hint_youtube_channel_id)
        }
        binding.etInput.hint = hint
    }

    private fun updateModeSelectionUI() {
        binding.tvUrl.background = null
        binding.tvVideo.background = null
        binding.tvChannel.background = null
        binding.tvUrl.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        binding.tvVideo.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        binding.tvChannel.setTextColor(ContextCompat.getColor(this, android.R.color.black))

        val selectedBg = ContextCompat.getDrawable(this, R.drawable.bg_mode_selected)
        val selectedColor = ContextCompat.getColor(this, android.R.color.white)

        when (model.type) {
            YoutubeType.URL -> {
                binding.tvUrl.background = selectedBg
                binding.tvUrl.setTextColor(selectedColor)
            }
            YoutubeType.VIDEO -> {
                binding.tvVideo.background = selectedBg
                binding.tvVideo.setTextColor(selectedColor)
            }
            YoutubeType.CHANNEL -> {
                binding.tvChannel.background = selectedBg
                binding.tvChannel.setTextColor(selectedColor)
            }
        }
    }

    private fun generateQRCode() {
        val input = binding.etInput.text?.toString()?.trim() ?: ""
        if (input.isEmpty()) {
            binding.etInput.error = getString(R.string.error_empty_input)
            return
        }

        model.input = input
        currentQRBitmap = QRCodeGenerator.generateQRCode(model.getQRContent())
        if (currentQRBitmap != null) {
            binding.ivQRCode.setImageBitmap(currentQRBitmap)
            binding.btnSave.visibility = View.VISIBLE
            binding.btnShare.visibility = View.VISIBLE
        }
    }

    private fun saveQRCodeToGallery(bitmap: Bitmap) {
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
        super.onDestroy()
        PermissionHelper.onPermissionResult -= ::onPermissionResult
    }
}