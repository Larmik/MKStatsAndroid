package fr.harmoniamk.statsmk.fragment.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {


    private val _sharedDisconnect = MutableSharedFlow<Unit>()
    private val _sharedDisconnectPopup = MutableSharedFlow<Boolean>()
    private val _sharedThemePopup = MutableSharedFlow<Boolean>()
    private val _sharedManageTeam = MutableSharedFlow<Unit>()
    private val _sharedManagePlayers= MutableSharedFlow<Unit>()
    private val _sharedThemeClick = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedUserLabel = MutableSharedFlow<String>()
    val sharedDisconnect = _sharedDisconnect.asSharedFlow()
    val sharedDisconnectPopup = _sharedDisconnectPopup.asSharedFlow()
    val sharedThemePopup = _sharedThemePopup.asSharedFlow()
    val sharedManageTeam = _sharedManageTeam.asSharedFlow()
    val sharedManagePlayers = _sharedManagePlayers.asSharedFlow()
    val sharedThemeClick = _sharedThemeClick.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedUserLabel = _sharedUserLabel.asSharedFlow()

    fun bind(onLogout: Flow<Unit>, onManageTeam: Flow<Unit>, onTheme: Flow<Unit>, onManagePlayers: Flow<Unit>, onMigrate: Flow<Unit>, onPopup: Flow<Boolean>, onPopupTheme: Flow<Boolean>) {
        onLogout.onEach {
            preferencesRepository.currentUser = null
            preferencesRepository.currentTeam = null
            preferencesRepository.authEmail = null
            preferencesRepository.authPassword = null
        }.bind(_sharedDisconnect, viewModelScope)
        onPopup.bind(_sharedDisconnectPopup, viewModelScope)
        onPopupTheme.bind(_sharedThemePopup, viewModelScope)
        val teamClick = onManageTeam.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
        val playersClick = onManagePlayers.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

        teamClick.filter { preferencesRepository.currentTeam != null }.bind(_sharedManageTeam, viewModelScope)
        playersClick.filter { preferencesRepository.currentTeam != null }.bind(_sharedManagePlayers, viewModelScope)
        flowOf(teamClick, playersClick)
            .flattenMerge()
            .filter { preferencesRepository.currentTeam == null }
            .map { "Vous devez intégrer une équipe pour avoir accès à cette fonctionnalité." }
            .bind(_sharedToast, viewModelScope)
        onTheme.bind(_sharedThemeClick, viewModelScope)
        flowOf(preferencesRepository.currentUser?.name)
            .filterNotNull()
            .onEach { delay(20) }
            .bind(_sharedUserLabel, viewModelScope)

        onMigrate
            .onEach {

            }.launchIn(viewModelScope)
    }



}