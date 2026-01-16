package com.dwlhm.startbrowser.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.dwlhm.browser.BrowserMediaState
import com.dwlhm.browser.MediaSessionController
import com.dwlhm.browser.MediaSessionControllerImpl
import com.dwlhm.media.MediaIntent
import com.dwlhm.media.api.MediaNotificationBuilder
import com.dwlhm.media.api.MediaPlaybackServiceBridge
import com.dwlhm.media.api.MediaPlaybackState
import com.dwlhm.startbrowser.MainActivity
import com.dwlhm.startbrowser.R
import com.dwlhm.startbrowser.store.MediaStores

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
    
    private var currentState: MediaPlaybackState? = null
    
    private lateinit var notificationBuilder: MediaNotificationBuilder
    private lateinit var sessionController: MediaSessionController

    override fun onCreate() {
        super.onCreate()
        
        notificationBuilder = MediaNotificationBuilder(this, MainActivity::class.java, R.drawable.ic_launcher_foreground)
        sessionController = MediaSessionControllerImpl(this)
        
        notificationBuilder.createNotificationChannel()
        
        sessionController.initialize(createMediaSessionCallback())

        startForegroundWithNotification()
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
        // START_STICKY: Service akan di-restart oleh sistem jika di-kill
        // Ini memastikan media tetap berjalan meski sistem menghentikan service
        return START_STICKY
    }

    /**
     * Handle semua intent yang masuk.
     * Setiap intent membawa informasi untuk update state atau trigger action.
     */
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_PLAY -> handlePlayAction()
            ACTION_PAUSE -> handlePauseAction()
            ACTION_STOP -> handleStopAction()
            ACTION_PREVIOUS -> handlePreviousAction()
            ACTION_NEXT -> handleNextAction()
            
            MediaIntent.Action.INITIALIZE -> handleInitialize(intent)
            MediaIntent.Action.UPDATE_STATE -> handleUpdateState(intent)
            MediaIntent.Action.UPDATE_METADATA -> handleUpdateMetadata(intent)
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
        Log.d("[Media:Service] --init", "initializing...")
        val tabId = intent.getStringExtra(MediaIntent.Extra.TAB_ID) ?: return
        Log.d("[Media:Service] --init", "tabId: $tabId")
        val mediaSession = MediaStores.sessionStore.get(MediaIntent.Extra.SESSION) ?: return
        Log.d("[Media:Service] --init", "mediaSession: $mediaSession")
        
        // Ambil initial state dari intent, default ke PAUSE jika tidak ada
        val initialStateName = intent.getStringExtra(MediaIntent.Extra.STATE)
        val initialState = if (initialStateName != null) {
            try {
                BrowserMediaState.valueOf(initialStateName)
            } catch (_: IllegalArgumentException) {
                BrowserMediaState.PAUSE
            }
        } else {
            BrowserMediaState.PAUSE
        }

        currentState = MediaPlaybackState(
            tabId = tabId,
            mediaSession = mediaSession,
            playbackState = initialState
        )
        
        sessionController.updatePlaybackState(initialState)
    }
    
    /**
     * Update playback state (play/pause/stop).
     */
    private fun handleUpdateState(intent: Intent) {
        val state = currentState ?: return
        val stateName = intent.getStringExtra(MediaIntent.Extra.STATE) ?: return
        val newPlaybackState = BrowserMediaState.valueOf(stateName)
        
        currentState = state.withPlaybackState(newPlaybackState)
        
        sessionController.updatePlaybackState(newPlaybackState)
        updateNotification()
    }
    
    /**
     * Update metadata (title, artist, album, artwork).
     */
    private fun handleUpdateMetadata(intent: Intent) {
        val state = currentState ?: return
        
        val title = intent.getStringExtra(MediaIntent.Extra.TITLE)
        val artist = intent.getStringExtra(MediaIntent.Extra.ARTIST)
        val album = intent.getStringExtra(MediaIntent.Extra.ALBUM)
        val artwork = MediaStores.artworkStore.get(MediaIntent.Extra.ARTWORK)
        
        currentState = state.withMetadata(title, artist, album, artwork)
        
        currentState?.let { newState ->
            sessionController.updateMetadata(
                title = newState.title,
                artist = newState.artist,
                album = newState.album,
                artwork = newState.artwork
            )
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
        const val ACTION_PLAY = MediaNotificationBuilder.ACTION_PLAY
        const val ACTION_PAUSE = MediaNotificationBuilder.ACTION_PAUSE
        const val ACTION_STOP = MediaNotificationBuilder.ACTION_STOP
        const val ACTION_PREVIOUS = MediaNotificationBuilder.ACTION_PREVIOUS
        const val ACTION_NEXT = MediaNotificationBuilder.ACTION_NEXT
    }
}

