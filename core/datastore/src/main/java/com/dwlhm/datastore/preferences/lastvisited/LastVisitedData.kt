package com.dwlhm.datastore.preferences.lastvisited

data class LastVisitedData(
    val url: String = "",
    val title: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
