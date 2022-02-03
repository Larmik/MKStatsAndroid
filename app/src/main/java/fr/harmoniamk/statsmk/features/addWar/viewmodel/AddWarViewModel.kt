package fr.harmoniamk.statsmk.features.addWar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.harmoniamk.statsmk.extension.bind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AddWarViewModel : ViewModel() {

    private val _sharedGoToWait = MutableSharedFlow<Unit>()
    val sharedGoToWait = _sharedGoToWait.asSharedFlow()

    fun bind(onCreateWar: Flow<Unit>) {
        onCreateWar.bind(_sharedGoToWait, viewModelScope)
    }
}