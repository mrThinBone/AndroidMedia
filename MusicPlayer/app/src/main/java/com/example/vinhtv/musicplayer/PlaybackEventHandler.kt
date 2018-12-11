package com.example.vinhtv.musicplayer

import android.os.Handler
import android.os.Message
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_PAUSE
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_PLAY
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_PROGRESS_UPDATE

class PlaybackEventHandler(private val listener: MediaPlayBackEvent?): Handler() {

    override fun handleMessage(msg: Message) {
        when(msg.what) {
            ON_PLAY -> listener?.mediaPlaying()
            ON_PAUSE -> listener?.mediaPause()
            ON_PROGRESS_UPDATE -> {
                listener?.mediaBuffer(msg.arg1, msg.arg2)
            }
        }
    }

    interface MediaPlayBackEvent {
        fun mediaPlaying()
        fun mediaPause()
        fun mediaBuffer(duration: Int, peek: Int)
    }
}