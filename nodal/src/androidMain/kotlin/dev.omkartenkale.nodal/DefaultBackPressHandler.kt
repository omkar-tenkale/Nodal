package dev.omkartenkale.nodal

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import dev.omkartenkale.nodal.misc.BackPressCallback
import dev.omkartenkale.nodal.misc.BackPressHandler

public class DefaultBackPressHandler(private val onBackPressedDispatcher: OnBackPressedDispatcher) : BackPressHandler {
    override fun dispatchBackPress() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun addBackPressCallback(backPressCallback: BackPressCallback) {
        onBackPressedDispatcher.addCallback(object :
            OnBackPressedCallback(backPressCallback.isEnabled) {
            override fun handleOnBackPressed() {
                backPressCallback()
            }
        })
    }
}