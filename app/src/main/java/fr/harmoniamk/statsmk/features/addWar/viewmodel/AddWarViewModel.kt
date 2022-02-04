package fr.harmoniamk.statsmk.features.addWar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.harmoniamk.statsmk.extension.bind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AddWarViewModel : ViewModel() {

    private val _sharedGoToWait = MutableSharedFlow<Unit>()
    private val _sharedGoToCurrentWar = MutableSharedFlow<Unit>()
    val sharedGoToWait = _sharedGoToWait.asSharedFlow()
    val sharedGoToCurrentWar = _sharedGoToCurrentWar.asSharedFlow()

    fun bind(onCreateWar: Flow<Unit>, onWarBegin: Flow<Unit>) {
        onCreateWar.bind(_sharedGoToWait, viewModelScope)
        onWarBegin.bind(_sharedGoToCurrentWar, viewModelScope)
    }
}