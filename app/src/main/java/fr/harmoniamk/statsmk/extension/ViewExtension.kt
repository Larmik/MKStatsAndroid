package fr.harmoniamk.statsmk.extension

import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.appcompat.widget.SwitchCompat
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
        if (isActive) offer(Unit)
    }
    awaitClose { }
}

@ExperimentalCoroutinesApi
fun RadioGroup.checks(): Flow<Int> = callbackFlow {
   this@checks.setOnCheckedChangeListener { group, checkedId ->
       if (isActive) offer(checkedId)
   }
    awaitClose {  this@checks.setOnCheckedChangeListener(null) }
}

@ExperimentalCoroutinesApi
fun CheckBox.checks() = callbackFlow {
    this@checks.setOnCheckedChangeListener { _, isChecked ->
        if (isActive) offer(isChecked)
    }
    awaitClose { this@checks.setOnCheckedChangeListener(null) }
}

@ExperimentalCoroutinesApi
fun EditText.onTextChanged() = callbackFlow {
    this@onTextChanged.addTextChangedListener {
        if (isActive) offer(it.toString())
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