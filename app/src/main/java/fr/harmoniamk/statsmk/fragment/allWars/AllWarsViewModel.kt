package fr.harmoniamk.statsmk.fragment.allWars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.enums.TrackSortType
import fr.harmoniamk.statsmk.enums.WarSortType
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.TrackStats
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
class AllWarsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedSortTypeSelected = MutableStateFlow(WarSortType.DATE)


    val sharedWars = _sharedWars.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedSortTypeSelected = _sharedSortTypeSelected.asStateFlow()

    private val wars = mutableListOf<MKWar>()
    private val teams = mutableListOf<Team>()

    fun bind(onItemClick: Flow<MKWar>, onSearch: Flow<String>, onSortClick: Flow<WarSortType>) {
        firebaseRepository.getNewWars()
            .mapNotNull { list -> list.filter { war -> war.teamHost == preferencesRepository.currentTeam?.mid }.sortedByDescending { it.createdDate?.formatToDate() }.map { MKWar(it) } }
            .flatMapLatest { it.withName(firebaseRepository) }
            .onEach {
                wars.clear()
                wars.addAll(it)
                _sharedWars.emit(when (_sharedSortTypeSelected.value) {
                    WarSortType.DATE -> it.sortedByDescending { it.war?.mid }
                    WarSortType.SCORE -> it.sortedByDescending { it.scoreHost }
                    WarSortType.TEAM -> it.sortedByDescending { it.name }
                })
            }
            .flatMapLatest { firebaseRepository.getTeams() }
            .onEach {
                teams.clear()
                teams.addAll(it)
            }
            .launchIn(viewModelScope)
        onItemClick.bind(_sharedWarClick, viewModelScope)

        onSearch
            .map { searched ->
                val filteredTeams = teams.filter { it.name?.toLowerCase()?.contains(searched.toLowerCase()).isTrue }
                val filteredWars = mutableListOf<MKWar>()
                filteredTeams.forEach { team ->
                    filteredWars.addAll(wars.filter { it.war?.teamOpponent?.equals(team.mid).isTrue })
                }
                when (searched.isEmpty()) {
                    true -> wars
                    else -> filteredWars
                }
            }
            .onEach {
                _sharedWars.emit(it)
            }.launchIn(viewModelScope)

        onSortClick
            .onEach {
                _sharedSortTypeSelected.emit(it)
                val sortedWars = when (it) {
                    WarSortType.DATE -> wars.sortedByDescending { it.war?.createdDate }
                    WarSortType.TEAM -> wars.sortedBy { it.name }
                    WarSortType.SCORE -> wars.sortedByDescending { it.scoreHost }
                }
                _sharedWars.emit(sortedWars)
            }.launchIn(viewModelScope)
    }

}