package com.swx.xizhou.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.swx.xizhou.App

object SPUtil {
    const val KEY_BEEP_ENABLED = "beep_enabled"
    const val DEFAULT_BEEP_ENABLED = true

    fun getInstance(): SPUtil {
        return this
    }

    fun contains(key: String, context: Context = App.context): Boolean {
        return getSharedPreferences(context).contains(key)
    }

    fun remove(key: String, context: Context = App.context) {
        getSharedPreferences(context).edit { remove(key) }
    }

    fun clear(context: Context = App.context) {
        getSharedPreferences(context).edit { clear() }
    }

    fun getAll(context: Context = App.context): Map<String, *> {
        return getSharedPreferences(context).all
    }

    fun set(key: String, value: Any, context: Context = App.context) {
        getSharedPreferences(context).edit {
            when (value) {
                is Int -> putInt(key, value)
                is String -> putString(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is Boolean -> putBoolean(key, value)
                is Set<*> -> putStringSet(key, value.toStringSet(key))
                else -> throw IllegalArgumentException("SPUtil does not support ${value::class.java.simpleName}")
            }
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false, context: Context = App.context): Boolean {
        return getSharedPreferences(context).getBoolean(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int = 0, context: Context = App.context): Int {
        return getSharedPreferences(context).getInt(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float = 0F, context: Context = App.context): Float {
        return getSharedPreferences(context).getFloat(key, defaultValue)
    }

    fun getString(key: String, defaultValue: String = "", context: Context = App.context): String {
        return getSharedPreferences(context).getString(key, defaultValue) ?: defaultValue
    }

    fun getLong(key: String, defaultValue: Long = 0L, context: Context = App.context): Long {
        return getSharedPreferences(context).getLong(key, defaultValue)
    }

    fun getStringSet(
        key: String,
        defaultValue: Set<String> = emptySet(),
        context: Context = App.context
    ): Set<String> {
        return getSharedPreferences(context).getStringSet(key, defaultValue)?.toSet() ?: defaultValue
    }

    fun get(key: String, defaultValue: Boolean = false, context: Context = App.context): Boolean {
        return getBoolean(key, defaultValue, context)
    }

    fun get(key: String, defaultValue: Int = 0, context: Context = App.context): Int {
        return getInt(key, defaultValue, context)
    }

    fun get(key: String, defaultValue: Float = 0F, context: Context = App.context): Float {
        return getFloat(key, defaultValue, context)
    }

    fun get(key: String, defaultValue: String = "", context: Context = App.context): String {
        return getString(key, defaultValue, context)
    }

    fun get(key: String, defaultValue: Long = 0L, context: Context = App.context): Long {
        return getLong(key, defaultValue, context)
    }

    fun get(key: String, defaultValue: Set<String> = emptySet(), context: Context = App.context): Set<String> {
        return getStringSet(key, defaultValue, context)
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(App.SP_NAME, Context.MODE_PRIVATE)
    }

    private fun Set<*>.toStringSet(key: String): Set<String> {
        return map {
            it as? String ?: throw IllegalArgumentException("SPUtil only supports Set<String>, key=$key")
        }.toSet()
    }
}
