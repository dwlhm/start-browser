package com.dwlhm.startbrowser.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.dwlhm.browser.BrowserMediaSession
import com.dwlhm.browser.BrowserMediaState
import com.dwlhm.startbrowser.services.media.MediaNotificationBuilder
import com.dwlhm.startbrowser.services.media.MediaPlaybackState
import com.dwlhm.startbrowser.services.media.MediaSessionController

/**
 * Foreground service untuk background media playback.
 * 
 * Prinsip Arsitektur:
 * - Thin Orchestrator: Hanya mengkoordinasi komponen lain
 * - Tidak menyimpan logic kompleks
 * - State dikelola via MediaPlaybackState (immutable)
 * - Notification dibangun oleh MediaNotificationBuilder
 * - MediaSession dikelola oleh MediaSessionController
 * 
 * Flow:
 * 1. Service menerima state dari MediaPlaybackManager via Intent
 * 2. State disimpan di currentState (single source of truth di service)
 * 3. Komponen (notification, media session) di-update berdasarkan state
 * 4. User actions dari notification diteruskan ke BrowserMediaSession
 */
class MediaPlaybackService : Service() {
    
    // === State ===
    private var currentState: MediaPlaybackState? = null
    
    // === Components ===
    private lateinit var notificationBuilder: MediaNotificationBuilder
    private lateinit var sessionController: MediaSessionController
    
    // === Lifecycle ===
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize components
        notificationBuilder = MediaNotificationBuilder(this)
        sessionController = MediaSessionController(this)
        
        // Setup notification channel
        notificationBuilder.createNotificationChannel()
        
        // Initialize media session dengan callback
        sessionController.initialize(createMediaSessionCallback())
    }
    
    override fun onDestroy() {
        // Cleanup
        sessionController.release()
        currentState = null
        
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return START_NOT_STICKY
    }
    
    // === Intent Handling ===
    
    /**
     * Handle semua intent yang masuk.
     * Setiap intent membawa informasi untuk update state atau trigger action.
     */
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            // User actions dari notification
            ACTION_PLAY -> handlePlayAction()
            ACTION_PAUSE -> handlePauseAction()
            ACTION_STOP -> handleStopAction()
            ACTION_PREVIOUS -> handlePreviousAction()
            ACTION_NEXT -> handleNextAction()
            
            // State updates dari Manager
            ACTION_INITIALIZE -> handleInitialize(intent)
            ACTION_UPDATE_STATE -> handleUpdateState(intent)
            ACTION_UPDATE_METADATA -> handleUpdateMetadata(intent)
        }
    }
    
    /**
     * Initialize service dengan state awal.
     * Dipanggil pertama kali saat media diaktifkan.
     * 
     * PENTING: Initial state (PLAY/PAUSE) di-include dalam intent untuk mencegah
     * race condition. Ini memastikan notification langsung menampilkan state yang benar.
     */
    private fun handleInitialize(intent: Intent) {
        val tabId = intent.getStringExtra(EXTRA_TAB_ID) ?: return
        val mediaSession = MediaPlaybackServiceBridge.getMediaSession() ?: return
        
        // Ambil initial state dari intent, default ke PAUSE jika tidak ada
        val initialStateName = intent.getStringExtra(EXTRA_STATE)
        val initialState = if (initialStateName != null) {
            try {
                BrowserMediaState.valueOf(initialStateName)
            } catch (_: IllegalArgumentException) {
                BrowserMediaState.PAUSE
            }
        } else {
            BrowserMediaState.PAUSE
        }
        
        // Buat initial state dengan playback state yang benar
        currentState = MediaPlaybackState(
            tabId = tabId,
            mediaSession = mediaSession,
            playbackState = initialState
        )
        
        // Update media session dengan state awal
        sessionController.updatePlaybackState(initialState)
        
        // Start foreground dengan notification awal
        startForegroundWithNotification()
    }
    
    /**
     * Update playback state (play/pause/stop).
     */
    private fun handleUpdateState(intent: Intent) {
        val state = currentState ?: return
        val stateName = intent.getStringExtra(EXTRA_STATE) ?: return
        val newPlaybackState = BrowserMediaState.valueOf(stateName)
        
        // Update state
        currentState = state.withPlaybackState(newPlaybackState)
        
        // Update komponen
        sessionController.updatePlaybackState(newPlaybackState)
        updateNotification()
    }
    
    /**
     * Update metadata (title, artist, album, artwork).
     */
    private fun handleUpdateMetadata(intent: Intent) {
        val state = currentState ?: return
        
        val title = intent.getStringExtra(EXTRA_TITLE)
        val artist = intent.getStringExtra(EXTRA_ARTIST)
        val album = intent.getStringExtra(EXTRA_ALBUM)
        val artwork = MediaPlaybackServiceBridge.consumeArtwork()
        
        // Update state
        currentState = state.withMetadata(title, artist, album, artwork)
        
        // Update komponen
        currentState?.let { newState ->
            sessionController.updateMetadata(newState)
            updateNotification()
        }
    }
    
    // === User Actions ===
    
    private fun handlePlayAction() {
        currentState?.mediaSession?.play()
    }
    
    private fun handlePauseAction() {
        currentState?.mediaSession?.pause()
    }
    
    private fun handleStopAction() {
        currentState?.mediaSession?.stop()
        stopSelf()
    }
    
    private fun handlePreviousAction() {
        currentState?.mediaSession?.previousTrack()
    }
    
    private fun handleNextAction() {
        currentState?.mediaSession?.nextTrack()
    }
    
    // === Notification ===
    
    private fun startForegroundWithNotification() {
        val state = currentState ?: return
        
        val notification = notificationBuilder.buildNotification(
            state = state,
            mediaSessionToken = sessionController.sessionToken,
            serviceClass = MediaPlaybackService::class.java
        )
        
        startForeground(MediaNotificationBuilder.NOTIFICATION_ID, notification)
    }
    
    private fun updateNotification() {
        val state = currentState ?: return
        
        val notification = notificationBuilder.buildNotification(
            state = state,
            mediaSessionToken = sessionController.sessionToken,
            serviceClass = MediaPlaybackService::class.java
        )
        
        notificationBuilder.updateNotification(notification)
    }
    
    // === Media Session Callback ===
    
    /**
     * Callback untuk aksi dari MediaSession (headphone controls, dll).
     */
    private fun createMediaSessionCallback(): MediaSessionController.MediaSessionCallback {
        return object : MediaSessionController.MediaSessionCallback {
            override fun onPlay() {
                handlePlayAction()
            }
            
            override fun onPause() {
                handlePauseAction()
            }
            
            override fun onStop() {
                handleStopAction()
            }
            
            override fun onSkipToNext() {
                handleNextAction()
            }
            
            override fun onSkipToPrevious() {
                handlePreviousAction()
            }
        }
    }
    
    // === Constants ===
    
    companion object {
        // Actions dari notification buttons
        const val ACTION_PLAY = "com.dwlhm.startbrowser.ACTION_PLAY"
        const val ACTION_PAUSE = "com.dwlhm.startbrowser.ACTION_PAUSE"
        const val ACTION_STOP = "com.dwlhm.startbrowser.ACTION_STOP"
        const val ACTION_PREVIOUS = "com.dwlhm.startbrowser.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.dwlhm.startbrowser.ACTION_NEXT"
        
        // Actions dari Manager
        const val ACTION_INITIALIZE = "com.dwlhm.startbrowser.ACTION_INITIALIZE"
        const val ACTION_UPDATE_STATE = "com.dwlhm.startbrowser.ACTION_UPDATE_STATE"
        const val ACTION_UPDATE_METADATA = "com.dwlhm.startbrowser.ACTION_UPDATE_METADATA"
        
        // Extras
        const val EXTRA_TAB_ID = "extra_tab_id"
        const val EXTRA_STATE = "extra_state"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_ALBUM = "extra_album"
    }
}

