package com.example.vinhtv.musicplayer

import android.content.Intent
import android.os.Bundle
import android.os.Messenger
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PlaybackEventHandler.MediaPlayBackEvent {

    private var playing = false
    private lateinit var messenger: Messenger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        music_control.setOnClickListener {
            val command = if(playing) PlayerService.PAUSE_COMMAND else PlayerService.PLAY_COMMAND
            sendCommand(command)
        }
        val handler = PlaybackEventHandler(this)
        messenger = Messenger(handler)
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, PlayerService::class.java).apply {
            putExtra("bind", messenger)
        })
    }

    override fun onStop() {
        startService(Intent(this, PlayerService::class.java).apply {
            putExtra("unbind", messenger)
        })
        super.onStop()
    }

    private fun sendCommand(command: Int) {
        startService(Intent(this, PlayerService::class.java).apply {
            putExtra(PlayerService.COMMAND_KEY, command)
        })
    }

    override fun mediaPlaying() {
        music_control.pause()
    }

    override fun mediaPause() {
        music_control.play()
    }
}
