package com.swx.xizhou.activity

import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.swx.xizhou.R
import com.swx.xizhou.database.HistoryType
import com.swx.xizhou.databinding.ActivityCalenderCreateBinding
import com.swx.xizhou.model.CalendarQRModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CalenderCreateActivity : BaseQRCodeCreateActivity<ActivityCalenderCreateBinding>(
    ActivityCalenderCreateBinding::inflate
) {

    @RequiresApi(Build.VERSION_CODES.O)
    private val model = CalendarQRModel()
    override val historyType = HistoryType.CALENDAR

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

        generateQRCode(
            content = model.getQRContent(),
            title = model.getID(),
            qrImageView = binding.ivQRCode,
            saveButton = binding.btnSave,
            shareButton = binding.btnShare,
            successMessage = R.string.calendar_generated
        )
    }
}
