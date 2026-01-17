package com.dwlhm.browser.session

data class SessionDescriptor(
    val id: String,
    val url: String,
    val title: String,
    val isIncognito: Boolean,
    val isMediaSession: Boolean
)
