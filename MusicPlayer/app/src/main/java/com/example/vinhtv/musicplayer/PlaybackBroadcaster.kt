package com.example.vinhtv.musicplayer

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager

class PlaybackBroadcaster(context: Context) {

    companion object {
        const val ACTION = "vmedia.playback"
        const val CODE_ON_PLAY = 8701
        const val CODE_ON_PAUSE = 8702
        const val CODE_ON_PROGRESS_UPDATE = 8702
        const val KEY_CODE = "op_code"
        const val KEY_DATA = "op_dat"
    }

    private val localBrcastMgr = LocalBroadcastManager.getInstance(context)

    fun onPlay() {
        val intent = getIntent(CODE_ON_PLAY)
        localBrcastMgr.sendBroadcast(intent)
    }

    fun onPause() {
        val intent = getIntent(CODE_ON_PAUSE)
        localBrcastMgr.sendBroadcast(intent)
    }

    fun onProgressUpdate(arg: Int) {
        val intent = getIntent(CODE_ON_PROGRESS_UPDATE)
        intent.putExtra(KEY_DATA, arg)
        localBrcastMgr.sendBroadcast(intent)
    }

    private fun getIntent(code: Int): Intent {
        return Intent(ACTION).apply {
            putExtra(KEY_CODE, code)
        }
    }

}