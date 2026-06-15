package com.swx.xizhou.pages.createPage

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.swx.xizhou.R
import com.swx.xizhou.activity.CalenderCreateActivity
import com.swx.xizhou.activity.FacebookCreateActivity
import com.swx.xizhou.activity.XCreateActivity
import com.swx.xizhou.activity.YoutubeCreateActivity
import com.swx.xizhou.database.HistoryType

data class CreateTypeDefinition(
    val type: CreateItemType,
    val historyType: HistoryType,
    @StringRes val nameRes: Int,
    @DrawableRes val iconRes: Int,
    val activityClass: Class<out Activity>
)

object CreateTypes {

    val all = listOf(
        CreateTypeDefinition(
            type = CreateItemType.X,
            historyType = HistoryType.X,
            nameRes = R.string.create_type_x,
            iconRes = R.drawable.vector_ic_x,
            activityClass = XCreateActivity::class.java
        ),
        CreateTypeDefinition(
            type = CreateItemType.YOUTUBE,
            historyType = HistoryType.YOUTUBE,
            nameRes = R.string.create_type_youtube,
            iconRes = R.drawable.vector_ic_youtube,
            activityClass = YoutubeCreateActivity::class.java
        ),
        CreateTypeDefinition(
            type = CreateItemType.FACEBOOK,
            historyType = HistoryType.FACEBOOK,
            nameRes = R.string.create_type_facebook,
            iconRes = R.drawable.vector_ic_facebook,
            activityClass = FacebookCreateActivity::class.java
        ),
        CreateTypeDefinition(
            type = CreateItemType.CALENDAR,
            historyType = HistoryType.CALENDAR,
            nameRes = R.string.create_type_calendar,
            iconRes = R.drawable.vector_ic_calendar,
            activityClass = CalenderCreateActivity::class.java
        )
    )

    fun getByCreateType(type: CreateItemType): CreateTypeDefinition {
        return all.first { it.type == type }
    }

    fun getByHistoryType(type: HistoryType): CreateTypeDefinition? {
        return all.firstOrNull { it.historyType == type }
    }
}
