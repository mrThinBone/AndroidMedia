package com.example.vinhtv.musicplayer

import android.content.Context
import android.media.MediaPlayer
import android.os.Looper

class PlayerWrapper(val player: MediaPlayer) {

    private var prepared: Boolean = false

    fun play(): Boolean {
        if(player.isPlaying) return false
        player.start()
        return true
    }

    fun pause() {
        player.pause()
    }

    fun prepare() {
        if(prepared) throw IllegalStateException("Player has already prepared!!!")
        if(Looper.myLooper() == Looper.getMainLooper()) throw IllegalStateException("This operation must not be ran on Main Thread!")
        player.prepare()
        prepared = true
    }

    fun isReady() = prepared

    fun loadFromAsset(context: Context, fileName: String) {
        val descriptor = context.assets.openFd(fileName)
        player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
        descriptor.close()
        prepared = false
    }

    fun release() {
        if(player.isPlaying) player.stop()
        player.reset()
        player.release()
    }

    fun seekTo(peek: Int) {
        player.seekTo(peek)
    }
}