package com.dwlhm.data.browser

import android.content.Context
import com.dwlhm.datastore.preferences.LastVisitedPrefs
import com.dwlhm.domain.browser.LastVisitedData
import com.dwlhm.domain.browser.LastVisitedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LastVisitedRepositoryImpl(
    private val context: Context
) : LastVisitedRepository {
    
    override fun getLastVisited(): Flow<LastVisitedData> {
        return LastVisitedPrefs.lastVisitedFlow(context).map { prefsData ->
            LastVisitedData(
                url = prefsData.url,
                title = prefsData.title
            )
        }
    }
    
    override suspend fun getLastVisitedOnce(): LastVisitedData {
        val prefsData = LastVisitedPrefs.lastVisited(context)
        return LastVisitedData(
            url = prefsData.url,
            title = prefsData.title
        )
    }
    
    override suspend fun saveLastVisited(url: String, title: String) {
        LastVisitedPrefs.setLastVisited(context, url, title)
    }
}

