package com.andrognito.patternlockview.listener

import com.andrognito.patternlockview.PatternLockView

/**
 * Created by aritraroy on 19/03/17.
 */
/**
 * The callback interface for detecting patterns entered by the user
 */
interface PatternLockViewListener {
    /**
     * Fired when the pattern drawing has just started
     */
    fun onStarted()

    /**
     * Fired when the pattern is still being drawn and progressed to
     * one more [com.andrognito.patternlockview.PatternLockView.Dot]
     */
    fun onProgress(progressPattern: List<PatternLockView.Dot?>?)

    /**
     * Fired when the user has completed drawing the pattern and has moved their finger away
     * from the view
     */
    fun onComplete(pattern: List<PatternLockView.Dot>?)

    /**
     * Fired when the patten has been cleared from the view
     */
    fun onCleared()
}