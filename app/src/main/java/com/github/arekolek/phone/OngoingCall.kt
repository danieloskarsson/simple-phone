package com.github.arekolek.phone

import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log
import io.reactivex.subjects.BehaviorSubject

object OngoingCall {
    val state: BehaviorSubject<Int> = BehaviorSubject.create()

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            Log.d(javaClass.simpleName, call.toString())
            state.onNext(newState)
        }
    }

    var call: Call? = null
        set(value) {
            field?.unregisterCallback(callback)
            value?.let {
                it.registerCallback(callback)
                state.onNext(it.state)
            }
            field = value
        }

    fun answer() {
        call!!.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        call!!.disconnect()
    }
}
