package fr.harmoniamk.statsmk.fragment.stats.mapStatsDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.WarFilterType
import fr.harmoniamk.statsmk.enums.WarSortType
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.local.MapDetails
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MapStatsDetailsViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {

    private val _sharedTrackClick = MutableSharedFlow<MapDetails>()
    private val _sharedTracks = MutableSharedFlow<List<MapDetails>>()
    private val _sharedSortTypeSelected = MutableStateFlow(WarSortType.DATE)
    private val _sharedFilterList = MutableSharedFlow<List<WarFilterType>>()

    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedSortTypeSelected = _sharedSortTypeSelected.asStateFlow()
    val sharedFilterList = _sharedFilterList.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()

    private val mapDetails = mutableListOf<MapDetails>()
    private val filters = mutableListOf<WarFilterType>()


    fun bind(details: List<MapDetails>, isIndiv: Boolean, onSortClick: Flow<WarSortType>, onFilterClick: Flow<WarFilterType>, onItemClick: Flow<MapDetails>) {
        mapDetails.addAll(details)
        onSortClick
            .onEach {
                val sortedWars = when (it) {
                    WarSortType.DATE -> mapDetails.sortedByDescending { it.warTrack.track?.mid }
                    WarSortType.TEAM -> mapDetails.sortedBy { it.war.name }
                    WarSortType.SCORE  -> when (isIndiv) {
                        true -> mapDetails.sortedBy { it.position }
                        else -> mapDetails.sortedByDescending { it.warTrack.teamScore }
                    }
                }.toMutableList()
                when {
                    filters.contains(WarFilterType.WEEK) -> sortedWars.removeAll(mapDetails.filterNot { it.war.isThisWeek }
                        .toSet())
                    filters.contains(WarFilterType.OFFICIAL) -> sortedWars.removeAll(mapDetails.filterNot { it.war.war?.isOfficial.isTrue }
                        .toSet())
                    filters.contains(WarFilterType.PLAY) -> sortedWars.removeAll(mapDetails.filterNot { it.war.hasPlayer(authenticationRepository.user?.uid) }
                        .toSet())
                }
                _sharedTracks.emit(sortedWars.filter { it.war.isOver })
                _sharedSortTypeSelected.emit(it)
            }.launchIn(viewModelScope)

        onFilterClick
            .onEach {
                val filteredWars = mutableListOf<MapDetails>()
                when (filters.contains(it)) {
                    true -> filters.remove(it)
                    else -> filters.add(it)
                }
                filteredWars.clear()
                when (filters.size) {
                    3 -> filteredWars.addAll(mapDetails.filter { it.war.war?.isOfficial.isTrue && it.war.isThisWeek && it.war.hasPlayer(authenticationRepository.user?.uid) })
                    2 -> when {
                        filters.contains(WarFilterType.WEEK) && filters.contains(WarFilterType.OFFICIAL) -> filteredWars.addAll(mapDetails.filter { it.war.isThisWeek && it.war.war?.isOfficial.isTrue})
                        filters.contains(WarFilterType.WEEK) && filters.contains(WarFilterType.PLAY) -> filteredWars.addAll(mapDetails.filter { it.war.isThisWeek && it.war.hasPlayer(authenticationRepository.user?.uid) })
                        filters.contains(WarFilterType.PLAY) && filters.contains(WarFilterType.OFFICIAL) -> filteredWars.addAll(mapDetails.filter { it.war.hasPlayer(authenticationRepository.user?.uid) && it.war.war?.isOfficial.isTrue })
                    }
                    1 -> when {
                        filters.contains(WarFilterType.WEEK) -> filteredWars.addAll(mapDetails.filter { it.war.isThisWeek })
                        filters.contains(WarFilterType.OFFICIAL) -> filteredWars.addAll(mapDetails.filter { it.war.war?.isOfficial.isTrue })
                        filters.contains(WarFilterType.PLAY) -> filteredWars.addAll(mapDetails.filter { it.war.hasPlayer(authenticationRepository.user?.uid) })
                    }
                    else -> filteredWars.addAll(mapDetails)
                }
                when (_sharedSortTypeSelected.value) {
                    WarSortType.DATE -> filteredWars.sortByDescending { it.war.war?.mid }
                    WarSortType.TEAM -> filteredWars.sortBy { it.war.name }
                    WarSortType.SCORE -> filteredWars.sortByDescending { it.war.scoreHost }
                }
                _sharedTracks.emit(filteredWars.filter { it.war.isOver })
                _sharedFilterList.emit(filters)
            }.launchIn(viewModelScope)

        onItemClick.bind(_sharedTrackClick, viewModelScope)
    }
}