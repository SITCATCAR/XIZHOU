package com.swx.xizhou.pages.createPage

enum class CreateItemType{
    X,
    YOUTUBE,
    FACEBOOK,
    CALENDAR
}

data class CreateItem(val name: String, val type: CreateItemType)
