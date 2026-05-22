package com.swx.xizhou.model

enum class YoutubeType {
    VIDEO,    // 视频 ID
    CHANNEL,  // 频道 ID
    URL       // 完整 URL
}

data class YoutubeQRModel(
    var type: YoutubeType = YoutubeType.VIDEO,
    var input: String = ""
) {
    companion object {
        private const val YOUTUBE_URL = "https://www.youtube.com/watch?v="
        private const val YOUTUBE_CHANNEL = "https://www.youtube.com/channel/"

        public fun isYoutubeLink(text: String): Boolean {
            val lower = text.lowercase()
            return lower.contains("youtube.com") || lower.contains("youtu.be")
        }
    }

    fun getQRContent(): String {
        if (input.isBlank()) return ""

        return if (isYoutubeLink(input)) {
            input
        } else {
            when (type) {
                YoutubeType.VIDEO -> "$YOUTUBE_URL$input"
                YoutubeType.CHANNEL -> "$YOUTUBE_CHANNEL$input"
                YoutubeType.URL -> "$YOUTUBE_URL$input"
            }
        }
    }
    fun getID(): String{
        if(!isYoutubeLink(input))
            return input
        return when (type) {
            // 视频ID提取
            YoutubeType.VIDEO -> {
                when {
                    input.contains("watch?v=") -> {
                        input.substringAfter("watch?v=")
                            .substringBefore("&")
                    }

                    input.contains("youtu.be/") -> {
                        input.substringAfter("youtu.be/")
                            .substringBefore("?")
                    }

                    else -> ""
                }
            }
            // 频道ID提取
            YoutubeType.CHANNEL -> {
                if (input.contains("/channel/")) {
                    input.substringAfter("/channel/")
                        .substringBefore("?")
                        .substringBefore("/")
                } else {
                    ""
                }
            }
            // URL模式默认按视频处理
            YoutubeType.URL -> {
                when {
                    input.contains("watch?v=") -> {
                        input.substringAfter("watch?v=")
                            .substringBefore("&")
                    }

                    input.contains("youtu.be/") -> {
                        input.substringAfter("youtu.be/")
                            .substringBefore("?")
                    }

                    else -> ""
                }
            }
        }
    }
}
