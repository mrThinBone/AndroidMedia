package com.example.vinhtv.musicplayer

import android.content.Context
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet

class PlayPauseButton(context: Context, attrs: AttributeSet): AppCompatImageButton(context, attrs) {

    companion object {
        const val STATE_PLAY = 0
        const val STATE_PAUSE = 1
    }

    private val allStates = arrayOf(
        intArrayOf(R.attr.state_play, -R.attr.state_pause),
        intArrayOf(-R.attr.state_play, R.attr.state_pause)
    )
    private var state: Int = STATE_PLAY

    init {
        setImageResource(R.drawable.asl_playpausestop)
        scaleType = ScaleType.CENTER_CROP
        background = null
    }

    fun play() {
        if(state == STATE_PLAY) return
        state = STATE_PLAY
        setImageState(allStates[0], true)
    }

    fun pause() {
        if(state == STATE_PAUSE) return
        state = STATE_PAUSE
        setImageState(allStates[1], true)
    }

    fun switchNext(): Int {
        val currentState= state
        this.state++
        if(state == 2) state = STATE_PLAY
        setImageState(allStates[state], true)
        return currentState
    }

}