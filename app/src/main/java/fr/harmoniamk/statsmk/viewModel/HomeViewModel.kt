package fr.harmoniamk.statsmk.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _sharedBackToFirstPage = MutableSharedFlow<Unit>()
    private val _sharedClose = MutableSharedFlow<Unit>()
    val sharedBackToFirstPage = _sharedBackToFirstPage.asSharedFlow()
    val sharedClose = _sharedClose.asSharedFlow()

    fun bind(onDisconnect: Flow<Unit>, onBack: Flow<Unit>) {
        onDisconnect.bind(_sharedBackToFirstPage, viewModelScope)
        onBack.bind(_sharedClose, viewModelScope)
    }

}