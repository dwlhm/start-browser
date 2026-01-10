package com.dwlhm.utils

fun isValidUrl(url: String): Boolean = URL_REGEX.matches(url)

fun normalizeUrl(
    url: String,
    fallbackStrategy: (url: String) -> String = { "https://google.com/search?q=$it" }
): String {
    val isValidUrl: Boolean = isValidUrl(url)
    return if (isValidUrl) url else fallbackStrategy(url)
}