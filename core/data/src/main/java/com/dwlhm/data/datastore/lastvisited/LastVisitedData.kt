package com.dwlhm.data.datastore.lastvisited

data class LastVisitedData(
    val url: String = "",
    val title: String? = "",
    val favicon: String? = "",
    val timestamp: Long = System.currentTimeMillis()
)
