package com.swx.xizhou.database

data class HistoryItemVO(var type: HistoryType = HistoryType.YOUTUBE,
                         var contentTop: String="",
                         var contentBottom: String="",
                         var timestamp: Long = 0L,
                         var id: Long = -1L) {


}