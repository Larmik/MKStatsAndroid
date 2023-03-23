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
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@FlowPreview
@ExperimentalCoroutinesApi
class StatsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedIndiv = MutableSharedFlow<List<MKWar>>()
    private val _sharedTeam = MutableSharedFlow<List<MKWar>>()
    private val _sharedPeriodic = MutableSharedFlow<List<MKWar>>()
    private val _sharedPlayers = MutableSharedFlow<List<User>>()
    private val _sharedOpponents = MutableSharedFlow<List<Team>>()
    private val _sharedMap = MutableSharedFlow<Unit>()

    val sharedToast = _sharedToast.asSharedFlow()
    val sharedIndiv = _sharedIndiv.asSharedFlow()
    val sharedTeam = _sharedTeam.asSharedFlow()
    val sharedPeriodic = _sharedPeriodic.asSharedFlow()
    val sharedMap = _sharedMap.asSharedFlow()
    val sharedPlayers = _sharedPlayers.asSharedFlow()
    val sharedOpponents = _sharedOpponents.asSharedFlow()

    fun bind(onIndiv: Flow<Unit>, onTeam: Flow<Unit>, onMap: Flow<Unit>, onPeriodic: Flow<Unit>, onPlayer: Flow<Unit>, onOpponent: Flow<Unit>) {

        val indivClick = onIndiv.shareIn(viewModelScope, SharingStarted.Lazily)
        val mapClick = onMap.shareIn(viewModelScope, SharingStarted.Lazily)
        val teamClick = onTeam.shareIn(viewModelScope, SharingStarted.Lazily)
        val periodicClick = onPeriodic.shareIn(viewModelScope, SharingStarted.Lazily)
        val playerClick = onPlayer.shareIn(viewModelScope, SharingStarted.Lazily)
        val opponentClick = onOpponent.shareIn(viewModelScope, SharingStarted.Lazily)

        indivClick
            .flatMapLatest { firebaseRepository.getNewWars() }
            .map {
                it.map { MKWar(it) }.filter { war -> war.hasPlayer(authenticationRepository.user?.uid)  }
            }
            .onEach {
                when (it.isEmpty()) {
                    true -> _sharedToast.emit("Vous devez avoir fait au moins une war pour avoir accès à cette fonctionnalité.")
                    else -> _sharedIndiv.emit(it)
                }
            }.launchIn(viewModelScope)

        teamClick
            .filter { preferencesRepository.currentTeam != null }
            .flatMapLatest { firebaseRepository.getNewWars() }
            .map { it.filter { newWar -> newWar.teamHost == preferencesRepository.currentTeam?.mid || newWar.teamOpponent == preferencesRepository.currentTeam?.mid }.map { MKWar(it) } }
            .onEach {
                when (it.isEmpty()) {
                    true -> _sharedToast.emit("Votre équipe doit avoir fait au moins une war pour avoir accès à cette fonctionnalité.")
                    else -> _sharedTeam.emit(it)
                }
            }.launchIn(viewModelScope)

        periodicClick
            .filter { preferencesRepository.currentTeam != null }
            .flatMapLatest { firebaseRepository.getNewWars() }
            .map { it.filter { newWar -> newWar.teamHost == preferencesRepository.currentTeam?.mid || newWar.teamOpponent == preferencesRepository.currentTeam?.mid }.map { MKWar(it) } }
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
            .bind(_sharedPlayers, viewModelScope)

        opponentClick
            .filter { preferencesRepository.currentTeam != null }
            .flatMapLatest { databaseRepository.getTeams() }
            .bind(_sharedOpponents, viewModelScope)

        mapClick.bind(_sharedMap, viewModelScope)

        merge(teamClick, periodicClick, playerClick, opponentClick)
            .filter { preferencesRepository.currentTeam == null }
            .map { "Vous devez intégrer une équipe pour avoir accès à cette fonctionnalité" }
            .bind(_sharedToast, viewModelScope)
    }

}