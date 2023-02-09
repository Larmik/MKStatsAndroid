package fr.harmoniamk.statsmk.fragment.allWars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.WarFilterType
import fr.harmoniamk.statsmk.enums.WarSortType
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AllWarsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {
    private val filters = mutableListOf<WarFilterType>()

    private val _sharedWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedLoading = MutableSharedFlow<Boolean>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedSortTypeSelected = MutableStateFlow(WarSortType.DATE)
    private val _sharedFilterList = MutableSharedFlow<List<WarFilterType>>()

    val sharedWars = _sharedWars.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedSortTypeSelected = _sharedSortTypeSelected.asStateFlow()
    val sharedFilterList = _sharedFilterList.asSharedFlow()

    private val wars = mutableListOf<MKWar>()
    private val teams = mutableListOf<Team>()

    fun bind(onItemClick: Flow<MKWar>, onSearch: Flow<String>, onSortClick: Flow<WarSortType>, onFilterClick: Flow<WarFilterType>) {
        flowOf(true).bind(_sharedLoading, viewModelScope)
        firebaseRepository.getNewWars()
            .mapNotNull { list -> list.filter { war -> war.teamHost == preferencesRepository.currentTeam?.mid }.sortedByDescending { it.mid }.map { MKWar(it) } }
            .flatMapLatest { it.withName(firebaseRepository) }
            .onEach {
                wars.clear()
                wars.addAll(it)
                when {
                    filters.contains(WarFilterType.WEEK) -> wars.removeAll(it.filterNot { it.isThisWeek })
                    filters.contains(WarFilterType.OFFICIAL) -> wars.removeAll(it.filterNot { it.war?.isOfficial.isTrue })
                    filters.contains(WarFilterType.PLAY) -> wars.removeAll(it.filterNot { it.hasPlayer(authenticationRepository.user?.uid) })
                }
                _sharedWars.emit(when (_sharedSortTypeSelected.value) {
                    WarSortType.DATE -> wars.sortedByDescending { it.war?.mid }.filter { it.isOver }
                    WarSortType.SCORE -> wars.sortedByDescending { it.scoreHost }.filter { it.isOver }
                    WarSortType.TEAM -> wars.sortedBy { it.name }.filter { it.isOver }
                })
                _sharedFilterList.emit(filters)
                _sharedLoading.emit(false)
            }
            .flatMapLatest { firebaseRepository.getTeams() }
            .onEach {
                teams.clear()
                teams.addAll(it)
            }.launchIn(viewModelScope)

        onSearch
            .onEach { searched ->
                val filteredWars = mutableListOf<MKWar>()
                when (searched.isEmpty()) {
                    true -> {
                        filteredWars.addAll(wars)
                        when {
                            filters.contains(WarFilterType.WEEK) -> filteredWars.removeAll(wars.filterNot { it.isThisWeek })
                            filters.contains(WarFilterType.OFFICIAL) -> filteredWars.removeAll(wars.filterNot { it.war?.isOfficial.isTrue })
                            filters.contains(WarFilterType.PLAY) -> filteredWars.removeAll(wars.filterNot { it.hasPlayer(authenticationRepository.user?.uid) })
                        }
                        when (_sharedSortTypeSelected.value) {
                            WarSortType.DATE -> filteredWars.sortByDescending { it.war?.mid }
                            WarSortType.SCORE -> filteredWars.sortByDescending { it.scoreHost }
                            WarSortType.TEAM -> filteredWars.sortBy { it.name }
                        }
                    }
                    else -> {
                        val filteredTeams = teams.filter { it.name?.toLowerCase()?.contains(searched.toLowerCase()).isTrue || it.shortName?.toLowerCase()?.contains(searched.toLowerCase()).isTrue}
                        filteredTeams.forEach { team -> filteredWars.addAll(wars.filter { it.war?.teamOpponent?.equals(team.mid).isTrue }) }
                        when {
                            filters.contains(WarFilterType.WEEK) -> filteredWars.removeAll(filteredWars.filterNot { it.isThisWeek })
                            filters.contains(WarFilterType.OFFICIAL) -> filteredWars.removeAll(filteredWars.filterNot { it.war?.isOfficial.isTrue })
                            filters.contains(WarFilterType.PLAY) -> filteredWars.removeAll(filteredWars.filterNot { it.hasPlayer(authenticationRepository.user?.uid) })
                        }
                        when (_sharedSortTypeSelected.value) {
                            WarSortType.DATE -> filteredWars.sortByDescending { it.war?.mid }
                            WarSortType.SCORE -> filteredWars.sortByDescending { it.scoreHost }
                            WarSortType.TEAM -> filteredWars.sortBy { it.name }
                        }
                    }
                }
                _sharedWars.emit(filteredWars.filter { it.isOver })
            }.launchIn(viewModelScope)

        onSortClick
            .onEach {
                val sortedWars = when (it) {
                    WarSortType.DATE -> wars.sortedByDescending { it.war?.mid }
                    WarSortType.TEAM -> wars.sortedBy { it.name }
                    WarSortType.SCORE -> wars.sortedByDescending { it.scoreHost }
                }.toMutableList()
                when {
                    filters.contains(WarFilterType.WEEK) -> sortedWars.removeAll(wars.filterNot { it.isThisWeek })
                    filters.contains(WarFilterType.OFFICIAL) -> sortedWars.removeAll(wars.filterNot { it.war?.isOfficial.isTrue })
                    filters.contains(WarFilterType.PLAY) -> sortedWars.removeAll(wars.filterNot { it.hasPlayer(authenticationRepository.user?.uid) })
                }
                _sharedWars.emit(sortedWars.filter { it.isOver })
                _sharedSortTypeSelected.emit(it)
            }.launchIn(viewModelScope)

        onFilterClick
            .onEach {
                val filteredWars = mutableListOf<MKWar>()
                when (filters.contains(it)) {
                    true -> filters.remove(it)
                    else -> filters.add(it)
                }
                when {
                    filters.contains(WarFilterType.WEEK) -> filteredWars.addAll(wars.filter { it.isThisWeek })
                    filters.contains(WarFilterType.OFFICIAL) -> filteredWars.addAll(wars.filter { it.war?.isOfficial.isTrue })
                    filters.contains(WarFilterType.PLAY) -> filteredWars.addAll(wars.filter { it.hasPlayer(authenticationRepository.user?.uid) })
                    !filters.contains(WarFilterType.WEEK) -> filteredWars.addAll(wars.filterNot { it.isThisWeek })
                    !filters.contains(WarFilterType.OFFICIAL) -> filteredWars.addAll(wars.filterNot { it.war?.isOfficial.isTrue })
                    !filters.contains(WarFilterType.PLAY) -> filteredWars.addAll(wars.filterNot { it.hasPlayer(authenticationRepository.user?.uid) })
                }
                when (_sharedSortTypeSelected.value) {
                    WarSortType.DATE -> filteredWars.sortByDescending { it.war?.mid }
                    WarSortType.TEAM -> filteredWars.sortBy { it.name }
                    WarSortType.SCORE -> filteredWars.sortByDescending { it.scoreHost }
                }
                _sharedWars.emit(filteredWars.filter { it.isOver })
                _sharedFilterList.emit(filters)
            }.launchIn(viewModelScope)

        onItemClick.bind(_sharedWarClick, viewModelScope)

    }

}