package com.dwlhm.session.api

import com.dwlhm.browser.session.SessionDescriptor
import com.dwlhm.data.room.sessions.SessionEntity

fun SessionEntity.toDescriptor() = SessionDescriptor(
    id = id,
    url = url,
    title = title ?: "No Title",
    isIncognito = isIncognito,
    isMediaSession = isMediaSession
)

fun SessionDescriptor.toEntity(createdAt: Long, updatedAt: Long) = SessionEntity(
    id = id,
    url = url,
    title = title,
    isIncognito = isIncognito,
    isMediaSession = isMediaSession,
    favicon = "",
    createdAt = createdAt,
    updatedAt = updatedAt,
)