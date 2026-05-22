package com.swx.xizhou.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.swx.xizhou.BaseActivity
import com.swx.xizhou.R
import com.swx.xizhou.database.HistoryDBHelper
import com.swx.xizhou.database.HistoryItemDTO
import com.swx.xizhou.database.HistoryMapper
import com.swx.xizhou.database.HistoryType
import com.swx.xizhou.databinding.ActivityCalenderCreateBinding
import com.swx.xizhou.model.CalendarQRModel
import com.swx.xizhou.pages.historyPage.HistoryPagerFragment
import com.swx.xizhou.util.ImageSaver
import com.swx.xizhou.util.PermissionHelper
import com.swx.xizhou.util.QRCodeGenerator
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CalenderCreateActivity : BaseActivity<ActivityCalenderCreateBinding>(
    ActivityCalenderCreateBinding::inflate
) {

    @RequiresApi(Build.VERSION_CODES.O)
    private val model = CalendarQRModel()
    private lateinit var historyMapper: HistoryMapper
    private var currentQRBitmap: Bitmap? = null

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun initView() {
        setupToolbar()
        setupDateTimePickers()
        updateDateTimeDisplays()

        binding.btnSave.visibility = View.GONE
        binding.btnShare.visibility = View.GONE

        binding.switchAllDay.setOnCheckedChangeListener { _, isChecked ->
            model.isAllDay = isChecked
            updateTimeVisibility(isChecked)
        }
    }

    override fun initData() {
        historyMapper = HistoryMapper(this)

        PermissionHelper.onPermissionResult += ::onPermissionResult
    }

    override fun initAction() {
        binding.btnGenerate.setOnClickListener {
            generateQRCode()
        }
        binding.btnSave.setOnClickListener {
            currentQRBitmap?.let { bitmap ->
                saveQRCodeToGallery(bitmap)
            }
        }
        binding.btnShare.setOnClickListener {
            shareQRCode()
        }
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupDateTimePickers() {
        binding.btnStartDate.setOnClickListener { showStartDatePicker() }
        binding.btnStartTime.setOnClickListener { showStartTimePicker() }
        binding.btnEndDate.setOnClickListener { showEndDatePicker() }
        binding.btnEndTime.setOnClickListener { showEndTimePicker() }
    }

    private fun showStartDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(model.startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val localDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
            model.startTime = model.startTime.with(localDate)
            updateStartDateDisplay()
        }
        datePicker.show(supportFragmentManager, "START_DATE_PICKER")
    }

    private fun showStartTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H)
            .setHour(model.startTime.hour)
            .setMinute(model.startTime.minute)
            .setTitleText(getString(R.string.select_time))
            .build()
        timePicker.addOnPositiveButtonClickListener {
            model.startTime = model.startTime.withHour(timePicker.hour).withMinute(timePicker.minute)
            updateStartTimeDisplay()
        }
        timePicker.show(supportFragmentManager, "START_TIME_PICKER")
    }

    private fun showEndDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(model.endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val localDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
            model.endTime = model.endTime.with(localDate)
            updateEndDateDisplay()
        }
        datePicker.show(supportFragmentManager, "END_DATE_PICKER")
    }

    private fun showEndTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H)
            .setHour(model.endTime.hour)
            .setMinute(model.endTime.minute)
            .setTitleText(getString(R.string.select_time))
            .build()
        timePicker.addOnPositiveButtonClickListener {
            model.endTime = model.endTime.withHour(timePicker.hour).withMinute(timePicker.minute)
            updateEndTimeDisplay()
        }
        timePicker.show(supportFragmentManager, "END_TIME_PICKER")
    }

    private fun updateDateTimeDisplays() {
        updateStartDateDisplay()
        updateStartTimeDisplay()
        updateEndDateDisplay()
        updateEndTimeDisplay()
    }

    private fun updateStartDateDisplay() { binding.tvStartDate.text = model.startTime.format(dateFormatter) }
    private fun updateStartTimeDisplay() { binding.tvStartTime.text = model.startTime.format(timeFormatter) }
    private fun updateEndDateDisplay() { binding.tvEndDate.text = model.endTime.format(dateFormatter) }
    private fun updateEndTimeDisplay() { binding.tvEndTime.text = model.endTime.format(timeFormatter) }

    private fun updateTimeVisibility(isAllDay: Boolean) {
        val visibility = if (isAllDay) View.GONE else View.VISIBLE
        binding.btnStartTime.visibility = visibility
        binding.btnEndTime.visibility = visibility
        binding.tvStartTime.visibility = visibility
        binding.tvEndTime.visibility = visibility
    }

    private fun generateQRCode() {
        model.title = binding.etTitle.text.toString().trim()
        model.location = binding.etLocation.text.toString().trim()
        model.description = binding.etDescription.text.toString().trim()

        if (!model.validate()) {
            if (model.title.isBlank()) binding.etTitle.error = getString(R.string.error_empty_title)
            if (model.endTime.isBefore(model.startTime)) {
                Toast.makeText(this, R.string.error_end_before_start, Toast.LENGTH_SHORT).show()
            }
            return
        }

        val content = model.getQRContent()
        currentQRBitmap = QRCodeGenerator.generateQRCode(content)

        if (currentQRBitmap != null) {
            binding.ivQRCode.setImageBitmap(currentQRBitmap)
            binding.btnSave.visibility = View.VISIBLE
            binding.btnShare.visibility = View.VISIBLE
            Toast.makeText(this, R.string.calendar_generated, Toast.LENGTH_SHORT).show()
            saveToHistory()
        } else {
            Toast.makeText(this, getString(R.string.error_generation_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToHistory() {
        val dto = HistoryItemDTO(
            content = model.getQRContent(),
            format = HistoryType.CALENDAR,
            title = model.getID(),
            timestamp = System.currentTimeMillis()
        )
        historyMapper.insert(dto, HistoryDBHelper.C_TABLE_NAME)
        HistoryPagerFragment.onItemChangeEvent.invoke(Unit)
    }

    private fun shareQRCode() {
        currentQRBitmap?.let { bitmap ->
            val shareIntent = ImageSaver.getShareIntent(this, bitmap) ?: return
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
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