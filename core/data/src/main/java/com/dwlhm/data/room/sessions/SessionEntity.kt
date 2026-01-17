package com.dwlhm.data.room.sessions

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "sessions",
    primaryKeys = ["id"],
    indices = [
        Index("id"),
        Index("updatedAt")
    ]
)
data class SessionEntity(
    val id: String,
    val url: String,
    val title: String?,
    val favicon: String?,
    val isIncognito: Boolean,
    val isMediaSession: Boolean,

    val createdAt: Long,
    val updatedAt: Long,
)
