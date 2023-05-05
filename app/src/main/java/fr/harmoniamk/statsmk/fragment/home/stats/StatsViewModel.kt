package fr.harmoniamk.statsmk.fragment.home.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@FlowPreview
@ExperimentalCoroutinesApi
class StatsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedIndiv = MutableSharedFlow<List<MKWar>>()
    private val _sharedTeam = MutableSharedFlow<List<MKWar>>()
    private val _sharedPeriodic = MutableSharedFlow<List<MKWar>>()
    private val _sharedPlayers = MutableSharedFlow<Pair<List<User>, List<MKWar>>>()
    private val _sharedOpponents = MutableSharedFlow<Pair<List<Team>, List<MKWar>>>()
    private val _sharedMap = MutableSharedFlow<List<MKWar>>()

    val sharedToast = _sharedToast.asSharedFlow()
    val sharedIndiv = _sharedIndiv.asSharedFlow()
    val sharedTeam = _sharedTeam.asSharedFlow()
    val sharedPeriodic = _sharedPeriodic.asSharedFlow()
    val sharedMap = _sharedMap.asSharedFlow()
    val sharedPlayers = _sharedPlayers.asSharedFlow()
    val sharedOpponents = _sharedOpponents.asSharedFlow()

    val warList = mutableListOf<MKWar>()

    fun bind(onIndiv: Flow<Unit>, onTeam: Flow<Unit>, onMap: Flow<Unit>, onPeriodic: Flow<Unit>, onPlayer: Flow<Unit>, onOpponent: Flow<Unit>) {

        val indivClick = onIndiv.shareIn(viewModelScope, SharingStarted.Lazily)
        val mapClick = onMap.shareIn(viewModelScope, SharingStarted.Lazily)
        val teamClick = onTeam.shareIn(viewModelScope, SharingStarted.Lazily)
        val periodicClick = onPeriodic.shareIn(viewModelScope, SharingStarted.Lazily)
        val playerClick = onPlayer.shareIn(viewModelScope, SharingStarted.Lazily)
        val opponentClick = onOpponent.shareIn(viewModelScope, SharingStarted.Lazily)

        databaseRepository.getWars()
            .onEach {
                warList.clear()
                warList.addAll(it)
            }.launchIn(viewModelScope)



        indivClick
            .map {
               warList.filter { war -> war.hasPlayer(authenticationRepository.user?.uid)  }
            }
            .onEach {
                when (it.isEmpty()) {
                    true -> _sharedToast.emit("Vous devez avoir fait au moins une war pour avoir accès à cette fonctionnalité.")
                    else -> _sharedIndiv.emit(it)
                }
            }.launchIn(viewModelScope)

        teamClick
            .filter { preferencesRepository.currentTeam != null }
            .map { warList.filter { newWar -> newWar.war?.teamHost == preferencesRepository.currentTeam?.mid || newWar.war?.teamOpponent == preferencesRepository.currentTeam?.mid } }
            .onEach {
                when (it.isEmpty()) {
                    true -> _sharedToast.emit("Votre équipe doit avoir fait au moins une war pour avoir accès à cette fonctionnalité.")
                    else -> _sharedTeam.emit(it)
                }
            }.launchIn(viewModelScope)

        periodicClick
            .filter { preferencesRepository.currentTeam != null }
            .map { warList.filter { newWar -> newWar.war?.teamHost == preferencesRepository.currentTeam?.mid || newWar.war?.teamOpponent == preferencesRepository.currentTeam?.mid } }
            .onEach {
                when (it.isEmpty()) {
                    true -> _sharedToast.emit("Votre équipe doit avoir fait au moins une war pour avoir accès à cette fonctionnalité.")
                    else -> _sharedPeriodic.emit(it)
                }
            }.launchIn(viewModelScope)

        playerClick
            .filter { preferencesRepository.currentTeam != null }
            .flatMapLatest { databaseRepository.getUsers() }
            .map { it.filter { user -> user.team == preferencesRepository.currentTeam?.mid } }
            .map { Pair(it, warList) }
            .bind(_sharedPlayers, viewModelScope)

        opponentClick
            .filter { preferencesRepository.currentTeam != null }
            .flatMapLatest { databaseRepository.getTeams() }
            .map { Pair(it, warList) }
            .bind(_sharedOpponents, viewModelScope)

        mapClick.map { warList }.bind(_sharedMap, viewModelScope)

        merge(teamClick, periodicClick, playerClick, opponentClick)
            .filter { preferencesRepository.currentTeam == null }
            .map { "Vous devez intégrer une équipe pour avoir accès à cette fonctionnalité" }
            .bind(_sharedToast, viewModelScope)
    }

}