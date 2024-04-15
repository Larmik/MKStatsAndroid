package fr.harmoniamk.statsmk.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
fun Context.getActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}