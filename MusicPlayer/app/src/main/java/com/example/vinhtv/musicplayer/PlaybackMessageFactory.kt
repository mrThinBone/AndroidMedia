package com.example.vinhtv.musicplayer

import android.os.Message
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_BUFFERING
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_PAUSE
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_PLAY
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_PROGRESS_UPDATE

class PlaybackMessageFactory {
    companion object {

        fun onPlayMsg(duration: Int): Message {
            return Message.obtain(null, ON_PLAY, duration, 0)
        }

        fun onPauseMsg(): Message {
            return Message.obtain(null, ON_PAUSE)
        }

        fun onProgressMsg(duration: Int, peek: Int): Message {
            return Message.obtain(null, ON_PROGRESS_UPDATE, duration, peek)
        }

        fun onBuffering(): Message {
            return Message.obtain(null, ON_BUFFERING)
        }
    }
}