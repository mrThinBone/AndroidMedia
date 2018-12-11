package com.example.vinhtv.musicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC
import android.support.v4.content.ContextCompat


class MediaNotificationWrapper(val context: Context) {

    companion object {
        const val CHANNEL_ID = "music_notification_control"
    }

    val notificationManager: NotificationManager
    private val playAction: NotificationCompat.Action
    private val pauseAction: NotificationCompat.Action
    private var isPlaying = false

    init {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        playAction = NotificationCompat.Action(R.drawable.vd_playpausestop_play, "play", buildPendingIntent(PlayerService.PLAY_COMMAND))
        pauseAction = NotificationCompat.Action(R.drawable.vd_playpausestop_pause, "pause", buildPendingIntent(PlayerService.PAUSE_COMMAND))
    }

    private fun buildPendingIntent(command: Int): PendingIntent {
        val intent = Intent(context, PlayerService::class.java).apply {
            putExtra(PlayerService.COMMAND_KEY, command)
        }
        return PendingIntent.getService(context, command, intent, 0)
    }

    private fun activityIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    fun notification(isPlaying: Boolean): Notification {
        this.isPlaying = isPlaying
        setupNotificationChannel()
        val stopServicePendingIntent = buildPendingIntent(PlayerService.STOP_COMMAND)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setContentTitle("Forest Whitaker")
            setContentText("Bad Books")
            setSubText("Bad Books - \"Forest Whitaker\" Acoustic")
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.bad_book))

            setContentIntent(activityIntent())

            setDeleteIntent(stopServicePendingIntent)

            setVisibility(VISIBILITY_PUBLIC)

            setSmallIcon(R.drawable.ic_music_note)
            color = ContextCompat.getColor(context, R.color.colorPrimaryDark)

            addAction(if(isPlaying) pauseAction else playAction)

            setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0)
            )

            setOnlyAlertOnce(true)
            setSound(null)
            setDefaults(0)

        }
        return builder.build()
    }

    fun bufferingNotification(): Notification {
        setupNotificationChannel()
        val stopServicePendingIntent = buildPendingIntent(PlayerService.STOP_COMMAND)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setContentTitle("Forest Whitaker")
            setContentText("Bad Books")
            setSubText("buffering...")
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.bad_book))

            setContentIntent(activityIntent())

            setDeleteIntent(stopServicePendingIntent)

            setVisibility(VISIBILITY_PUBLIC)

            setSmallIcon(R.drawable.ic_music_note)
            color = ContextCompat.getColor(context, R.color.colorPrimaryDark)

            addAction(NotificationCompat.Action(R.drawable.vd_playpausestop_play, "play", null))

            setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0)
            )

            setOnlyAlertOnce(true)
            setSound(null)
            setDefaults(0)

        }
        return builder.build()
    }

    fun autoNotification(playerReady: Boolean): Notification {
        return if(playerReady) notification(isPlaying)
        else bufferingNotification()
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "music control"
            val description = "notification for music control"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel)
        }
    }
}