package fr.harmoniamk.statsmk.features.home.settings

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
    private val _sharedManageTeam = MutableSharedFlow<Unit>()
    val sharedDisconnect = _sharedDisconnect.asSharedFlow()
    val sharedManageTeam = _sharedManageTeam.asSharedFlow()

    fun bind(onLogout: Flow<Unit>, onManageTeam: Flow<Unit>) {
        onLogout.onEach {
            preferencesRepository.currentUser = null
            preferencesRepository.currentTeam = null
        }.bind(_sharedDisconnect, viewModelScope)
        onManageTeam.bind(_sharedManageTeam, viewModelScope)

    }

}