/**
 * Bridge untuk passing non-serializable data ke service.
 * 
 * Catatan: Ini masih menggunakan static reference, tapi:
 * - Data langsung di-consume setelah diakses
 * - Hanya digunakan saat initialize dan update metadata
 * - Alternative: Binder-based communication (lebih kompleks)
 * 
 * TODO: Pertimbangkan migrasi ke Binder jika diperlukan.
 */
object MediaPlaybackServiceBridge {
    private var pendingMediaSession: BrowserMediaSession? = null
    private var pendingArtwork: android.graphics.Bitmap? = null
    
    /**
     * Set MediaSession untuk diambil oleh service.
     * Dipanggil oleh Manager sebelum start service.
     */
    fun setMediaSession(session: BrowserMediaSession?) {
        pendingMediaSession = session
    }
    
    /**
     * Get dan consume MediaSession.
     * Session di-null-kan setelah diambil.
     */
    fun getMediaSession(): BrowserMediaSession? {
        val session = pendingMediaSession
        pendingMediaSession = null
        return session
    }
    
    /**
     * Set artwork untuk diambil oleh service.
     * Bitmap tidak bisa dikirim via Intent karena size limit.
     */
    fun setArtwork(artwork: android.graphics.Bitmap?) {
        pendingArtwork = artwork
    }
    
    /**
     * Get dan consume artwork.
     * Artwork di-null-kan setelah diambil.
     */
    fun consumeArtwork(): android.graphics.Bitmap? {
        val artwork = pendingArtwork
        pendingArtwork = null
        return artwork
    }
    
    /**
     * Clear semua pending data.
     */
    fun clear() {
        pendingMediaSession = null
        pendingArtwork = null
    }
}
