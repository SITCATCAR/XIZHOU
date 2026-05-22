package com.swx.xizhou.pages.createPage

enum class CreateItemType{
    X,
    Youtube,
    Calender
}

class CreateItem(val name: String,val type: CreateItemType) {
}