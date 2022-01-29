package fr.harmoniamk.statsmk.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*

class PositionViewModel : ViewModel() {

    private val _sharedPos = MutableSharedFlow<Int>()
    val sharedPos = _sharedPos.asSharedFlow()

    fun bind(
        onPos1: Flow<Unit>,
        onPos2: Flow<Unit>,
        onPos3: Flow<Unit>,
        onPos4: Flow<Unit>,
        onPos5: Flow<Unit>,
        onPos6: Flow<Unit>,
        onPos7: Flow<Unit>,
        onPos8: Flow<Unit>,
        onPos9: Flow<Unit>,
        onPos10: Flow<Unit>,
        onPos11: Flow<Unit>,
        onPos12: Flow<Unit>
    ) {

        onPos1.onEach { _sharedPos.emit(1) }.launchIn(viewModelScope)
        onPos2.onEach { _sharedPos.emit(2) }.launchIn(viewModelScope)
        onPos3.onEach { _sharedPos.emit(3) }.launchIn(viewModelScope)
        onPos4.onEach { _sharedPos.emit(4) }.launchIn(viewModelScope)
        onPos5.onEach { _sharedPos.emit(5) }.launchIn(viewModelScope)
        onPos6.onEach { _sharedPos.emit(6) }.launchIn(viewModelScope)
        onPos7.onEach { _sharedPos.emit(7) }.launchIn(viewModelScope)
        onPos8.onEach { _sharedPos.emit(8) }.launchIn(viewModelScope)
        onPos9.onEach { _sharedPos.emit(9) }.launchIn(viewModelScope)
        onPos10.onEach { _sharedPos.emit(10) }.launchIn(viewModelScope)
        onPos11.onEach { _sharedPos.emit(11) }.launchIn(viewModelScope)
        onPos12.onEach { _sharedPos.emit(12) }.launchIn(viewModelScope)

    }
}