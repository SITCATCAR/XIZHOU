package com.swx.xizhou.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class CalendarQRModel constructor(
    var title: String = "",
    var description: String = "",
    var location: String = "",
    var isAllDay: Boolean = false,
    var startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime = LocalDateTime.now().plusHours(1)
) {
    companion object {
        private const val BEGIN_VCALENDAR = "BEGIN:VCALENDAR"
        private const val VERSION = "VERSION:2.0"
        private const val PRODID = "PRODID:-//XIZHOU//Calendar QR Generator//EN"
        private const val BEGIN_VEVENT = "BEGIN:VEVENT"
        private const val END_VEVENT = "END:VEVENT"
        private const val END_VCALENDAR = "END:VCALENDAR"

        private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")


        fun fromString(icsContent: String): CalendarQRModel? {
            // 按行分割
            val lines = icsContent.lines().map { it.trim() }.filter { it.isNotEmpty() }

            var title = ""
            var description = ""
            var location = ""
            var startTimeStr: String? = null
            var endTimeStr: String? = null
            var isAllDay = false

            // 解析每一行
            for (line in lines) {
                when {
                    line.startsWith("SUMMARY:") -> {
                        title = line.substringAfter("SUMMARY:", "")
                    }
                    line.startsWith("DESCRIPTION:") -> {
                        description = line.substringAfter("DESCRIPTION:", "")
                    }
                    line.startsWith("LOCATION:") -> {
                        location = line.substringAfter("LOCATION:", "")
                    }
                    line.startsWith("DTSTART;VALUE=DATE:") -> {
                        startTimeStr = line.substringAfter("DTSTART;VALUE=DATE:", "")
                        isAllDay = true
                    }
                    line.startsWith("DTEND;VALUE=DATE:") -> {
                        endTimeStr = line.substringAfter("DTEND;VALUE=DATE:", "")
                        isAllDay = true
                    }
                    line.startsWith("DTSTART:") -> {
                        startTimeStr = line.substringAfter("DTSTART:", "")
                    }
                    line.startsWith("DTEND:") -> {
                        endTimeStr = line.substringAfter("DTEND:", "")
                    }
                }
            }

            // 验证必要字段
            if (title.isBlank() || startTimeStr == null || endTimeStr == null) {
                return null
            }

            return try {
                if (isAllDay) {
                    val startDate = LocalDate.parse(startTimeStr, DATE_FORMATTER)
                    val endDate = LocalDate.parse(endTimeStr, DATE_FORMATTER)
                    CalendarQRModel(
                        title = title,
                        description = description,
                        location = location,
                        isAllDay = true,
                        startTime = startDate.atStartOfDay(),
                        endTime = endDate.atStartOfDay()
                    )
                } else {
                    CalendarQRModel(
                        title = title,
                        description = description,
                        location = location,
                        startTime = LocalDateTime.parse(startTimeStr, DATETIME_FORMATTER),
                        endTime = LocalDateTime.parse(endTimeStr, DATETIME_FORMATTER)
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }


    fun getQRContent(): String {
        return buildString {
            appendLine(BEGIN_VCALENDAR)
            appendLine(VERSION)
            appendLine(PRODID)
            appendLine(BEGIN_VEVENT)
            appendLine("SUMMARY:$title")
            if (isAllDay) {
                appendLine("DTSTART;VALUE=DATE:${startTime.format(DATE_FORMATTER)}")
                appendLine("DTEND;VALUE=DATE:${endTime.format(DATE_FORMATTER)}")
            } else {
                appendLine("DTSTART:${startTime.format(DATETIME_FORMATTER)}")
                appendLine("DTEND:${endTime.format(DATETIME_FORMATTER)}")
            }
            if (location.isNotBlank()) {
                appendLine("LOCATION:$location")
            }
            if (description.isNotBlank()) {
                appendLine("DESCRIPTION:$description")
            }
            appendLine(END_VEVENT)
            appendLine(END_VCALENDAR)
        }
    }


    fun getID(): String {
        return title.ifBlank { "Untitled" }
    }

    /**
     * 验证输入是否有效
     */
    fun validate(): Boolean {
        if (title.isBlank()) return false

        if (isAllDay) {
            if (endTime.toLocalDate().isBefore(startTime.toLocalDate())) return false
        } else {
            if (endTime.isBefore(startTime)) return false
        }

        return true
    }

    private fun StringBuilder.appendLine(str: String): StringBuilder {
        append(str)
        append("\n")
        return this
    }
}
