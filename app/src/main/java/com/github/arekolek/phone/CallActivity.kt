package com.github.arekolek.phone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telecom.Call
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.github.arekolek.phone.databinding.ActivityCallBinding
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class CallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCallBinding
    private val disposables = CompositeDisposable()

    private var number: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        number = intent?.data?.schemeSpecificPart
    }

    override fun onStart() {
        super.onStart()

        binding.answer.setOnClickListener {
            OngoingCall.answer()
        }

        binding.hangup.setOnClickListener {
            OngoingCall.hangup()
        }

        OngoingCall.state
            .subscribe(::updateUi)
            .addTo(disposables)

        OngoingCall.state
            .filter { it == Call.STATE_DISCONNECTED }
            .delay(1, TimeUnit.SECONDS)
            .firstElement()
            .subscribe { finish() }
            .addTo(disposables)
    }

    private fun updateUi(state: Int) {
        binding.callInfo.text = getString(when (state) {
            Call.STATE_NEW -> R.string.call_state_new
            Call.STATE_RINGING -> R.string.call_state_ringing
            Call.STATE_DIALING -> R.string.call_state_dialing
            Call.STATE_ACTIVE -> R.string.call_state_active
            Call.STATE_HOLDING -> R.string.call_state_holding
            Call.STATE_DISCONNECTED -> R.string.call_state_disconnected
            Call.STATE_CONNECTING -> R.string.call_state_connecting
            Call.STATE_DISCONNECTING -> R.string.call_state_disconnecting
            Call.STATE_SELECT_PHONE_ACCOUNT -> R.string.call_state_select_phone_account
            else -> R.string.call_state_unknown
        })
        binding.number.text = number
        binding.answer.isVisible = state == Call.STATE_RINGING
        binding.hangup.isVisible = state in listOf(
            Call.STATE_DIALING,
            Call.STATE_RINGING,
            Call.STATE_ACTIVE
        )
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    companion object {
        fun start(context: Context, call: Call) {
            Intent(context, CallActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call.details.handle)
                .let(context::startActivity)
        }
    }
}
