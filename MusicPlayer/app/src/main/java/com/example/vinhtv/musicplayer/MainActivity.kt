package com.example.vinhtv.musicplayer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        music_control.setOnClickListener {
            val command = when(music_control.switchNext()) {
                PlayPauseButton.STATE_PLAY -> PlayerService.PLAY_COMMAND
                PlayPauseButton.STATE_PAUSE -> PlayerService.PAUSE_COMMAND
                else -> PlayerService.STOP_COMMAND
            }
            sendCommand(command)
        }
    }

    private fun sendCommand(command: Int) {
        startService(Intent(this, PlayerService::class.java).apply {
            putExtra(PlayerService.COMMAND_KEY, command)
        })
    }
}
