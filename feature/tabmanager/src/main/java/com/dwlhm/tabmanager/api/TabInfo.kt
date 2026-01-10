package com.dwlhm.tabmanager.api

data class TabInfo(
    val id: String,
    val title: String,
    val url: String,
    val mode: TabMode,
)
