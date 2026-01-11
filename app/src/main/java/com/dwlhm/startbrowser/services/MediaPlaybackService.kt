package com.dwlhm.startbrowser.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.dwlhm.browser.BrowserMediaMetadata
import com.dwlhm.browser.BrowserMediaSession
import com.dwlhm.browser.BrowserMediaState
import com.dwlhm.startbrowser.MainActivity
import com.dwlhm.startbrowser.R

/**
 * Foreground service untuk background media playback.
 * Dikontrol oleh MediaPlaybackManager - tidak listen events sendiri.
 */
class MediaPlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "media_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.dwlhm.startbrowser.ACTION_PLAY"
        const val ACTION_PAUSE = "com.dwlhm.startbrowser.ACTION_PAUSE"
        const val ACTION_STOP = "com.dwlhm.startbrowser.ACTION_STOP"
        const val ACTION_PREVIOUS = "com.dwlhm.startbrowser.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.dwlhm.startbrowser.ACTION_NEXT"
        
        const val ACTION_UPDATE_STATE = "com.dwlhm.startbrowser.ACTION_UPDATE_STATE"
        const val ACTION_UPDATE_METADATA = "com.dwlhm.startbrowser.ACTION_UPDATE_METADATA"
        
        const val EXTRA_STATE = "extra_state"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_ALBUM = "extra_album"

        // Static reference untuk callback dari notification actions
        private var mediaSessionCallback: BrowserMediaSession? = null
        
        fun setMediaSession(session: BrowserMediaSession?) {
            mediaSessionCallback = session
        }

        fun startService(context: Context) {
            val intent = Intent(context, MediaPlaybackService::class.java)
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MediaPlaybackService::class.java)
            context.stopService(intent)
        }
        
        fun updateState(context: Context, state: BrowserMediaState) {
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = ACTION_UPDATE_STATE
                putExtra(EXTRA_STATE, state.name)
            }
            context.startService(intent)
        }
        
        fun updateMetadata(context: Context, metadata: BrowserMediaMetadata) {
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = ACTION_UPDATE_METADATA
                putExtra(EXTRA_TITLE, metadata.title)
                putExtra(EXTRA_ARTIST, metadata.artist)
                putExtra(EXTRA_ALBUM, metadata.album)
            }
            context.startService(intent)
            
            currentArtwork = metadata.artwork
        }
        
        private var currentArtwork: Bitmap? = null
    }

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var notificationManager: NotificationManager

    private var currentState: BrowserMediaState = BrowserMediaState.PAUSE
    private var currentTitle: String? = null
    private var currentArtist: String? = null
    private var currentAlbum: String? = null

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        initMediaSession()

        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        mediaSession?.run {
            isActive = false
            release()
        }
        mediaSession = null
        mediaSessionCallback = null
        currentArtwork = null
        
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> mediaSessionCallback?.play()
            ACTION_PAUSE -> mediaSessionCallback?.pause()
            ACTION_STOP -> {
                mediaSessionCallback?.stop()
                stopSelf()
            }
            ACTION_PREVIOUS -> mediaSessionCallback?.previousTrack()
            ACTION_NEXT -> mediaSessionCallback?.nextTrack()
            ACTION_UPDATE_STATE -> {
                val stateName = intent.getStringExtra(EXTRA_STATE)
                currentState = BrowserMediaState.valueOf(stateName ?: "PAUSE")
                updatePlaybackState()
                updateNotification()
            }
            ACTION_UPDATE_METADATA -> {
                currentTitle = intent.getStringExtra(EXTRA_TITLE)
                currentArtist = intent.getStringExtra(EXTRA_ARTIST)
                currentAlbum = intent.getStringExtra(EXTRA_ALBUM)
                updateMediaSessionMetadata()
                updateNotification()
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Media Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Kontrol pemutaran media browser"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "StartBrowserMediaSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    mediaSessionCallback?.play()
                }

                override fun onPause() {
                    mediaSessionCallback?.pause()
                }

                override fun onStop() {
                    mediaSessionCallback?.stop()
                    stopSelf()
                }

                override fun onSkipToNext() {
                    mediaSessionCallback?.nextTrack()
                }

                override fun onSkipToPrevious() {
                    mediaSessionCallback?.previousTrack()
                }

                override fun onCustomAction(action: String?, extras: Bundle?) {
                    when (action) {
                        ACTION_STOP -> {
                            mediaSessionCallback?.stop()
                            stopSelf()
                        }
                    }
                }
            })
            isActive = true
        }
    }

    private fun updateMediaSessionMetadata() {
        val session = mediaSession ?: return

        val builder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTitle ?: "Unknown")
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentArtist ?: "")
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentAlbum ?: "")

        currentArtwork?.let { bitmap ->
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
        }

        session.setMetadata(builder.build())
    }

    private fun updatePlaybackState() {
        val session = mediaSession ?: return
        
        val state = when (currentState) {
            BrowserMediaState.PLAY -> PlaybackStateCompat.STATE_PLAYING
            BrowserMediaState.PAUSE -> PlaybackStateCompat.STATE_PAUSED
            BrowserMediaState.STOP -> PlaybackStateCompat.STATE_STOPPED
        }

        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
            .build()

        session.setPlaybackState(playbackState)
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isPlaying = currentState == BrowserMediaState.PLAY

        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                "Pause",
                createPendingIntent(ACTION_PAUSE)
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                "Play",
                createPendingIntent(ACTION_PLAY)
            ).build()
        }

        val previousAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_previous,
            "Previous",
            createPendingIntent(ACTION_PREVIOUS)
        ).build()

        val nextAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_next,
            "Next",
            createPendingIntent(ACTION_NEXT)
        ).build()

        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            createPendingIntent(ACTION_STOP)
        ).build()

        val title = currentTitle ?: "Media sedang diputar"
        val artist = currentArtist ?: "Start Browser"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(previousAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(stopAction)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )

        currentArtwork?.let { bitmap ->
            builder.setLargeIcon(bitmap)
        }

        return builder.build()
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MediaPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateNotification() {
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }
}
