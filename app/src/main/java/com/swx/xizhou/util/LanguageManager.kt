package com.swx.xizhou.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.annotation.StringRes
import com.swx.xizhou.R
import java.util.Locale

object LanguageManager {
    const val LANGUAGE_SYSTEM = ""
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_CHINESE = "zh"
    const val LANGUAGE_ARABIC = "ar"

    private const val PREF_NAME = "xizhou_settings"
    private const val KEY_LANGUAGE = "language"

    val languageOptions = listOf(
        LanguageOption(
            code = LANGUAGE_SYSTEM,
            nameRes = R.string.language_system,
            nativeNameRes = R.string.language_system_desc,
            tagRes = R.string.language_system_tag,
            flagRes = R.string.language_system_flag,
            locale = null
        ),
        LanguageOption(
            code = LANGUAGE_ENGLISH,
            nameRes = R.string.language_english,
            nativeNameRes = R.string.language_english_native,
            tagRes = R.string.language_english_tag,
            flagRes = R.string.language_english_flag,
            locale = Locale.ENGLISH
        ),
        LanguageOption(
            code = LANGUAGE_CHINESE,
            nameRes = R.string.language_chinese,
            nativeNameRes = R.string.language_chinese_native,
            tagRes = R.string.language_chinese_tag,
            flagRes = R.string.language_chinese_flag,
            locale = Locale.SIMPLIFIED_CHINESE
        ),
        LanguageOption(
            code = LANGUAGE_ARABIC,
            nameRes = R.string.language_arabic,
            nativeNameRes = R.string.language_arabic_native,
            tagRes = R.string.language_arabic_tag,
            flagRes = R.string.language_arabic_flag,
            locale = Locale.forLanguageTag("ar")
        )
    )

    fun getLanguage(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    fun wrapContext(context: Context): ContextWrapper {
        val language = getLanguage(context)
        val locale = getLocale(language)
        if (locale == null) {
            return ContextWrapper(context)
        }

        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return ContextWrapper(context.createConfigurationContext(configuration))
    }

    fun getLanguageName(context: Context, language: String = getLanguage(context)): String {
        val option = getLanguageOption(language)
        return context.getString(option.nameRes)
    }

    fun getLanguageOption(language: String): LanguageOption {
        return languageOptions.firstOrNull { it.code == language } ?: languageOptions.first()
    }

    private fun getLocale(language: String): Locale? {
        return languageOptions.firstOrNull { it.code == language }?.locale
    }
}

data class LanguageOption(
    val code: String,
    @param:StringRes val nameRes: Int,
    @param:StringRes val nativeNameRes: Int,
    @param:StringRes val tagRes: Int,
    @param:StringRes val flagRes: Int,
    val locale: Locale?
)
