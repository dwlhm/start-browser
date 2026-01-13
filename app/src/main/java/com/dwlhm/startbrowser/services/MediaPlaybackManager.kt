package com.dwlhm.startbrowser.services

import android.content.Context
import android.content.Intent
import com.dwlhm.browser.BrowserMediaSession
import com.dwlhm.browser.BrowserMediaState
import com.dwlhm.event.EventDispatcher
import com.dwlhm.event.MediaActivatedEvent
import com.dwlhm.event.MediaDeactivatedEvent
import com.dwlhm.event.MediaMetadataChangedEvent
import com.dwlhm.event.MediaStateChangedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * Manager untuk media playback background.
 * 
 * Prinsip Arsitektur:
 * - Single Source of Truth: Semua state dan control flow melalui sini
 * - Unidirectional Data Flow: Event → Manager → Service
 * - Clean Separation: Manager handle logic, Service handle UI (notification)
 * 
 * Tanggung Jawab:
 * 1. Listen media events dari browser
 * 2. Manage service lifecycle (start/stop)
 * 3. Forward state changes ke service
 * 
 * @param context Application context
 * @param scope CoroutineScope untuk collect events
 */
class MediaPlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    // === State ===
    private var isServiceRunning = false
    private var activeMediaTabId: String? = null
    private var currentMediaSession: BrowserMediaSession? = null
    
    // === Deactivation Debounce ===
    // Mencegah race condition saat view re-attach yang menyebabkan
    // rapid deactivate → activate sequence
    private var deactivationJob: Job? = null
    
    companion object {
        /**
         * Delay sebelum benar-benar stop service saat media deactivated.
         * 
         * Alasan: Saat view re-attach, GeckoView bisa fire onMediaDeactivated
         * diikuti onMediaActivated dalam waktu singkat. Delay ini memberikan
         * waktu untuk activation event membatalkan deactivation.
         * 
         * Nilai 300ms dipilih karena:
         * - Cukup pendek untuk tidak terasa oleh user
         * - Cukup panjang untuk handle race condition
         */
        private const val DEACTIVATION_DELAY_MS = 300L
    }
    
    // === Event Collection ===
    private val eventJobs = mutableListOf<Job>()
    
    /**
     * Inisialisasi manager dan mulai observe events.
     */
    fun initialize() {
        observeMediaActivation()
        observeMediaDeactivation()
        observeMediaStateChanges()
        observeMediaMetadataChanges()
    }
    
    /**
     * Cleanup semua resources.
     */
    fun destroy() {
        // Cancel pending deactivation
        deactivationJob?.cancel()
        deactivationJob = null
        
        cancelAllEventJobs()
        stopService()
        clearState()
    }
    
    // === Event Observers ===
    
    /**
     * Observe event saat media diaktifkan (mulai play pertama kali).
     */
    private fun observeMediaActivation() {
        val job = scope.launch {
            EventDispatcher.events
                .filterIsInstance<MediaActivatedEvent>()
                .collect { event -> handleMediaActivated(event) }
        }
        eventJobs.add(job)
    }
    
    /**
     * Observe event saat media di-deactivate.
     */
    private fun observeMediaDeactivation() {
        val job = scope.launch {
            EventDispatcher.events
                .filterIsInstance<MediaDeactivatedEvent>()
                .collect { event -> handleMediaDeactivated(event) }
        }
        eventJobs.add(job)
    }
    
    /**
     * Observe perubahan state playback (play/pause/stop).
     */
    private fun observeMediaStateChanges() {
        val job = scope.launch {
            EventDispatcher.events
                .filterIsInstance<MediaStateChangedEvent>()
                .collect { event -> handleMediaStateChanged(event) }
        }
        eventJobs.add(job)
    }
    
    /**
     * Observe perubahan metadata (title, artist, etc).
     */
    private fun observeMediaMetadataChanges() {
        val job = scope.launch {
            EventDispatcher.events
                .filterIsInstance<MediaMetadataChangedEvent>()
                .collect { event -> handleMediaMetadataChanged(event) }
        }
        eventJobs.add(job)
    }
    
    // === Event Handlers ===
    
    /**
     * Handle media activation.
     * Start service dan kirim initial state.
     * 
     * PENTING: Cancel pending deactivation untuk handle race condition
     * saat view re-attach (deactivate → activate dalam waktu singkat).
     * 
     * NOTE: Saat onMediaActivated, kita belum tahu apakah media PLAY atau PAUSE.
     * State sebenarnya akan datang dari MediaStateChangedEvent.
     * Jika service belum running, biarkan MediaStateChangedEvent yang start service
     * dengan state yang benar.
     */
    private fun handleMediaActivated(event: MediaActivatedEvent) {
        // Cancel pending deactivation - activation takes priority
        deactivationJob?.cancel()
        deactivationJob = null
        
        // Update internal state
        activeMediaTabId = event.tabId
        currentMediaSession = event.mediaSession
        
        // Setup bridge untuk passing non-serializable data
        MediaPlaybackServiceBridge.setMediaSession(event.mediaSession)
        
        // TIDAK start service di sini - biarkan MediaStateChangedEvent yang start
        // dengan state yang benar (PLAY atau PAUSE)
        // Ini mencegah race condition dimana service start dengan PAUSE default
        // padahal state sebenarnya adalah PLAY
    }
    
    /**
     * Handle media deactivation.
     * Stop service jika tidak ada media aktif.
     * 
     * PENTING: Menggunakan debounce untuk mencegah race condition.
     * Saat view re-attach, GeckoView bisa fire deactivated → activated
     * dalam waktu singkat. Debounce memberikan waktu untuk activation
     * event membatalkan deactivation.
     */
    private fun handleMediaDeactivated(event: MediaDeactivatedEvent) {
        // Verify ini adalah tab yang aktif
        if (event.tabId != activeMediaTabId) return
        
        // Cancel previous deactivation job jika ada
        deactivationJob?.cancel()
        
        // Debounce: delay sebelum benar-benar stop service
        deactivationJob = scope.launch {
            delay(DEACTIVATION_DELAY_MS)
            
            // Double check - mungkin sudah di-activate lagi
            if (event.tabId != activeMediaTabId) return@launch
            
            // Clear state
            activeMediaTabId = null
            currentMediaSession = null
            
            // Stop service
            stopService()
            
            // Clear bridge
            MediaPlaybackServiceBridge.clear()
        }
    }
    
    /**
     * Handle perubahan state playback.
     * 
     * PENTING: 
     * - PLAY state juga cancel pending deactivation untuk memastikan service tidak stop
     * - Saat start service, initial state disertakan untuk mencegah race condition
     */
    private fun handleMediaStateChanged(event: MediaStateChangedEvent) {
        when (event.state) {
            BrowserMediaState.PLAY -> {
                // Cancel pending deactivation - media masih aktif
                deactivationJob?.cancel()
                deactivationJob = null
                
                // Jika service belum running, start dengan state PLAY langsung
                // Ini mencegah race condition dimana service start dengan PAUSE default
                if (!isServiceRunning) {
                    activeMediaTabId = event.tabId
                    currentMediaSession = event.mediaSession
                    MediaPlaybackServiceBridge.setMediaSession(event.mediaSession)
                    startServiceWithInitialState(event.tabId, event.state)
                    // Tidak perlu updateServiceState - state sudah di-include di initialize
                } else {
                    // Service sudah running, update state saja
                    updateServiceState(event.state)
                }
            }
            
            BrowserMediaState.PAUSE -> {
                // Jika service belum running, start dengan state PAUSE
                if (!isServiceRunning) {
                    activeMediaTabId = event.tabId
                    currentMediaSession = event.mediaSession
                    MediaPlaybackServiceBridge.setMediaSession(event.mediaSession)
                    startServiceWithInitialState(event.tabId, event.state)
                } else {
                    // Service sudah running, update state saja
                    updateServiceState(event.state)
                }
            }
            
            BrowserMediaState.STOP -> {
                // Verify ini adalah tab yang aktif
                if (event.tabId == activeMediaTabId) {
                    // Cancel pending deactivation
                    deactivationJob?.cancel()
                    deactivationJob = null
                    
                    stopService()
                    clearState()
                }
            }
        }
    }
    
    /**
     * Handle perubahan metadata.
     */
    private fun handleMediaMetadataChanged(event: MediaMetadataChangedEvent) {
        // Ignore jika service tidak running
        if (!isServiceRunning) return
        
        // Setup artwork di bridge (tidak bisa via Intent)
        MediaPlaybackServiceBridge.setArtwork(event.mediaMetadata.artwork)
        
        // Kirim metadata ke service
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_UPDATE_METADATA
            putExtra(MediaPlaybackService.EXTRA_TITLE, event.mediaMetadata.title)
            putExtra(MediaPlaybackService.EXTRA_ARTIST, event.mediaMetadata.artist)
            putExtra(MediaPlaybackService.EXTRA_ALBUM, event.mediaMetadata.album)
        }
        context.startService(intent)
    }
    
    // === Service Control ===
    
    /**
     * Start service dengan initial state.
     * 
     * @param tabId ID tab yang sedang memutar media
     * @param initialState State awal playback (PLAY/PAUSE)
     * 
     * PENTING: Initial state di-include dalam intent untuk mencegah race condition.
     * Sebelumnya, service start dengan PAUSE default, kemudian UPDATE_STATE(PLAY)
     * dikirim terpisah. Jika UPDATE_STATE sampai sebelum INITIALIZE diproses,
     * state PLAY akan di-drop karena currentState masih null.
     */
    private fun startServiceWithInitialState(tabId: String, initialState: BrowserMediaState) {
        if (isServiceRunning) return
        
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_INITIALIZE
            putExtra(MediaPlaybackService.EXTRA_TAB_ID, tabId)
            putExtra(MediaPlaybackService.EXTRA_STATE, initialState.name)
        }
        
        context.startForegroundService(intent)
        isServiceRunning = true
    }
    
    /**
     * Update state di service.
     */
    private fun updateServiceState(state: BrowserMediaState) {
        if (!isServiceRunning) return
        
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_UPDATE_STATE
            putExtra(MediaPlaybackService.EXTRA_STATE, state.name)
        }
        context.startService(intent)
    }
    
    /**
     * Stop service.
     */
    fun stopService() {
        if (!isServiceRunning) return
        
        val intent = Intent(context, MediaPlaybackService::class.java)
        context.stopService(intent)
        isServiceRunning = false
    }
    
    // === Helper Methods ===
    
    /**
     * Cancel semua event collection jobs.
     */
    private fun cancelAllEventJobs() {
        eventJobs.forEach { it.cancel() }
        eventJobs.clear()
    }
    
    /**
     * Clear semua internal state.
     */
    private fun clearState() {
        activeMediaTabId = null
        currentMediaSession = null
        MediaPlaybackServiceBridge.clear()
    }
}
