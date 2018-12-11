package com.example.vinhtv.musicplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

//https://medium.com/@anitaa_1990/how-to-update-an-activity-from-background-service-or-a-broadcastreceiver-6dabdb5cef74
//https://developer.android.com/training/notify-user/expanded#media-style
//https://developer.android.com/guide/topics/media-apps/audio-app/building-an-audio-app
class PlayerService: Service() {

    companion object {
        const val COMMAND_KEY = "command"
        const val COMMAND_VALUE = "value"
        const val PLAY_COMMAND = 32100
        const val PAUSE_COMMAND = 32211
        const val STOP_COMMAND = 33233
        const val SEEK_TO_COMMAND = 33134
    }


//    private lateinit var playbackBroadcaster: PlaybackBroadcaster
    private lateinit var mServiceLooper: Looper
    private lateinit var mServiceHandler: ServiceHandler
    private lateinit var mPlayerWrapper: PlayerWrapper
    private var timer: Timer? = null
    private val mClients = ArrayList<Messenger>()
    private lateinit var mediaNotification: MediaNotificationWrapper
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
            startForeground(notificationId, mediaNotification.autoNotification(mPlayerWrapper.isReady()))
        }

        val command = intent.getIntExtra(COMMAND_KEY, -1)
        if(command != -1) {
            mServiceHandler.obtainMessage()?.also {
                it.arg1 = startId
                it.what = command
                it.arg2 = intent.getIntExtra(COMMAND_VALUE, 0)
                Log.d("player_service", "what: ${it.what}")
                mServiceHandler.sendMessage(it)
            }
        }
        return START_NOT_STICKY
    }

    private fun processClient(intent: Intent): Boolean {
        var client = intent.getParcelableExtra<Messenger?>("bind")
        if(client != null) {
            mClients.add(client)
            if(mClients.size == 1) {
                pendingStart++
            }
            // notification must already there
            if(pendingStart>1 && mPlayerWrapper.isReady()) {
                startForeground(notificationId, mediaNotification.autoNotification(mPlayerWrapper.isReady()))
            }
            return true
        }
        client = intent.getParcelableExtra<Messenger?>("unbind")
        if(client != null) {
            mClients.remove(client)
            if(mClients.size == 0) {
                stopForeground(false)
                if(!mPlayerWrapper.isReady()) {
                    stopSelf()
                }
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
            val player = MediaPlayer()
            mPlayerWrapper = PlayerWrapper(player)
            mPlayerWrapper.loadFromAsset(applicationContext, "bad_book.mp3")
            mServiceLooper = looper
            mServiceHandler = ServiceHandler(looper)
            settingMediaPlaybackListener(player)
        }
        mediaNotification = MediaNotificationWrapper(applicationContext)
//        playbackBroadcaster = PlaybackBroadcaster(applicationContext)
        notificationId = (System.currentTimeMillis()/1000).toInt()
    }

    override fun onDestroy() {
        timer?.cancel()
        timer = null
        mPlayerWrapper.release()
        mClients.clear()
        mServiceLooper.quit()
        Log.d("player_service", "kill/destroy")
        super.onDestroy()
    }

    private fun settingMediaPlaybackListener(player: MediaPlayer) {
        player.setOnCompletionListener {
            timer?.cancel()
            timer = null
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
        if(mClients.size > 0) notifyClients(PlaybackMessageFactory.onPlayMsg(0))
    }

    private fun notifyOnPause() {
        mediaNotification.notificationManager.notify(notificationId, mediaNotification.notification(false))
        if(mClients.size > 0) notifyClients(PlaybackMessageFactory.onPauseMsg())
    }

    private fun notifyOnBuffering() {
        mediaNotification.notificationManager.notify(notificationId, mediaNotification.bufferingNotification())
        if(mClients.size > 0) notifyClients(PlaybackMessageFactory.onBuffering())
    }

    private fun notifyOnProgress() {
        timer = Timer()
        timer?.scheduleAtFixedRate(300L, 100L) {
            if(mClients.size>0) {
                val msg = PlaybackMessageFactory.onProgressMsg(
                    mPlayerWrapper.player.duration,
                    mPlayerWrapper.player.currentPosition
                )
                notifyClients(msg)
            }
        }
    }

    private inner class ServiceHandler(looper: Looper): Handler(looper) {

        override fun handleMessage(msg: Message) {
            when(msg.what) {
                PLAY_COMMAND -> play()
                PAUSE_COMMAND -> pause()
                SEEK_TO_COMMAND -> seekTo(msg.arg2)
                STOP_COMMAND -> {
                    // stop this service
                    stopSelf(msg.arg1)
                }
            }
        }

        fun play() {
            if(!mPlayerWrapper.isReady()) {
                notifyOnBuffering()
                mPlayerWrapper.prepare()
            }
            if(mPlayerWrapper.play()) {
                notifyOnPlay()
                notifyOnProgress()
                Log.d("player_service", "play")
            }
        }

        fun pause() {
            timer?.cancel()
            timer = null
            mPlayerWrapper.pause()
            notifyOnPause()
            Log.d("player_service", "pause")
        }

        fun seekTo(peek: Int) {
            mPlayerWrapper.seekTo(peek)
        }

    }
}