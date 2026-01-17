package com.dwlhm.dashboardsession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.dwlhm.browser.session.SessionDescriptor
import com.dwlhm.browser.session.SessionManager
import com.dwlhm.browser.session.SessionRegistry
import com.dwlhm.utils.normalizeUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardSessionViewModel(
    private val sessionRegistry: SessionRegistry,
    private val sessionManager: SessionManager,
    private val navController: NavHostController,
): ViewModel() {
    private val _inputUrl = MutableStateFlow("")
    val inputUrl: StateFlow<String> = _inputUrl

    val sessions: StateFlow<List<SessionDescriptor>> = sessionRegistry.sessions

    fun onInputChange(url: String) {
        _inputUrl.value = url
    }

    fun onSubmit(url: String) {
        val normalized = normalizeUrl(url)
        sessionManager.createSession(normalized, false)
        _inputUrl.value = ""
        navController.navigate("browser")
    }

    fun onSessionClick(selectedSession: SessionDescriptor) {
        viewModelScope.launch {
            sessionManager.openSession(selectedSession.id)
            navController.navigate("browser")
        }
    }

    fun onSessionClose(selectedSession: SessionDescriptor) {
        sessionRegistry.removeSession(selectedSession.id)
        sessionManager.closeSession(selectedSession.id)
    }
}