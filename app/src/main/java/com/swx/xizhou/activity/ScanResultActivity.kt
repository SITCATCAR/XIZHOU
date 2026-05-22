package com.swx.xizhou.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.swx.xizhou.BaseActivity
import com.swx.xizhou.R
import com.swx.xizhou.databinding.ActivityScanResultBinding
import com.swx.xizhou.model.CalendarQRModel
import com.swx.xizhou.model.XQRModel
import com.swx.xizhou.model.YoutubeQRModel

class ScanResultActivity : BaseActivity<ActivityScanResultBinding>(
    ActivityScanResultBinding::inflate
) {

    private var scanResult: String = ""
    private var scanType: Int = TYPE_UNKNOWN

    override fun initData() {
        scanResult = intent.getStringExtra(EXTRA_SCAN_RESULT) ?: ""
        scanType = intent.getIntExtra(EXTRA_SCAN_TYPE, TYPE_UNKNOWN)
    }

    override fun initView() {
        setupToolbar()
        displayContent()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initAction() {
        binding.btnPrimary.setOnClickListener {
            when (scanType) {
                TYPE_YOUTUBE -> openYoutube()
                TYPE_X -> openX()
                TYPE_CALENDAR -> addToCalendar()
                TYPE_TEXT -> openAsText()
                else -> shareContent()
            }
        }
        binding.btnSecondary.setOnClickListener {
            shareContent()
        }
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun displayContent() {
        binding.tvContent.text = scanResult

        when (scanType) {
            TYPE_YOUTUBE -> {
                binding.ivTypeIcon.setImageResource(R.drawable.vector_ic_youtube)
                binding.tvTypeLabel.setText(R.string.result_youtube)
                binding.tvTitle.text = YoutubeQRModel().apply { input = scanResult }.getID()
                binding.btnPrimary.setText(R.string.open)
            }
            TYPE_X -> {
                binding.ivTypeIcon.setImageResource(R.drawable.vector_ic_x)
                binding.tvTypeLabel.setText(R.string.result_x)
                binding.tvTitle.text = XQRModel().apply { input = scanResult }.getID()
                binding.btnPrimary.setText(R.string.open)
            }
            TYPE_CALENDAR -> {
                binding.ivTypeIcon.setImageResource(R.drawable.vector_ic_calendar)
                binding.tvTypeLabel.setText(R.string.result_calendar)
                val model = CalendarQRModel.fromString(scanResult)
                binding.tvTitle.text = model?.title ?: getString(R.string.calendar_event_title)
                binding.btnPrimary.setText(R.string.add_to_calendar)
            }
            TYPE_TEXT, TYPE_UNKNOWN -> {
                binding.ivTypeIcon.setImageResource(R.drawable.vector_ic_result_text)
                binding.tvTypeLabel.setText(R.string.result_text)
                binding.tvTitle.text = scanResult.take(50)
                binding.btnPrimary.setText(R.string.open)
            }
            else -> {
                binding.ivTypeIcon.setImageResource(R.drawable.vector_ic_result_text)
                binding.tvTypeLabel.setText(R.string.result_text)
                binding.tvTitle.text = scanResult.take(50)
                binding.btnPrimary.setText(R.string.share)
            }
        }
    }

    private fun openYoutube() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scanResult))
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addToCalendar() {
        val model = CalendarQRModel.fromString(scanResult) ?: run {
            Toast.makeText(this, getString(R.string.error_invalid_calendar_data), Toast.LENGTH_SHORT).show()
            return
        }

        val calendar = java.util.Calendar.getInstance()
        var startMillis: Long? = null
        var endMillis: Long? = null

        model.startTime?.let {
            calendar.set(it.year, it.monthValue - 1, it.dayOfMonth, it.hour, it.minute, it.second)
            startMillis = calendar.timeInMillis
        }
        model.endTime?.let {
            calendar.set(it.year, it.monthValue - 1, it.dayOfMonth, it.hour, it.minute, it.second)
            endMillis = calendar.timeInMillis
        }

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, model.title)
            putExtra(CalendarContract.Events.DESCRIPTION, model.description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, model.location)
            startMillis?.let { putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) }
            endMillis?.let { putExtra(CalendarContract.EXTRA_EVENT_END_TIME, it) }
        }

        startActivity(intent)
    }

    private fun openAsText() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scanResult))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            shareContent()
        }
    }

    private fun openX() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scanResult))
        startActivity(intent)
    }

    private fun shareContent() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, scanResult)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    companion object {
        const val EXTRA_SCAN_RESULT = "scan_result"
        const val EXTRA_SCAN_TYPE = "scan_type"
        const val TYPE_UNKNOWN = 0
        const val TYPE_YOUTUBE = 1
        const val TYPE_CALENDAR = 2
        const val TYPE_TEXT = 3
        const val TYPE_X = 4
    }
}