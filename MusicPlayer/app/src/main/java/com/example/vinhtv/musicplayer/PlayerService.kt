package com.example.vinhtv.musicplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.util.Log

//https://medium.com/@anitaa_1990/how-to-update-an-activity-from-background-service-or-a-broadcastreceiver-6dabdb5cef74
//https://developer.android.com/training/notify-user/expanded#media-style
//https://developer.android.com/guide/topics/media-apps/audio-app/building-an-audio-app
class PlayerService: Service() {

    companion object {
        const val COMMAND_KEY = "command"
        const val PLAY_COMMAND = 32100
        const val PAUSE_COMMAND = 32211
        const val STOP_COMMAND = 33233
    }

    private lateinit var mediaNotification: MediaNotificationWrapper
    private var mServiceLooper: Looper? = null
    private var mServiceHandler: ServiceHandler? = null
    private var startId: Int = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("player_service", "onStartCommand")
        mServiceHandler?.obtainMessage()?.also {
            it.arg1 = startId
            it.what = intent?.getIntExtra(COMMAND_KEY, -1)?: -1
            Log.d("player_service", "what: ${it.what}")
            if(it.what != -1) mServiceHandler?.sendMessage(it)
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("player_service", "create")
        HandlerThread("PlayerServiceI", Process.THREAD_PRIORITY_FOREGROUND).apply {
            start()
            mServiceLooper = looper
            mServiceHandler = ServiceHandler(looper, MediaPlayer.create(applicationContext, R.raw.bad_book))
        }
        mediaNotification = MediaNotificationWrapper(applicationContext)
        startId = (System.currentTimeMillis()/1000).toInt()
        // this will make notification unable to dismiss
        startForeground(startId, mediaNotification.notification(false))
    }

    override fun onDestroy() {
        mServiceLooper?.quit()
        Log.d("player_service", "kill/destroy")
        super.onDestroy()
    }

    private inner class ServiceHandler(looper: Looper, val player: MediaPlayer): Handler(looper) {

        override fun handleMessage(msg: Message) {
            when(msg.what) {
                PLAY_COMMAND -> play()
                PAUSE_COMMAND -> pause()
                STOP_COMMAND -> {
                    if(player.isPlaying) player.stop()
                    player.reset()
                    player.release()
                    // stop this service
                    stopSelf(msg.arg1)
                }
            }
        }

        fun play() {
            if(!player.isPlaying) {
                player.start()
                mediaNotification.notificationManager.notify(startId, mediaNotification.notification(true))
            }
            Log.d("player_service", "play")
        }

        fun pause() {
            player.pause()
            mediaNotification.notificationManager.notify(startId, mediaNotification.notification(false))
            Log.d("player_service", "pause")
        }

    }
}