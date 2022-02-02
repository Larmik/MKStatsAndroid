package fr.harmoniamk.statsmk.features.welcome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.harmoniamk.statsmk.extension.bind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class WelcomeViewModel : ViewModel() {

    private val _sharedFinish = MutableSharedFlow<Unit>()
    private val _sharedNoCode = MutableSharedFlow<Unit>()

    val sharedFinish = _sharedFinish.asSharedFlow()
    val sharedNoCode = _sharedNoCode.asSharedFlow()

    fun bind(onFinish: Flow<Unit>, onNoCode: Flow<Unit>) {
        onFinish.bind(_sharedFinish, viewModelScope)
        onNoCode.bind(_sharedNoCode, viewModelScope)
    }

}