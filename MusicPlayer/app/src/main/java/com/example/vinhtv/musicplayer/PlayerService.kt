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
//    private lateinit var playbackBroadcaster: PlaybackBroadcaster
    private var mServiceLooper: Looper? = null
    private var mServiceHandler: ServiceHandler? = null
    private val mClients = ArrayList<Messenger>()
    private var pendingStart: Int = 0
    private var notificationId: Int = 0


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("player_service", "onStartCommand")
        // this will make notification unable to dismiss
        if(processClient(intent)) return START_NOT_STICKY
        if(pendingStart == 1) {
            startForeground(notificationId, mediaNotification.autoNotification())
        }
        mServiceHandler?.obtainMessage()?.also {
            it.arg1 = startId
            it.what = intent.getIntExtra(COMMAND_KEY, -1)
            Log.d("player_service", "what: ${it.what}")
            if(it.what != -1) mServiceHandler?.sendMessage(it)
        }
        return START_NOT_STICKY
    }

    private fun processClient(intent: Intent): Boolean {
        var client = intent.getParcelableExtra<Messenger?>("bind")
        if(client != null) {
            mClients.add(client)
            if(mClients.size == 1) {
                Log.e("player_service", "pending start")
                pendingStart++
            }
            // notification must already there
            if(pendingStart>1) {
                startForeground(notificationId, mediaNotification.autoNotification())
            }
            return true
        }
        client = intent.getParcelableExtra<Messenger?>("unbind")
        if(client != null) {
            mClients.remove(client)
            if(mClients.size == 0) {
                stopForeground(false)
            }
            return true
        }
        return false
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("player_service", "create")
        HandlerThread("PlayerServiceI", Process.THREAD_PRIORITY_FOREGROUND).apply {
            start()
            val player = MediaPlayer.create(applicationContext, R.raw.bad_book)
            mServiceLooper = looper
            mServiceHandler = ServiceHandler(looper, player)
            settingMediaPlaybackListener(player)
        }
        mediaNotification = MediaNotificationWrapper(applicationContext)
//        playbackBroadcaster = PlaybackBroadcaster(applicationContext)
        notificationId = (System.currentTimeMillis()/1000).toInt()
    }

    override fun onDestroy() {
        mClients.clear()
        mServiceLooper?.quit()
        Log.d("player_service", "kill/destroy")
        super.onDestroy()
    }

    private fun settingMediaPlaybackListener(player: MediaPlayer) {
        player.setOnCompletionListener {
            notifyOnPause()
        }
    }

    private fun notifyClients(msg: Message) {
        for (i in 0 until mClients.size) {
            try {
                val client = mClients[0]
                client.send(msg)
            } catch (e: RemoteException) {
                mClients.removeAt(i)
            }
        }
        if(mClients.size == 0) stopForeground(false)
    }

    private fun notifyOnPlay() {
        mediaNotification.notificationManager.notify(notificationId, mediaNotification.notification(true))
        if(mClients.size > 0) notifyClients(PlaybackMessageFactory.onPlayMsg())
    }

    private fun notifyOnPause() {
        mediaNotification.notificationManager.notify(notificationId, mediaNotification.notification(false))
        if(mClients.size > 0) notifyClients(PlaybackMessageFactory.onPauseMsg())
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
                notifyOnPlay()
                Log.d("player_service", "play")
            }
        }

        fun pause() {
            if(player.isPlaying) {
                player.pause()
                notifyOnPause()
                Log.d("player_service", "pause")
            }
        }

    }
}