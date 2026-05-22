package com.swx.xizhou.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import com.swx.xizhou.BaseActivity
import com.swx.xizhou.R
import com.swx.xizhou.database.HistoryDBHelper
import com.swx.xizhou.database.HistoryItemDTO
import com.swx.xizhou.database.HistoryMapper
import com.swx.xizhou.database.HistoryType
import com.swx.xizhou.databinding.ActivityHistoryDisplayBinding
import com.swx.xizhou.model.CalendarQRModel
import com.swx.xizhou.util.ImageSaver
import com.swx.xizhou.util.PermissionHelper
import com.swx.xizhou.util.QRCodeGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryDisplayActivity : BaseActivity<ActivityHistoryDisplayBinding>(
    ActivityHistoryDisplayBinding::inflate
) {

    private lateinit var historyMapper: HistoryMapper
    private var currentItem: HistoryItemDTO? = null
    private var currentQRBitmap: Bitmap? = null

    override fun initData() {
        historyMapper = HistoryMapper(this)
        loadHistoryItem()
        PermissionHelper.onPermissionResult += ::onPermissionResult
    }

    override fun initView() {
        setupToolbar()
    }

    override fun initAction() {
        binding.btnSave.setOnClickListener { saveQRCodeToGallery() }
        binding.btnShare.setOnClickListener { shareQRCode() }
        binding.btnOpen.setOnClickListener { openQRCode() }
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun loadHistoryItem() {
        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1L)
        val tableName = intent.getStringExtra(EXTRA_TABLE_NAME) ?: HistoryDBHelper.C_TABLE_NAME
        currentItem = historyMapper.selectById(itemId, tableName)
        currentItem?.let { displayItem(it) } ?: run {
            Toast.makeText(this, getString(R.string.error_record_not_found), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayItem(item: HistoryItemDTO) {
        binding.tvTitle.text = item.title
        setFormatIcon(item.format)
        binding.tvTimestamp.text = formatTimestamp(item.timestamp)
        currentQRBitmap = QRCodeGenerator.generateQRCode(item.content)
        binding.ivQRCode.setImageBitmap(currentQRBitmap)
        binding.tvContent.text = item.content
    }

    private fun setFormatIcon(format: HistoryType) {
        val (iconRes, textRes) = when (format) {
            HistoryType.YOUTUBE -> R.drawable.vector_ic_youtube to R.string.type_youtube
            HistoryType.CALENDAR -> R.drawable.vector_ic_calendar to R.string.type_calendar
            HistoryType.X -> R.drawable.vector_ic_x to R.string.type_x
            HistoryType.TEXT -> R.drawable.vector_ic_result_text to R.string.type_text
        }
        binding.ivTypeIcon.setImageResource(iconRes)
        binding.tvType.setText(textRes)
    }

    private fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    }

    private fun shareQRCode() {
        currentQRBitmap?.let { bitmap ->
            val shareIntent = ImageSaver.getShareIntent(this, bitmap) ?: return
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
        } ?: run {
            Toast.makeText(this, getString(R.string.error_qr_image_not_found), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveQRCodeToGallery() {
        currentQRBitmap?.let { bitmap ->
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
        } ?: run {
            Toast.makeText(this, getString(R.string.qr_code_not_found), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openQRCode(){
        //TODO 提取为顶层方法之类的可以复用
        when(currentItem?.format){
            HistoryType.YOUTUBE->{
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentItem?.content))
                startActivity(intent)
            }

            HistoryType.X->{
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentItem?.content))
                startActivity(intent)
            }

            HistoryType.CALENDAR->{
//                val model = CalendarQRModel.fromString(currentItem.content) ?: run {
//                    Toast.makeText(this, "Invalid calendar data", Toast.LENGTH_SHORT).show()
//                    return
//                }
                //IDE 生成的，kotlin可空也太麻烦了。。。。。
                val model = currentItem?.let { CalendarQRModel.fromString(it.content) }

                val calendar = java.util.Calendar.getInstance()
                var startMillis: Long? = null
                var endMillis: Long? = null

                model?.startTime?.let {
                    calendar.set(it.year, it.monthValue - 1, it.dayOfMonth, it.hour, it.minute, it.second)
                    startMillis = calendar.timeInMillis
                }
                model?.endTime?.let {
                    calendar.set(it.year, it.monthValue - 1, it.dayOfMonth, it.hour, it.minute, it.second)
                    endMillis = calendar.timeInMillis
                }

                val intent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, model?.title)
                    putExtra(CalendarContract.Events.DESCRIPTION, model?.description)
                    putExtra(CalendarContract.Events.EVENT_LOCATION, model?.location)
                    startMillis?.let { putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) }
                    endMillis?.let { putExtra(CalendarContract.EXTRA_EVENT_END_TIME, it) }
                }

                startActivity(intent)
            }
            else -> {
                return
            }
        }

    }

    private fun onPermissionResult(result: PermissionHelper.PermissionResult) {
        if (result.type == PermissionHelper.PermissionType.STORAGE && result.granted) {
            saveQRCodeToGallery()
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

    companion object {
        const val EXTRA_ITEM_ID = "item_id"
        const val EXTRA_TABLE_NAME = "table_name"

        fun start(activity: BaseActivity<*>, itemId: Long, tableName: String) {
            val intent = Intent(activity, HistoryDisplayActivity::class.java).apply {
                putExtra(EXTRA_ITEM_ID, itemId)
                putExtra(EXTRA_TABLE_NAME, tableName)
            }
            activity.startActivity(intent)
        }
    }
}