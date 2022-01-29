package fr.harmoniamk.statsmk.extension

import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive


@ExperimentalCoroutinesApi
fun View.clicks(): Flow<Unit> = callbackFlow {
    this@clicks.setOnClickListener {
        if (isActive) offer(Unit)
    }
    awaitClose { }
}

@ExperimentalCoroutinesApi
fun RadioGroup.checks(): Flow<Int> = callbackFlow {
   this@checks.setOnCheckedChangeListener { group, checkedId ->
       if (isActive) offer(checkedId)
   }
    awaitClose { }
}

@ExperimentalCoroutinesApi
fun EditText.onTextChanged() = callbackFlow {
    this@onTextChanged.addTextChangedListener {
        if (isActive) offer(it.toString())
    }
    awaitClose { }
}