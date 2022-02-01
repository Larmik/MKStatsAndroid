package fr.harmoniamk.statsmk.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedDisconnect = MutableSharedFlow<Unit>()
    val sharedDisconnect = _sharedDisconnect.asSharedFlow()

    fun bind(onLogout: Flow<Unit>) {
        onLogout.onEach { preferencesRepository.isConnected = false }.bind(_sharedDisconnect, viewModelScope)
    }

}