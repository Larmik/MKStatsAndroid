package fr.harmoniamk.statsmk.fragment.home.war

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.local.MKTeam
import fr.harmoniamk.statsmk.model.local.MKWar
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
    private val _sharedCurrentWarClick = MutableSharedFlow<MKWar>()
    private val _sharedLastWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedBestWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedGoToWar = MutableSharedFlow<MKWar>()
    private val _sharedButtonVisible = MutableSharedFlow<Boolean>()

    val sharedTeam = _sharedTeam.asSharedFlow()
    val sharedHasTeam = _sharedHasTeam.asSharedFlow()
    val sharedTeamName = _sharedTeamName.asSharedFlow()
    val sharedCreateWar = _sharedCreateWar.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()
    val sharedCurrentWarClick = _sharedCurrentWarClick.asSharedFlow()
    val sharedLastWars = _sharedLastWars.asSharedFlow()
    val sharedBestWars = _sharedBestWars.asSharedFlow()
    val sharedGoToWar = _sharedGoToWar.asSharedFlow()
    val sharedButtonVisible = _sharedButtonVisible.asSharedFlow()

    fun bind(onCodeTeam: Flow<String>, onTeamClick: Flow<Unit>, onCreateWar: Flow<Unit>, onCurrentWarClick: Flow<Unit>, onWarClick: Flow<MKWar>) {

        var codeTeam: String? = null
        var chosenTeam: String? = null
        var war: MKWar? = null

        flowOf(preferencesRepository.currentUser?.isAdmin.isTrue)
            .bind(_sharedButtonVisible, viewModelScope)

        val warsFlow = flowOf(firebaseRepository.getNewWars(), firebaseRepository.listenToNewWars())
            .flattenMerge()
            .map { it.map { MKWar(it) } }
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

        warsFlow
            .mapNotNull { war = it.getCurrent(preferencesRepository.currentTeam?.mid); war}
            .flatMapLatest { listOf(it).withName(firebaseRepository) }
            .onEach { _sharedCurrentWar.emit(it.singleOrNull()) }
            .launchIn(viewModelScope)

        warsFlow
            .map { it.getLasts(preferencesRepository.currentTeam?.mid) }
            .map {
                val temp = mutableListOf<MKWar>()
                it.forEach {
                    val hostName = firebaseRepository.getTeam(it.war?.teamHost).firstOrNull()?.shortName
                    val opponentName = firebaseRepository.getTeam(it.war?.teamOpponent).firstOrNull()?.shortName
                    temp.add(it.apply { this.name = "$hostName - $opponentName" })
                }
                temp
            }
            .bind(_sharedLastWars, viewModelScope)

        warsFlow
            .flatMapLatest { it.getBests(preferencesRepository.currentTeam?.mid).withName(firebaseRepository) }
            .bind(_sharedBestWars, viewModelScope)

        warsFlow
            .mapNotNull { preferencesRepository.currentTeam?.name }
            .bind(_sharedTeamName, viewModelScope)

        firebaseRepository.getUsers()
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
                val wars = firebaseRepository.getNewWars().first()
                preferencesRepository.currentTeam = team
                war = wars.map { MKWar(it) }.getCurrent(preferencesRepository.currentTeam?.mid)
                _sharedHasTeam.emit(true)
                _sharedTeamName.emit(preferencesRepository.currentTeam?.name ?: "")
                _sharedLastWars.emit(wars.map { w -> MKWar(w) }.getLasts(preferencesRepository.currentTeam?.mid))
                _sharedCurrentWar.emit(war)

            }.launchIn(viewModelScope)

        onCurrentWarClick
            .mapNotNull { war }
            .bind(_sharedCurrentWarClick, viewModelScope)

        onWarClick.bind(_sharedGoToWar, viewModelScope)
        onCreateWar.bind(_sharedCreateWar, viewModelScope)
    }

}