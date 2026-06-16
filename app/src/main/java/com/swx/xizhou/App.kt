package com.swx.xizhou

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class App : Application() {

    companion object {
        const val SP_NAME = "xizhouPrefs"

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}
