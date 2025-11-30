package com.dwlhm.home.internal

import com.dwlhm.home.api.Home
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepository @Inject constructor() {
    fun getUserName(userId: String): Flow<Home> = flow {
        emit(Home(name = "Jhon Sinabar"))
    }

}