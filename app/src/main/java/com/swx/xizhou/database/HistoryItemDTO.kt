package com.swx.xizhou.database
enum class HistoryType{
    X,
    YOUTUBE,
    CALENDAR,
    TEXT
}
data class HistoryItemDTO(var content: String="",
                          var format: HistoryType= HistoryType.YOUTUBE,
                          var title: String="",
                          var timestamp: Long=0L,
                          var id: Long = 0L){

}
