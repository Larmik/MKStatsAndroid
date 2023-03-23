package fr.harmoniamk.statsmk.fragment.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.displayedString
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedThemePopup = MutableSharedFlow<Boolean>()
    private val _sharedManageTeam = MutableSharedFlow<Unit>()
    private val _sharedManagePlayers= MutableSharedFlow<Unit>()
    private val _sharedThemeClick = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedGoToProfile = MutableSharedFlow<Unit>()
    private val _sharedGoToPlayers = MutableSharedFlow<Unit>()
    private val _sharedProgress = MutableSharedFlow<Boolean>()
    val sharedThemePopup = _sharedThemePopup.asSharedFlow()
    val sharedManageTeam = _sharedManageTeam.asSharedFlow()
    val sharedManagePlayers = _sharedManagePlayers.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedGoToProfile = _sharedGoToProfile.asSharedFlow()
    val sharedGoToPlayers = _sharedGoToPlayers.asSharedFlow()
    val sharedProgress = _sharedProgress.asSharedFlow()


    fun bind(onManageTeam: Flow<Unit>, onTheme: Flow<Unit>, onManagePlayers: Flow<Unit>, onPopupTheme: Flow<Boolean>, onProfileClick: Flow<Unit>, onPlayersClick: Flow<Unit>, onSimulate: Flow<Unit>) {
        onPopupTheme.bind(_sharedThemePopup, viewModelScope)
        val teamClick = onManageTeam.flatMapLatest { authenticationRepository.userRole }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
        val playersClick = onManagePlayers.flatMapLatest { authenticationRepository.userRole }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
        val playerListClick = onPlayersClick.flatMapLatest { authenticationRepository.userRole }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
        teamClick.filter { it >= UserRole.ADMIN.ordinal && preferencesRepository.currentTeam != null }.map{}.bind(_sharedManageTeam, viewModelScope)
        playersClick.filter { it >= UserRole.ADMIN.ordinal && preferencesRepository.currentTeam != null }.map{}.bind(_sharedManagePlayers, viewModelScope)
        flowOf(teamClick, playersClick, playerListClick)
            .flattenMerge()
            .filter { preferencesRepository.currentTeam == null || it < UserRole.ADMIN.ordinal }
            .map { "Vous devez être leader ou admin d'une équipe pour avoir accès à cette fonctionnalité." }
            .bind(_sharedToast, viewModelScope)
        onTheme.bind(_sharedThemeClick, viewModelScope)
        onProfileClick.bind(_sharedGoToProfile, viewModelScope)
        playerListClick.filter { it >= UserRole.ADMIN.ordinal && preferencesRepository.currentTeam != null }.map{}.bind(_sharedGoToPlayers, viewModelScope)
        onSimulate
            .onEach { _sharedProgress.emit(true) }
            .flatMapLatest { simulate() }
            .onEach { _sharedProgress.emit(false) }
            .launchIn(viewModelScope)
    }

    fun simulate() : Flow<Unit> {
        val teamPlayerIds = mutableListOf<String>()
        val teamIdList = mutableListOf<String>()
        return databaseRepository.getUsers()
            .map { it.filter { user -> user.team == preferencesRepository.currentTeam?.mid }.mapNotNull { it.mid } }
            .onEach { teamPlayerIds.addAll(it) }
            .flatMapLatest { databaseRepository.getTeams() }
            .onEach { teamIdList.addAll(it.mapNotNull { team -> team.mid }.filterNot { it == preferencesRepository.currentTeam?.mid }) }
            .map {
                for (i in 1 .. 100) {
                    val playerList = teamPlayerIds.shuffled().subList(1,7)
                    val warTracks = mutableListOf<NewWarTrack>()
                    val alreadyPlayedMaps = mutableListOf<Int>()
                    for (i in 0..11) {
                        val warPositions = mutableListOf<NewWarPositions>()
                        val randomizedPositions = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).shuffled()
                        val alreadyPlayerPosition = mutableListOf<String>()
                        for (position in randomizedPositions.subList(1, 7)) {
                            val playerId = playerList.filterNot { alreadyPlayerPosition.contains(it) }.random()
                            warPositions.add(NewWarPositions(
                                mid = System.currentTimeMillis().toString(),
                                playerId = playerId,
                                position = position
                            ))
                            delay(5)
                            alreadyPlayerPosition.add(playerId)
                        }
                        warTracks.add(
                            NewWarTrack(
                                mid = System.currentTimeMillis().toString(),
                                trackIndex = Maps.values().filterNot { alreadyPlayedMaps.contains(it.ordinal) }.random().ordinal,
                                warPositions = warPositions
                            )
                        )
                    }
                    val war = NewWar(
                        mid = System.currentTimeMillis().toString(),
                        playerHostId = authenticationRepository.user?.uid,
                        teamHost = preferencesRepository.currentTeam?.mid,
                        teamOpponent = teamIdList.random(),
                        createdDate = Date().displayedString(),
                        warTracks = warTracks
                    )
                    firebaseRepository.writeNewWar(war).first()
                }

            }
    }
}