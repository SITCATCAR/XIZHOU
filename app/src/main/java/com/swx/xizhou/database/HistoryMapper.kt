package com.swx.xizhou.database

import androidx.fragment.app.FragmentActivity

class HistoryMapper(val activity: FragmentActivity?) {

    // 缓存DBHelper实例，避免重复创建
    private val dbHelper: HistoryDBHelper by lazy {
        HistoryDBHelper(activity, 1)
    }

    fun close() {
        dbHelper.close()
    }

    fun hasHistory(tableName: String): Boolean{
        val writDB = dbHelper.writableDatabase
        val cursor = writDB.rawQuery("select 1 from ${tableName}", null)
        val hasData=cursor.moveToFirst()
        cursor.close()
        writDB.close()
        return hasData
    }

    fun insert(dto: HistoryItemDTO,tableName: String){
        val database = dbHelper.writableDatabase
        database.execSQL("insert into ${tableName}(content,format,title,timestamp) values(?,?,?,?)",
            arrayOf(dto.content,dto.format,dto.title,dto.timestamp))
        database.close()
    }

    fun selectAll(tableName: String):List<HistoryItemDTO>{
        var dtoList = mutableListOf<HistoryItemDTO>()
        val readableDatabase = dbHelper.readableDatabase
        val cursor = readableDatabase.rawQuery("select * from ${tableName} order by timestamp desc", null)
        if(cursor.moveToFirst()){
            do{
                val dto = HistoryItemDTO(
                    content = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDBHelper.CONTENT)),
                    format = HistoryType.valueOf(
                        cursor.getString(cursor.getColumnIndexOrThrow(HistoryDBHelper.FORMAT))
                    ),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDBHelper.TITLE)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(HistoryDBHelper.TIMESTAMP))
                )
                dto.id = cursor.getLong(cursor.getColumnIndexOrThrow(HistoryDBHelper.ID))
                dtoList.add(dto)
            }while (cursor.moveToNext())
        }
        cursor.close()
        return dtoList
    }

    /**
     * 根据ID更新历史记录的标题和时间戳
     */
    fun updateTitle(id: Long, newTitle: String, timestamp: Long, tableName: String) {
        val database = dbHelper.writableDatabase
        database.execSQL(
            "update ${tableName} set ${HistoryDBHelper.TITLE}=?, ${HistoryDBHelper.TIMESTAMP}=? where ${HistoryDBHelper.ID}=?",
            arrayOf(newTitle, timestamp, id)
        )
        database.close()
    }


    /**
     * 批量删除历史记录
     */
    fun deleteByIds(ids: Set<Long>, tableName: String) {
        if (ids.isEmpty()) return

        val database = dbHelper.writableDatabase

        // 构建IN查询语句
        val placeholders = ids.joinToString(",") { "?" }
        database.execSQL(
            "delete from ${tableName} where ${HistoryDBHelper.ID} in ($placeholders)",
            ids.toTypedArray()
        )
        database.close()
    }

    /**
     * 根据ID查询单条历史记录
     */
    fun selectById(id: Long, tableName: String): HistoryItemDTO? {
        val readableDatabase = dbHelper.readableDatabase
        val cursor = readableDatabase.rawQuery(
            "select * from ${tableName} where ${HistoryDBHelper.ID}=?",
            arrayOf(id.toString())
        )

        var dto: HistoryItemDTO? = null
        if (cursor.moveToFirst()) {
            dto = HistoryItemDTO(
                content = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDBHelper.CONTENT)),
                format = HistoryType.valueOf(
                    cursor.getString(cursor.getColumnIndexOrThrow(HistoryDBHelper.FORMAT))
                ),
                title = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDBHelper.TITLE)),
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(HistoryDBHelper.TIMESTAMP))
            ).apply {
                this.id = cursor.getLong(cursor.getColumnIndexOrThrow(HistoryDBHelper.ID))
            }
        }
        cursor.close()
        return dto
    }

}