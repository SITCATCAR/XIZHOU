package com.swx.xizhou.activity

import android.view.View
import androidx.core.content.ContextCompat
import com.swx.xizhou.R
import com.swx.xizhou.database.HistoryType
import com.swx.xizhou.databinding.ActivityXcreateBinding
import com.swx.xizhou.model.XQRModel
import com.swx.xizhou.model.XType

class XCreateActivity : BaseQRCodeCreateActivity<ActivityXcreateBinding>(ActivityXcreateBinding::inflate) {

    private val model = XQRModel()
    override val historyType = HistoryType.X

    override fun initView() {
        binding.ivBack.setOnClickListener { finish() }
        updateHint()
        updateModeSelectionUI()
        binding.btnSave.visibility = View.GONE
        binding.btnShare.visibility = View.GONE
    }

    override fun initAction() {
        setUpModeSelection()
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

    private fun updateHint() {
        val hint = when (model.type) {
            XType.URL -> getString(R.string.hint_x_url)
            XType.USERNAME -> getString(R.string.hint_x_username)
        }
        binding.etInput.hint = hint
    }

    private fun updateModeSelectionUI() {
        binding.tvUrl.background = null
        binding.tvUsername.background = null
        binding.tvUrl.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        binding.tvUsername.setTextColor(ContextCompat.getColor(this, android.R.color.black))

        val selectedBg = ContextCompat.getDrawable(this, R.drawable.bg_mode_selected)
        val selectedColor = ContextCompat.getColor(this, android.R.color.white)

        when (model.type) {
            XType.URL -> {
                binding.tvUrl.background = selectedBg
                binding.tvUrl.setTextColor(selectedColor)
            }
            XType.USERNAME -> {
                binding.tvUsername.background = selectedBg
                binding.tvUsername.setTextColor(selectedColor)
            }
        }
    }

    private fun selectMode(type: XType) {
        model.type = type
        updateHint()
        updateModeSelectionUI()
    }

    private fun setUpModeSelection() {
        binding.tvUrl.setOnClickListener { selectMode(XType.URL) }
        binding.tvUsername.setOnClickListener { selectMode(XType.USERNAME) }
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
