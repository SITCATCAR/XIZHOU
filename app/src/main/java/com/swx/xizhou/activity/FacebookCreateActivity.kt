package com.swx.xizhou.activity

import android.view.View
import androidx.core.content.ContextCompat
import com.swx.xizhou.R
import com.swx.xizhou.database.HistoryType
import com.swx.xizhou.databinding.ActivityXcreateBinding
import com.swx.xizhou.model.FacebookQRModel
import com.swx.xizhou.model.FacebookType

class FacebookCreateActivity : BaseQRCodeCreateActivity<ActivityXcreateBinding>(
    ActivityXcreateBinding::inflate
) {

    private val model = FacebookQRModel()
    override val historyType = HistoryType.FACEBOOK

    override fun initView() {
        binding.ivBack.setOnClickListener { finish() }
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

    private fun setupModeSelection() {
        binding.tvUrl.setOnClickListener { selectMode(FacebookType.URL) }
        binding.tvUsername.setOnClickListener { selectMode(FacebookType.USERNAME) }
    }

    private fun selectMode(type: FacebookType) {
        model.type = type
        updateHint()
        updateModeSelectionUI()
    }

    private fun updateHint() {
        val hint = when (model.type) {
            FacebookType.URL -> getString(R.string.hint_facebook_url)
            FacebookType.USERNAME -> getString(R.string.hint_facebook_username)
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
            FacebookType.URL -> {
                binding.tvUrl.background = selectedBg
                binding.tvUrl.setTextColor(selectedColor)
            }
            FacebookType.USERNAME -> {
                binding.tvUsername.background = selectedBg
                binding.tvUsername.setTextColor(selectedColor)
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
