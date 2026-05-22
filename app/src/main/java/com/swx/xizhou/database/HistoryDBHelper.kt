package com.swx.xizhou.database

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.fragment.app.FragmentActivity

class HistoryDBHelper(context: FragmentActivity?, version: Int)
    : SQLiteOpenHelper(context,DB_NAME,null,version) {

    companion object{
        const val DB_NAME="history.db"
        const val C_TABLE_NAME="created"
        const val S_TABLE_NAME="scanned"
        const val ID="id"
        const val CONTENT="content"
        const val FORMAT="format"
        const val TITLE="title"
        const val TIMESTAMP="timestamp"
    }


    override fun onCreate(database: SQLiteDatabase?) {
        database?.execSQL(
            "create table ${C_TABLE_NAME} ("+
            "${ID} INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "${CONTENT} TEXT,"+
            "${FORMAT} TEXT,"+
            "${TITLE} TEXT,"+
            "${TIMESTAMP} INTEGER)"
        )
        database?.execSQL(
            "create table ${S_TABLE_NAME} ("+
            "${ID} INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "${CONTENT} TEXT,"+
            "${FORMAT} TEXT,"+
            "${TITLE} TEXT,"+
            "${TIMESTAMP} INTEGER)"
        )
    }

    override fun onUpgrade(
        database: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        database?.apply{
            execSQL("drop table if exists ${C_TABLE_NAME}")
            execSQL("drop table if exists ${S_TABLE_NAME}")
        }
        onCreate(database)
    }
}