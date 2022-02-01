package fr.harmoniamk.statsmk.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.harmoniamk.statsmk.extension.bind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class WelcomeViewModel : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    private val _sharedFinish = MutableSharedFlow<Unit>()
    val sharedNext = _sharedNext.asSharedFlow()
    val sharedFinish = _sharedFinish.asSharedFlow()

    fun bind(onNext: Flow<Unit>, onfinish: Flow<Unit>) {
        onNext.bind(_sharedNext, viewModelScope)
        onfinish.bind(_sharedFinish, viewModelScope)
    }

}