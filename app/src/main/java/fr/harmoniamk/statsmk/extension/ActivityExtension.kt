package fr.harmoniamk.statsmk.extension

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive

@ExperimentalCoroutinesApi
fun ComponentActivity.backPressedDispatcher(owner: LifecycleOwner) = callbackFlow<Unit> {
    val callBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isActive) trySend(Unit)
        }
    }
    this@backPressedDispatcher.onBackPressedDispatcher.addCallback(owner, callBack)
    awaitClose { }
}