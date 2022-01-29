package fr.harmoniamk.statsmk.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

fun <T> Flow<T>.bind(target: MutableSharedFlow<T>, scope: CoroutineScope) = onEach { target.emit(it) }.launchIn(scope)
