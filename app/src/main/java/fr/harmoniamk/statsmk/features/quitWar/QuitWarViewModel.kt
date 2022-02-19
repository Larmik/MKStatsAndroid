package fr.harmoniamk.statsmk.features.quitWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.harmoniamk.statsmk.extension.bind
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class QuitWarViewModel : ViewModel() {

    private val _onDismiss = MutableSharedFlow<Unit>()
    private val _onWarQuit = MutableSharedFlow<Unit>()

    val onDismiss = _onDismiss.asSharedFlow()
    val onWarQuit = _onWarQuit.asSharedFlow()

    fun bind(onQuit: Flow<Unit>, onBack: Flow<Unit>) {
        onBack.bind(_onDismiss, viewModelScope)
        onQuit.bind(_onWarQuit, viewModelScope)
    }

}