package fr.harmoniamk.statsmk.fragment.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
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
class SettingsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {

    private val _sharedThemePopup = MutableSharedFlow<Boolean>()
    private val _sharedManageTeam = MutableSharedFlow<Unit>()
    private val _sharedManagePlayers= MutableSharedFlow<Unit>()
    private val _sharedThemeClick = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedGoToProfile = MutableSharedFlow<Unit>()
    val sharedThemePopup = _sharedThemePopup.asSharedFlow()
    val sharedManageTeam = _sharedManageTeam.asSharedFlow()
    val sharedManagePlayers = _sharedManagePlayers.asSharedFlow()
    val sharedThemeClick = _sharedThemeClick.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedGoToProfile = _sharedGoToProfile.asSharedFlow()

    fun bind(onManageTeam: Flow<Unit>, onTheme: Flow<Unit>, onManagePlayers: Flow<Unit>, onMigrate: Flow<Unit>, onPopupTheme: Flow<Boolean>, onProfileClick: Flow<Unit>) {
        onPopupTheme.bind(_sharedThemePopup, viewModelScope)
        val teamClick = onManageTeam.flatMapLatest { authenticationRepository.userRole }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
        val playersClick = onManagePlayers.flatMapLatest { authenticationRepository.userRole }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
        teamClick.filter { it >= UserRole.ADMIN.ordinal && preferencesRepository.currentTeam != null }.map{}.bind(_sharedManageTeam, viewModelScope)
        playersClick.filter { it >= UserRole.ADMIN.ordinal && preferencesRepository.currentTeam != null }.map{}.bind(_sharedManagePlayers, viewModelScope)
        flowOf(teamClick, playersClick)
            .flattenMerge()
            .filter { preferencesRepository.currentTeam == null || it < UserRole.ADMIN.ordinal }
            .map { "Vous devez être leader ou admin d'une équipe pour avoir accès à cette fonctionnalité." }
            .bind(_sharedToast, viewModelScope)
        onTheme.bind(_sharedThemeClick, viewModelScope)
        onProfileClick.bind(_sharedGoToProfile, viewModelScope)
        onMigrate.onEach {}.launchIn(viewModelScope)
    }
}