package fr.harmoniamk.statsmk.features.home.settings

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
    private val _sharedManageTeam = MutableSharedFlow<Unit>()
    private val _sharedManagePlayers= MutableSharedFlow<Unit>()
    private val _sharedThemeClick = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedUserLabel = MutableSharedFlow<String>()
    val sharedDisconnect = _sharedDisconnect.asSharedFlow()
    val sharedManageTeam = _sharedManageTeam.asSharedFlow()
    val sharedManagePlayers = _sharedManagePlayers.asSharedFlow()
    val sharedThemeClick = _sharedThemeClick.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedUserLabel = _sharedUserLabel.asSharedFlow()

    fun bind(onLogout: Flow<Unit>, onManageTeam: Flow<Unit>, onTheme: Flow<Unit>, onManagePlayers: Flow<Unit>, onMigrate: Flow<Unit>) {
        onLogout.onEach {
            preferencesRepository.currentUser = null
            preferencesRepository.currentTeam = null
        }.bind(_sharedDisconnect, viewModelScope)
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
                val newWarTracks = mutableListOf<NewWarTrack>()
                val newWarPos = mutableListOf<NewWarPositions>()
                val wars = firebaseRepository.getWars().first()
                wars.forEach { war ->
                    newWarTracks.clear()
                    val warTracks = firebaseRepository.getWarTracks().first().filter { it.warId == war.mid }
                    warTracks.forEach { track ->
                        newWarPos.clear()
                        val warPositions = firebaseRepository.getWarPositions().first().filter { it.warTrackId ==  track.mid}
                        newWarTracks.add(NewWarTrack(track.mid, track.trackIndex, warPositions.map { NewWarPositions(
                            it.mid,
                            it.playerId ?: "-1",
                            it.position
                        ) }))


                    }
                    firebaseRepository.writeNewWar(
                        NewWar(
                        war.mid, war.name, war.playerHostId, war.teamHost, war.teamOpponent, war.createdDate, newWarTracks
                    )
                    ).first()
                }
            }.launchIn(viewModelScope)
    }



}