package com.dwlhm.datastore.room.tabmanager.internal

import androidx.room.Entity

@Entity(
    tableName = "tabs",
    primaryKeys = ["id"]
)
data class TabEntity(
    val id: String,
    val url: String,
    val title: String?,
)