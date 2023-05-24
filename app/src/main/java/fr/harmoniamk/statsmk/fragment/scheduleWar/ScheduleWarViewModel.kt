package fr.harmoniamk.statsmk.fragment.scheduleWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
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


    val sharedChosenLU = _sharedChosenLU.asSharedFlow()
    val sharedTeams = _sharedTeams.asSharedFlow()
    val sharedButtonVisible = _sharedButtonVisible.asSharedFlow()
    val sharedDismiss = _sharedDismiss.asSharedFlow()


    private val teams = mutableListOf<Team>()
    private val players = mutableListOf<LineUpSelector>()
    private var teamSelected: String? = null

    fun bind(dispo: WarDispo, onSearch: Flow<String>, onTeamClick: Flow<Team>, onPlayerDelete: Flow<LineUpSelector>, onWarScheduled: Flow<Unit>) {

        onTeamClick
            .onEach { teamSelected = it.mid }
            .map { players.size == 6 }
            .bind(_sharedButtonVisible, viewModelScope)


        onWarScheduled
            .flatMapLatest { firebaseRepository.getDispos() }
            .map {
                val final = mutableListOf<WarDispo>()
                it.forEach {
                    when (it.dispoHour == dispo.dispoHour) {
                        true -> {
                            final.add(it.apply {
                                this.opponentId = teamSelected
                                this.lineUp = players.map { it.user?.mid ?: "" }
                            })
                        }
                        else -> final.add(it)
                    }
                }
                final
            }
            .flatMapLatest { firebaseRepository.writeDispo(it) }
            .onEach { _sharedDismiss.emit(Unit) }
            .launchIn(viewModelScope)

        flowOf(dispo)
            .map {
                val list = mutableListOf<Pair<String, Int>>()
                it.dispoPlayers.forEach {
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
}