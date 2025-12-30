package com.dwlhm.tabmanager.internal

import com.dwlhm.tabmanager.api.SessionNavigator
import com.dwlhm.tabmanager.api.TabManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TabSessionNavigatorImpl @Inject constructor(
    private val tabManager: TabManager
): SessionNavigator {

    override val activeSession = tabManager.activeSession

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentUrl = activeSession
        .flatMapLatest { session ->
            session?.currentUrl ?: flowOf("")
        }
        .stateIn(
            scope,
            started = SharingStarted.Companion.Eagerly,
            initialValue = ""
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentTitle = activeSession
        .flatMapLatest { session ->
            session?.currentTitle ?: flowOf("")
        }
        .stateIn(
            scope,
            started = SharingStarted.Companion.Eagerly,
            initialValue = ""
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val canGoBack = activeSession
        .flatMapLatest { session ->
            session?.canGoBack ?: flowOf(false)
        }
        .stateIn(
            scope,
            started = SharingStarted.Companion.Eagerly,
            initialValue = false
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val canGoForward = activeSession
        .flatMapLatest { session ->
            session?.canGoForward ?: flowOf(false)
        }
        .stateIn(
            scope,
            started = SharingStarted.Companion.Eagerly,
            initialValue = false
        )

    override fun loadUrl(url: String) {
        val session = activeSession.value
        if (session != null) {
            session.loadUrl(url)
        } else {
            // Belum ada session aktif, buat tab baru
            tabManager.addTab(url)
        }
    }

    override fun openInNewTab(url: String) {
        tabManager.addTab(url)
    }

    override fun goBack(): Boolean {
        val session = activeSession.value ?: return false
        if (!canGoBack.value) return false
        session.goBack()
        return true
    }

    override fun goForward(): Boolean {
        val session = activeSession.value ?: return false
        if (!canGoForward.value) return false
        session.goForward()
        return true
    }
}