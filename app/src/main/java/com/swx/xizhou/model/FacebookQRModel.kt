package com.swx.xizhou.model

enum class FacebookType {
    URL,
    USERNAME
}

data class FacebookQRModel(
    var type: FacebookType = FacebookType.URL,
    var input: String = ""
) {
    companion object {
        private const val FACEBOOK_BASE_URL = "https://www.facebook.com/"
        private const val FACEBOOK_PROFILE_SCHEME = "fb://profile/"

        fun isFacebookLink(text: String): Boolean {
            val lower = text.lowercase()
            return lower.contains("facebook.com") || lower.startsWith(FACEBOOK_PROFILE_SCHEME)
        }
    }

    fun getQRContent(): String {
        if (input.isBlank()) return ""
        return if (isFacebookLink(input)) {
            input
        } else {
            when (type) {
                FacebookType.URL -> input
                FacebookType.USERNAME -> "$FACEBOOK_BASE_URL${input.removePrefix("@")}"
            }
        }
    }

    fun getID(): String {
        if (input.startsWith(FACEBOOK_PROFILE_SCHEME)) {
            return input.substringAfter(FACEBOOK_PROFILE_SCHEME).substringBefore("?")
        }
        if (!isFacebookLink(input)) return input.removePrefix("@")

        return input.substringAfter("facebook.com/", "")
            .substringBefore("?")
            .substringBefore("/")
            .ifBlank { input }
    }
}
