package fr.harmoniamk.statsmk.extension

import androidx.lifecycle.Lifecycle

val Lifecycle.isResumed
    get() = currentState.isAtLeast(Lifecycle.State.RESUMED)