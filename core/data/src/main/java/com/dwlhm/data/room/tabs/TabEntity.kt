package com.dwlhm.data.room.tabs

import androidx.room.Entity

@Entity(
    tableName = "tabs",
    primaryKeys = ["id"]
)
data class TabEntity(
    val id: String,
    val url: String,
    val title: String?,
    val favicon: String?,
    val createdAt: String,
    val updatedAt: String,
)