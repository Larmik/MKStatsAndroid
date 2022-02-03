package fr.harmoniamk.statsmk.features.home.war

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.Team
import fr.harmoniamk.statsmk.database.firebase.model.War
import fr.harmoniamk.statsmk.extension.bind
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
class WarViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedTeam = MutableSharedFlow<Team?>()
    private val _sharedHasTeam = MutableSharedFlow<Team?>()
    private val _sharedCreateWar = MutableSharedFlow<Unit>()
    private val _sharedCurrentWar = MutableSharedFlow<War>()

    val sharedTeam = _sharedTeam.asSharedFlow()
    val sharedHasTeam = _sharedHasTeam.asSharedFlow()
    val sharedCreateWar = _sharedCreateWar.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()

    fun bind(onCodeTeam: Flow<String>, onTeamClick: Flow<Unit>, onCreateWar: Flow<Unit>) {

        var codeTeam: String? = null
        var chosenTeam: String? = null

        flowOf(Unit)
            .onEach {
                delay(50)
                preferencesRepository.currentTeam?.let {
                    _sharedHasTeam.emit(it)
                }
            }.launchIn(viewModelScope)

        onCodeTeam
            .onEach { codeTeam = it }
            .flatMapLatest { firebaseRepository.getTeams() }
            .map { it.singleOrNull { team -> team.accessCode == codeTeam } }
            .onEach { chosenTeam = it?.mid.toString() }
            .bind(_sharedTeam, viewModelScope)

        onTeamClick
            .mapNotNull { preferencesRepository.currentUser?.apply { this.team = chosenTeam } }
            .onEach { preferencesRepository.currentUser = it }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .mapNotNull { chosenTeam }
            .flatMapLatest { firebaseRepository.getTeam(it) }
            .onEach { preferencesRepository.currentTeam = it }
            .onEach { _sharedHasTeam.emit(it) }
            .launchIn(viewModelScope)

        onCreateWar.bind(_sharedCreateWar, viewModelScope)

        firebaseRepository.listenToWars()
            .mapNotNull { it.singleOrNull { war -> war.teamHost == preferencesRepository.currentTeam?.mid } }
            .bind(_sharedCurrentWar, viewModelScope)


    }

}