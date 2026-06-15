package com.swx.xizhou.activity

import android.view.View
import androidx.core.content.ContextCompat
import com.swx.xizhou.R
import com.swx.xizhou.database.HistoryType
import com.swx.xizhou.databinding.ActivityYoutubeCreateBinding
import com.swx.xizhou.model.YoutubeType
import com.swx.xizhou.model.YoutubeQRModel

class YoutubeCreateActivity : BaseQRCodeCreateActivity<ActivityYoutubeCreateBinding>(
    ActivityYoutubeCreateBinding::inflate
) {

    private val model = YoutubeQRModel()
    override val historyType = HistoryType.YOUTUBE

    override fun initView() {
        setupToolbar()
        updateHint()
        updateModeSelectionUI()
        binding.btnSave.visibility = View.GONE
        binding.btnShare.visibility = View.GONE
    }

    override fun initAction() {
        setupModeSelection()
        binding.btnGenerate.setOnClickListener {
            generateQRCode()
        }
        binding.btnSave.setOnClickListener {
            currentQRBitmap?.let { saveQRCodeToGallery(it) }
        }
        binding.btnShare.setOnClickListener {
            shareQRCode()
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
            YoutubeType.CHANNEL -> getString(R.string.hint_youtube_channel_url)
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
        generateQRCode(
            content = model.getQRContent(),
            title = model.getID(),
            qrImageView = binding.ivQRCode,
            saveButton = binding.btnSave,
            shareButton = binding.btnShare
        )
    }
}
