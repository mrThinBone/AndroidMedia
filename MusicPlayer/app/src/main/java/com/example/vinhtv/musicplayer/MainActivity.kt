package com.example.vinhtv.musicplayer

import android.content.Intent
import android.os.Bundle
import android.os.Messenger
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PlaybackEventHandler.MediaPlayBackEvent {

    private var playing = false
    private var seekBarModifying = false
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
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                seekBarModifying = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                sendCommand(PlayerService.SEEK_TO_COMMAND, seekBar.progress)
                seekBarModifying = false
            }

        })
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

    private fun sendCommand(command: Int, seekTo: Int = 0) {
        startService(Intent(this, PlayerService::class.java).apply {
            putExtra(PlayerService.COMMAND_KEY, command)
            putExtra(PlayerService.COMMAND_VALUE, seekTo)
        })
    }

    override fun mediaPlaying() {
        playing = true
        music_control.pause()
    }

    override fun mediaPause() {
        playing = false
        music_control.play()
    }

    override fun mediaBuffer(duration: Int, peek: Int) {
        if(seekBarModifying) return
        playing = true
        seekBar.max = duration
        seekBar.progress = peek
        music_control.pause()
    }
}
