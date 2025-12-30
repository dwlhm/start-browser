package com.dwlhm.datastore.room.tabmanager.internal

import com.dwlhm.datastore.room.tabmanager.api.StoredTab

internal fun TabEntity.toStoredTab() = StoredTab(
    id = id,
    url = url,
    title = title,
)

internal fun StoredTab.toEntity() = StoredTab(
    id = id,
    url = url,
    title = title,
)