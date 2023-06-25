package fr.harmoniamk.statsmk.fragment.scheduleWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import fr.harmoniamk.statsmk.model.firebase.LineUp
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ScheduleWarViewModel @Inject constructor(private val databaseRepository: DatabaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface): ViewModel() {

    private val _sharedChosenLU = MutableSharedFlow<List<LineUpSelector>>()
    private val _sharedTeams = MutableSharedFlow<List<Team>>()
    private val _sharedDismiss = MutableSharedFlow<Unit>()
    private val _sharedButtonVisible = MutableSharedFlow<Boolean>()
    private val _sharedShowTeamHostPopup = MutableSharedFlow<List<UserSelector>?>()
    private val _sharedShowOpponentHostPopup = MutableSharedFlow<Boolean>()
    private val _sharedChosenHost = MutableSharedFlow<String>()


    val sharedChosenLU = _sharedChosenLU.asSharedFlow()
    val sharedTeams = _sharedTeams.asSharedFlow()
    val sharedButtonVisible = _sharedButtonVisible.asSharedFlow()
    val sharedDismiss = _sharedDismiss.asSharedFlow()
    val sharedShowTeamHostPopup = _sharedShowTeamHostPopup.asSharedFlow()
    val sharedShowOpponentHostPopup = _sharedShowOpponentHostPopup.asSharedFlow()
    val sharedChosenHost = _sharedChosenHost.asSharedFlow()


    private val teams = mutableListOf<Team>()
    private val players = mutableListOf<LineUpSelector>()
    private var teamSelected: String? = null
    private var chosenHost: String? = null

    fun bind(dispo: WarDispo, onSearch: Flow<String>, onTeamClick: Flow<Team>, onPlayerDelete: Flow<LineUpSelector>, onWarScheduled: Flow<Unit>, onTeamHostClick: Flow<Unit>, onOpponentHostClick: Flow<Unit>) {

        onOpponentHostClick.map { true }.bind(_sharedShowOpponentHostPopup, viewModelScope)
        onTeamHostClick
            .mapNotNull {
                players.map { UserSelector(it.user, false) }
            }
            .bind(_sharedShowTeamHostPopup, viewModelScope)

        onTeamClick
            .onEach { teamSelected = it.mid }
            .map { players.size == 6 }
            .bind(_sharedButtonVisible, viewModelScope)


        onWarScheduled
            .flatMapLatest { firebaseRepository.getDispos() }
            .map {
                dispo.apply {
                    this.opponentId = teamSelected
                    this.lineUp = players.map { LineUp( it.user?.mid ?: "", it.user?.discordId ?: "") }
                    this.host = chosenHost
                }
            }
            .flatMapLatest { firebaseRepository.writeDispo(it) }
            .onEach { _sharedDismiss.emit(Unit) }
            .launchIn(viewModelScope)

        flowOf(dispo)
            .map {
                val list = mutableListOf<Pair<String, Int>>()
                it.dispoPlayers?.forEach {
                    when  {
                        it.dispo == 0 -> list.addAll(it.players?.map { Pair(it, 0) }.orEmpty())
                        it.dispo == 1 && list.size < 6 -> list.addAll(it.players?.map { Pair(it, 1) }.orEmpty())
                    }
                }
                list
            }
            .map {
                val nameList = mutableListOf<Pair<User, Int>>()
                it.forEach { pair ->
                    databaseRepository.getUser(pair.first).firstOrNull()?.let {
                        nameList.add(Pair(it, pair.second))
                    }
                }
                nameList
            }.onEach { userList ->
                players.clear()
                players.addAll(userList.map { LineUpSelector(it.first, it.second) })
                 _sharedChosenLU.emit(players)
            }.launchIn(viewModelScope)

        databaseRepository.getTeams()
            .map {
                teams.clear()
                teams.addAll(it.filterNot { team -> team.mid == preferencesRepository.currentTeam?.mid })
                teams.sortedBy { it.name }
            }
            .bind(_sharedTeams, viewModelScope)
        onSearch
            .map { searched ->
                teams.filter {
                    it.shortName?.toLowerCase(Locale.ROOT)
                        ?.contains(searched.toLowerCase(Locale.ROOT)).isTrue || it.name?.toLowerCase(
                        Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)) ?: true
                }.sortedBy { it.name }.filterNot { vm -> vm.mid == preferencesRepository.currentTeam?.mid }
            }
            .bind(_sharedTeams, viewModelScope)

        onPlayerDelete
            .onEach {
                players.remove(it)
                _sharedChosenLU.emit(players)
                _sharedButtonVisible.emit(players.size == 6 && teamSelected != null)
            }.launchIn(viewModelScope)

    }

    fun bindTeamPopup(onPlayerSelected: Flow<String>, onValidate: Flow<Unit>, onDismiss: Flow<Unit> ) {
        onDismiss.onEach { _sharedShowTeamHostPopup.emit(null) }.launchIn(viewModelScope)
        onPlayerSelected
            .onEach {
                this.chosenHost = it
            }
            .launchIn(viewModelScope)

        onValidate
            .mapNotNull { chosenHost }
            .flatMapLatest { databaseRepository.getUser(it) }
            .mapNotNull { it?.name }
            .onEach {
                _sharedChosenHost.emit(it)
                _sharedShowTeamHostPopup.emit(null)
            }.launchIn(viewModelScope)
    }

    fun bindOpponentPopup(onCodeAdded: Flow<String>, onValidate: Flow<Unit>, onDismiss: Flow<Unit> ) {
        onDismiss.onEach { _sharedShowOpponentHostPopup.emit(false) }.launchIn(viewModelScope)
        onCodeAdded.onEach { this.chosenHost = it }.launchIn(viewModelScope)
        onValidate
            .mapNotNull { chosenHost }
            .onEach {
                _sharedChosenHost.emit(it)
                _sharedShowOpponentHostPopup.emit(false)
            }.launchIn(viewModelScope)
    }

}