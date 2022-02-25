package fr.harmoniamk.statsmk.features.home.war

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.model.Team
import fr.harmoniamk.statsmk.database.model.War
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.getBests
import fr.harmoniamk.statsmk.extension.getCurrent
import fr.harmoniamk.statsmk.extension.getLasts
import fr.harmoniamk.statsmk.model.MKTeam
import fr.harmoniamk.statsmk.model.MKWar
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedTeam = MutableSharedFlow<MKTeam?>()
    private val _sharedHasTeam = MutableSharedFlow<Boolean>()
    private val _sharedTeamName = MutableSharedFlow<String>()
    private val _sharedCreateWar = MutableSharedFlow<Unit>()
    private val _sharedCurrentWar = MutableSharedFlow<MKWar?>()
    private val _sharedCurrentWarClick = MutableSharedFlow<War>()
    private val _sharedLastWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedBestWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedGoToWar = MutableSharedFlow<War>()

    val sharedTeam = _sharedTeam.asSharedFlow()
    val sharedHasTeam = _sharedHasTeam.asSharedFlow()
    val sharedTeamName = _sharedTeamName.asSharedFlow()
    val sharedCreateWar = _sharedCreateWar.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()
    val sharedCurrentWarClick = _sharedCurrentWarClick.asSharedFlow()
    val sharedLastWars = _sharedLastWars.asSharedFlow()
    val sharedBestWars = _sharedBestWars.asSharedFlow()
    val sharedGoToWar = _sharedGoToWar.asSharedFlow()

    fun bind(onCodeTeam: Flow<String>, onTeamClick: Flow<Unit>, onCreateWar: Flow<Unit>, onCurrentWarClick: Flow<Unit>, onWarClick: Flow<War>) {

        var codeTeam: String? = null
        var chosenTeam: String? = null
        var war: MKWar? = null

        val warsFlow = firebaseRepository.getWars()
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

        flowOf(warsFlow, firebaseRepository.listenToWars())
            .flattenMerge()
            .map { war = it.map { w -> MKWar(w) }.getCurrent(preferencesRepository.currentTeam?.mid); war }
            .bind(_sharedCurrentWar, viewModelScope)

        warsFlow
            .map { it.map { w -> MKWar(w) }.getLasts(preferencesRepository.currentTeam?.mid)}
            .bind(_sharedLastWars, viewModelScope)

        warsFlow
            .mapNotNull { it.map { w -> MKWar(w) }.getBests(preferencesRepository.currentTeam?.mid) }
            .bind(_sharedBestWars, viewModelScope)

        warsFlow
            .mapNotNull { preferencesRepository.currentTeam?.name }
            .bind(_sharedTeamName, viewModelScope)

        firebaseRepository.listenToUsers()
            .map{ it.singleOrNull{ user -> user.mid == preferencesRepository.currentUser?.mid}?.team != "-1" }
            .bind(_sharedHasTeam, viewModelScope)

        onCodeTeam
            .onEach { codeTeam = it }
            .flatMapLatest { firebaseRepository.getTeams() }
            .map { it.singleOrNull { team -> team.accessCode == codeTeam } }
            .onEach { chosenTeam = it?.mid.toString() }
            .map { MKTeam(it) }
            .bind(_sharedTeam, viewModelScope)

        onTeamClick
            .mapNotNull { preferencesRepository.currentUser?.apply { this.team = chosenTeam } }
            .onEach { preferencesRepository.currentUser = it }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .mapNotNull { chosenTeam }
            .flatMapLatest { firebaseRepository.getTeam(it) }
            .onEach { team ->
                val wars = firebaseRepository.getWars().first()
                preferencesRepository.currentTeam = team
                war = wars.map { MKWar(it) }.getCurrent(preferencesRepository.currentTeam?.mid)
                _sharedHasTeam.emit(true)
                _sharedTeamName.emit(preferencesRepository.currentTeam?.name ?: "")
                _sharedLastWars.emit(wars.map { w -> MKWar(w) }.getLasts(preferencesRepository.currentTeam?.mid))
                _sharedCurrentWar.emit(war)

            }.launchIn(viewModelScope)

        onCurrentWarClick
            .mapNotNull { war?.war }
            .bind(_sharedCurrentWarClick, viewModelScope)

        onWarClick.bind(_sharedGoToWar, viewModelScope)
        onCreateWar.bind(_sharedCreateWar, viewModelScope)
    }

}