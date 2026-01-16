package com.dwlhm.tabmanager.api

import com.dwlhm.browser.BrowserMediaMetadata
import com.dwlhm.browser.BrowserMediaSession
import com.dwlhm.browser.BrowserMediaState
import com.dwlhm.browser.BrowserSessionCallback
import com.dwlhm.browser.BrowserTab
import com.dwlhm.event.EventDispatcher
import com.dwlhm.event.MediaActivatedEvent
import com.dwlhm.event.MediaDeactivatedEvent
import com.dwlhm.event.MediaMetadataChangedEvent
import com.dwlhm.event.MediaStateChangedEvent
import com.dwlhm.event.TabClosedEvent
import com.dwlhm.event.TabCreatedEvent
import com.dwlhm.event.TabInfoChangedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.util.UUID

class TabSessionManager(
    private val tabRegistry: TabManagerRegistry,
    private val tabMode: TabMode,
    private val eventDispatcher: EventDispatcher,
    private val mediaStateRegistry: com.dwlhm.media.MediaStateRegistry? = null
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    val allTabs = MutableStateFlow<MutableMap<String, TabHandle>>(mutableMapOf())
    val selectedTab = MutableStateFlow<TabHandle?>(null)
    private var lastSelectedTab: TabHandle? = null
    
    // Track last known media state untuk sync saat kembali dari notification
    // (untuk backward compatibility dengan code yang masih pakai ini)
    private var lastKnownMediaState: BrowserMediaState? = null
    private var lastKnownMediaSession: BrowserMediaSession? = null

    init {
        observeSelectedTab()
    }

    fun createTab(initialUrl: String) {
        val manager = tabRegistry.manager(tabMode)
        val session = manager.newSession()
        val viewHost = manager.provideViewHost()
        val id = UUID.randomUUID().toString()

        session.loadUrl(initialUrl)

        val currentTabHandle = TabHandle(id, session, viewHost, tabMode)

        eventDispatcher.dispatch(
            TabCreatedEvent(
                tabId = id,
                initialUrl = initialUrl
            )
        )

        allTabs.update { allTabs ->
            allTabs[id] = currentTabHandle
            allTabs
        }

        selectedTab.update { currentTabHandle }
    }

    fun openTab(browserTab: BrowserTab) {
        val tabId = browserTab.id
        val requestedTab = allTabs.value[tabId]

        if (requestedTab != null) {
            // Update DefaultTabManager untuk mengganti _currentTab ke session yang benar
            // Ini penting karena saat GeckoView di-render ulang, viewHost.attach() 
            // akan memanggil manager.attach() yang menggunakan _currentTab
            val manager = tabRegistry.manager(requestedTab.mode)
            manager.acquire(requestedTab.session)
            
            selectedTab.update { requestedTab }
        } else {
            // Tab not in memory, restore it with the same ID
            val manager = tabRegistry.manager(tabMode)
            val session = manager.newSession()
            val viewHost = manager.provideViewHost()
            session.loadUrl(browserTab.url)

            val restoredTabHandle = TabHandle(tabId, session, viewHost, tabMode)

            allTabs.update { tabs ->
                tabs[tabId] = restoredTabHandle
                tabs
            }

            selectedTab.update { restoredTabHandle }
        }
    }

    fun closeTab(id: String) {
        if (selectedTab.value?.id == id) {
            selectedTab.value?.session?.destroy()

            allTabs.update { tabs ->
                tabs.remove(id)
                tabs
            }

            selectedTab.update { null }

            eventDispatcher.dispatch(
                TabClosedEvent(
                    tabId = id
                )
            )
        }
    }

    /**
     * Menangguhkan tab yang sedang aktif saat user meninggalkan browser view.
     * 
     * Perilaku:
     * - Jika tab sedang memutar media: tetap aktif (keepActive=true) agar media jalan di background
     * - Jika tab tidak memutar media: sepenuhnya suspended (keepActive=false)
     * 
     * Dipanggil saat user navigasi ke dashboard atau screen lain.
     */
    fun suspendCurrentTab() {
        val currentTab = selectedTab.value ?: return

        val isPlayingMedia = mediaStateRegistry?.isPlaying(currentTab.id) ?: currentTab.session.hasActiveMedia
        
        val stateBeforeSuspend = lastKnownMediaState
        val sessionBeforeSuspend = lastKnownMediaSession
        val tabIdBeforeSuspend = currentTab.id

        currentTab.session.suspendSession(keepActive = isPlayingMedia)

        if (isPlayingMedia && stateBeforeSuspend != null && sessionBeforeSuspend != null) {
            scope.launch {
                kotlinx.coroutines.delay(50)
                
                val currentTabAfterDelay = selectedTab.value
                if (currentTabAfterDelay != null && 
                    currentTabAfterDelay.id == tabIdBeforeSuspend &&
                    currentTabAfterDelay.session.hasActiveMedia) {
                    eventDispatcher.dispatch(
                        MediaStateChangedEvent(
                            tabId = tabIdBeforeSuspend,
                            state = stateBeforeSuspend,
                            mediaSession = sessionBeforeSuspend
                        )
                    )
                }
            }
        }
    }
    
    /**
     * Sync state media saat kembali ke browser dari notification.
     * @param tabId ID tab dari notification intent
     */
    fun syncMediaStateFromNotification(tabId: String) {
        val currentTab = selectedTab.value ?: return
        
        if (currentTab.id != tabId) return
        
        if (!currentTab.session.hasActiveMedia) return
        
        val state = lastKnownMediaState ?: return
        val mediaSession = lastKnownMediaSession ?: return
        
        eventDispatcher.dispatch(
            MediaStateChangedEvent(
                tabId = tabId,
                state = state,
                mediaSession = mediaSession
            )
        )
    }

    private fun observeSelectedTab() {
        selectedTab.onEach { tabHandle ->

            lastSelectedTab?.session?.sessionCallback = null

            if (tabHandle == null) {
                lastSelectedTab = null
                return@onEach
            }

            lastSelectedTab = tabHandle

            tabHandle.session.sessionCallback = object : BrowserSessionCallback {
                private var _mediaSession: BrowserMediaSession? = null

                override fun onTabInfoChanged(title: String, url: String) {
                    eventDispatcher.dispatch(
                        TabInfoChangedEvent(
                            tabId = tabHandle.id,
                            title = title,
                            url = url
                        )
                    )
                }

                override fun onMediaActivated(mediaSession: BrowserMediaSession) {
                    _mediaSession = mediaSession
                    lastKnownMediaSession = mediaSession
                    
                    eventDispatcher.dispatch(
                        MediaActivatedEvent(
                            tabId = tabHandle.id,
                            mediaSession,
                        )
                    )
                }

                override fun onMediaDeactivated() {
                    lastKnownMediaState = null
                    lastKnownMediaSession = null
                    
                    eventDispatcher.dispatch(
                        MediaDeactivatedEvent(
                            tabId = tabHandle.id,
                        )
                    )
                }

                override fun onMediaMetadataChanged(mediaMetadata: BrowserMediaMetadata) {
                    val session = _mediaSession ?: lastKnownMediaSession
                    if (session == null) return
                    
                    eventDispatcher.dispatch(
                        MediaMetadataChangedEvent(
                            tabId = tabHandle.id,
                            mediaMetadata = mediaMetadata,
                            metadataSession = session
                        )
                    )
                }

                override fun onMediaStateChanged(state: BrowserMediaState) {
                    val session = _mediaSession ?: lastKnownMediaSession
                    if (session == null) return
                    
                    lastKnownMediaState = state
                    
                    eventDispatcher.dispatch(
                        MediaStateChangedEvent(
                            tabId = tabHandle.id,
                            state = state,
                            mediaSession = session
                        )
                    )
                }
            }
        }.launchIn(scope)
    }
}