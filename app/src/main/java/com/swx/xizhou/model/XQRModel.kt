package com.swx.xizhou.model

enum class XType{
    URL,
    USERNAME
}
data class XQRModel(var type: XType= XType.URL, var input: String="") {

    companion object{
        private const val X_BASE_URL = "https://x.com/"

        fun isXLink(url: String): Boolean{
            return url.contains("x.com")
        }
    }

    fun getQRContent(): String {
        if (input.isBlank()) return ""
        return if (isXLink(input)) {
            input
        } else {
            when (type) {
                XType.URL -> input
                XType.USERNAME -> "${X_BASE_URL}${input.removePrefix("@")}"
            }
        }
    }

    fun getID(): String {
        if (!isXLink(input)) return input
        val afterDomain = input.substringAfter("x.com/").substringBefore("?")
        return when {
            afterDomain.contains("/status/") -> {
                // 帖子链接：提取 username/status/id
                afterDomain.substringBefore("/status/")
            }
            afterDomain.isNotEmpty() -> {
                // 用户主页链接：提取 username
                afterDomain.substringBefore("/")
            }
            else -> ""
        }
    }
}