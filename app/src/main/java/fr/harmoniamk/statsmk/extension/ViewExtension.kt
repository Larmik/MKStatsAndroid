package fr.harmoniamk.statsmk.extension

import android.app.Dialog
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive


@ExperimentalCoroutinesApi
fun View.clicks(): Flow<Unit> = callbackFlow {
    this@clicks.setOnClickListener {
        if (isActive) trySend(Unit)
    }
    awaitClose { }
}

@ExperimentalCoroutinesApi
fun Dialog?.dismiss(): Flow<Unit> = callbackFlow {
    this@dismiss?.setOnDismissListener {
        if (isActive) trySend(Unit)
    }
    awaitClose { }
}


@ExperimentalCoroutinesApi
fun RadioButton.checks(): Flow<Unit> = callbackFlow {
   this@checks.setOnCheckedChangeListener { _, checked ->
       if (isActive) trySend(Unit)

   }
    awaitClose {  this@checks.setOnCheckedChangeListener(null) }

}

@ExperimentalCoroutinesApi
fun EditText.onTextChanged() = callbackFlow {
    this@onTextChanged.addTextChangedListener {
        if (isActive) trySend(it.toString())
    }
    awaitClose { }
}
