package com.example.vinhtv.musicplayer

import android.os.Message
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_PAUSE
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_PLAY
import com.example.vinhtv.musicplayer.PlaybackCode.Companion.ON_PROGRESS_UPDATE

class PlaybackMessageFactory {
    companion object {

        fun onPlayMsg(): Message {
            return Message.obtain(null, ON_PLAY)
        }

        fun onPauseMsg(): Message {
            return Message.obtain(null, ON_PAUSE)
        }

        fun onProgressMsg(ms: Int): Message {
            return Message.obtain(null, ON_PROGRESS_UPDATE, ms, 0)
        }
    }
}