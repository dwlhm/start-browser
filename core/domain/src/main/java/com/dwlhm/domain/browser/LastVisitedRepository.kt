package com.dwlhm.domain.browser

import kotlinx.coroutines.flow.Flow

/**
 * Data class representing last visited page info
 */
data class LastVisitedData(
    val url: String = "",
    val title: String = ""
)

interface LastVisitedRepository {
    /**
     * Get the last visited data as a Flow for reactive updates
     */
    fun getLastVisited(): Flow<LastVisitedData>
    
    /**
     * Get the last visited data synchronously
     */
    suspend fun getLastVisitedOnce(): LastVisitedData
    
    /**
     * Save the last visited URL and title
     */
    suspend fun saveLastVisited(url: String, title: String)
}

