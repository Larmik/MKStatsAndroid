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

    private val _sharedIndiv = MutableSharedFlow<List<MKWar>>()
    private val _sharedTeam = MutableSharedFlow<List<MKWar>>()
    private val _sharedPeriodic = MutableSharedFlow<List<MKWar>>()
    private val _sharedPlayers = MutableSharedFlow<Pair<List<User>, List<MKWar>>>()
    private val _sharedOpponents = MutableSharedFlow<Pair<List<Team>, List<MKWar>>>()
    private val _sharedMap = MutableSharedFlow<List<MKWar>>()

    val sharedIndiv = _sharedIndiv.asSharedFlow()
    val sharedTeam = _sharedTeam.asSharedFlow()
    val sharedPeriodic = _sharedPeriodic.asSharedFlow()
    val sharedMap = _sharedMap.asSharedFlow()
    val sharedPlayers = _sharedPlayers.asSharedFlow()
    val sharedOpponents = _sharedOpponents.asSharedFlow()

    val warList = mutableListOf<MKWar>()

    fun bind(onIndiv: Flow<Unit>, onTeam: Flow<Unit>, onMap: Flow<Unit>, onPeriodic: Flow<Unit>, onPlayer: Flow<Unit>, onOpponent: Flow<Unit>) {

        databaseRepository.getWars()
            .onEach {
                warList.clear()
                warList.addAll(it)
            }.launchIn(viewModelScope)

        onIndiv
            .map { warList.filter { war -> war.hasPlayer(authenticationRepository.user?.uid)  } }
            .filterNot { it.isEmpty() }
            .onEach {_sharedIndiv.emit(it) }
            .launchIn(viewModelScope)

        onTeam
            .filter { preferencesRepository.currentTeam != null }
            .map { warList.filter { newWar -> newWar.war?.teamHost == preferencesRepository.currentTeam?.mid || newWar.war?.teamOpponent == preferencesRepository.currentTeam?.mid } }
            .filterNot { it.isEmpty() }
            .onEach {_sharedTeam.emit(it) }
            .launchIn(viewModelScope)

        onPeriodic
            .filter { preferencesRepository.currentTeam != null }
            .map { warList.filter { newWar -> newWar.war?.teamHost == preferencesRepository.currentTeam?.mid || newWar.war?.teamOpponent == preferencesRepository.currentTeam?.mid } }
            .filterNot { it.isEmpty() }
            .onEach { _sharedPeriodic.emit(it) }
            .launchIn(viewModelScope)

        onPlayer
            .filter { preferencesRepository.currentTeam != null }
            .flatMapLatest { databaseRepository.getUsers() }
            .map { it.filter { user -> user.team == preferencesRepository.currentTeam?.mid } }
            .filterNot { warList.isEmpty() }
            .onEach { _sharedPlayers.emit(Pair(it, warList)) }
            .launchIn(viewModelScope)

        onOpponent
            .filter { preferencesRepository.currentTeam != null }
            .flatMapLatest { databaseRepository.getTeams() }
            .filterNot { warList.isEmpty() }
            .onEach { _sharedOpponents.emit(Pair(it, warList)) }
            .launchIn(viewModelScope)

        onMap.map { warList }.bind(_sharedMap, viewModelScope)


    }

}