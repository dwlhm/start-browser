package com.dwlhm.startbrowser.services.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.dwlhm.startbrowser.MainActivity
import com.dwlhm.startbrowser.R

/**
 * Kelas khusus untuk membangun notification media playback.
 * 
 * Prinsip:
 * - Single Responsibility: Hanya membangun notification
 * - Tidak menyimpan state
 * - Stateless - semua data diterima via parameter
 * 
 * @param context Application context
 */
class MediaNotificationBuilder(
    private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "media_playback_channel"
        const val NOTIFICATION_ID = 1001
        
        // Action constants
        const val ACTION_PLAY = "com.dwlhm.startbrowser.ACTION_PLAY"
        const val ACTION_PAUSE = "com.dwlhm.startbrowser.ACTION_PAUSE"
        const val ACTION_STOP = "com.dwlhm.startbrowser.ACTION_STOP"
        const val ACTION_PREVIOUS = "com.dwlhm.startbrowser.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.dwlhm.startbrowser.ACTION_NEXT"
    }
    
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    /**
     * Inisialisasi notification channel.
     * Harus dipanggil sekali saat service dimulai.
     */
    fun createNotificationChannel() {
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
    
    /**
     * Membangun notification berdasarkan state media.
     * 
     * @param state State media saat ini
     * @param mediaSessionToken Token untuk integrasi MediaSession
     * @param serviceClass Class service untuk PendingIntent actions
     * @return Notification yang siap ditampilkan
     */
    fun buildNotification(
        state: MediaPlaybackState,
        mediaSessionToken: MediaSessionCompat.Token?,
        serviceClass: Class<*>
    ): Notification {
        val contentIntent = createContentIntent(state)
        val actions = createActions(state.isPlaying, serviceClass)
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(state.title)
            .setContentText(state.artist)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(state.isPlaying)
            .addAction(actions.previous)
            .addAction(actions.playPause)
            .addAction(actions.next)
            .addAction(actions.stop)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .apply {
                state.artwork?.let { bitmap ->
                    setLargeIcon(bitmap)
                }
            }
            .build()
    }
    
    /**
     * Update notification yang sudah ada.
     */
    fun updateNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Membuat PendingIntent untuk membuka app saat notification diklik.
     */
    private fun createContentIntent(state: MediaPlaybackState): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("destination", "browser")
            putExtra("tab_id", state.tabId)
            putExtra("media_state", state.playbackState.name)
        }
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Container untuk action buttons.
     */
    data class NotificationActions(
        val previous: NotificationCompat.Action,
        val playPause: NotificationCompat.Action,
        val next: NotificationCompat.Action,
        val stop: NotificationCompat.Action
    )
    
    /**
     * Membuat semua action buttons untuk notification.
     */
    private fun createActions(
        isPlaying: Boolean,
        serviceClass: Class<*>
    ): NotificationActions {
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                "Pause",
                createServicePendingIntent(ACTION_PAUSE, serviceClass)
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                "Play",
                createServicePendingIntent(ACTION_PLAY, serviceClass)
            ).build()
        }
        
        val previousAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_previous,
            "Previous",
            createServicePendingIntent(ACTION_PREVIOUS, serviceClass)
        ).build()
        
        val nextAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_next,
            "Next",
            createServicePendingIntent(ACTION_NEXT, serviceClass)
        ).build()
        
        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            createServicePendingIntent(ACTION_STOP, serviceClass)
        ).build()
        
        return NotificationActions(
            previous = previousAction,
            playPause = playPauseAction,
            next = nextAction,
            stop = stopAction
        )
    }
    
    /**
     * Membuat PendingIntent untuk action ke service.
     */
    private fun createServicePendingIntent(
        action: String,
        serviceClass: Class<*>
    ): PendingIntent {
        val intent = Intent(context, serviceClass).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
