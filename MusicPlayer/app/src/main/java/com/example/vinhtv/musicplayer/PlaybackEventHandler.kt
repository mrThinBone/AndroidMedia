package com.example.vinhtv.musicplayer

import android.os.Handler
import android.os.Message
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_PAUSE
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_PLAY

class PlaybackEventHandler(private val listener: MediaPlayBackEvent?): Handler() {

    override fun handleMessage(msg: Message) {
        when(msg.what) {
            ON_PLAY -> listener?.mediaPlaying()
            ON_PAUSE -> listener?.mediaPause()
        }
    }

    interface MediaPlayBackEvent {
        fun mediaPlaying()
        fun mediaPause()
    }
}