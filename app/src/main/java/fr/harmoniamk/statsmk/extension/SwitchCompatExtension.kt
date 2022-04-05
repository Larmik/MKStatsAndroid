package fr.harmoniamk.statsmk.extension

import androidx.appcompat.widget.SwitchCompat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
fun SwitchCompat.checks() = callbackFlow {
    this@checks.setOnCheckedChangeListener { _, isChecked ->
        this.offer(isChecked)
    }
    awaitClose { this@checks.setOnCheckedChangeListener(null) }
}