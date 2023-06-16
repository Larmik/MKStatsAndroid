package fr.harmoniamk.statsmk.extension

import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import java.net.URL
import java.util.concurrent.Executors


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
fun RadioGroup.checks(): Flow<Int> = callbackFlow {
   this@checks.setOnCheckedChangeListener { group, checkedId ->
       if (isActive) trySend(checkedId)
   }
    awaitClose {  this@checks.setOnCheckedChangeListener(null) }
}

@ExperimentalCoroutinesApi
fun RadioButton.checks(): Flow<Unit> = callbackFlow {
   this@checks.setOnCheckedChangeListener { _, checked ->
       if (isActive) trySend(Unit)

   }
    awaitClose {  this@checks.setOnCheckedChangeListener(null) }

}

@ExperimentalCoroutinesApi
fun CheckBox.checks() = callbackFlow {
    this@checks.setOnCheckedChangeListener { _, isChecked ->
        if (isActive) trySend(isChecked)
    }
    awaitClose { this@checks.setOnCheckedChangeListener(null) }
}

@ExperimentalCoroutinesApi
fun EditText.onTextChanged() = callbackFlow {
    this@onTextChanged.addTextChangedListener {
        if (isActive) trySend(it.toString())
    }
    awaitClose { }
}

fun ImageView.setImageURL(url: String?) {
    Executors.newSingleThreadExecutor().execute {
        try {
            val `in` = URL(url).openStream()
            val image = BitmapFactory.decodeStream(`in`)
            Handler(Looper.getMainLooper()).post { this.setImageBitmap(image) }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }
}