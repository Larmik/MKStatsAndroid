package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.enums.FilterType
import fr.harmoniamk.statsmk.enums.SortType
import fr.harmoniamk.statsmk.enums.WarFilterType
import fr.harmoniamk.statsmk.enums.WarSortType
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.MapDetails
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class WarTrackListViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface
) : ViewModel() {
    var onlyIndiv = preferencesRepository.currentTeam?.mid == null

    private val teams = mutableListOf<Team>()

    private val _sharedMapStats = MutableStateFlow<List<MapDetails>?>(null)
    private val _sharedTrackIndex = MutableStateFlow<Int>(-1)
    private val _sharedUserId = MutableStateFlow<String?>(null)
    private val _sharedTeamId = MutableStateFlow<String?>(null)
    private val _sharedSortTypeSelected = MutableStateFlow<SortType>(WarSortType.DATE)
    private val _sharedFilterList = MutableStateFlow<List<FilterType>>(listOf())
    private val _sharedBottomsheetValue = MutableStateFlow<MKBottomSheetState?>(null)

    val sharedMapStats = _sharedMapStats.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomsheetValue.asStateFlow()

    val wars = mutableListOf<MKWar>()


    fun onClickOptions() {
        _sharedBottomsheetValue.value = MKBottomSheetState.FilterSort(Sort.WarSort(), Filter.WarFilter())
    }

    fun dismissBottomSheet() {
        _sharedBottomsheetValue.value = null
    }

    fun init(trackIndex: Int, teamId: String?, userId: String?,  sort: SortType, filterType: List<FilterType>) {
        _sharedTrackIndex.value = trackIndex
        _sharedTeamId.value = teamId
        _sharedUserId.value = userId
        _sharedSortTypeSelected.value = sort
        _sharedFilterList.value = filterType
        databaseRepository.getTeams()
            .onEach {
                teams.clear()
                teams.addAll(it)
            }
            .flatMapLatest { databaseRepository.getWars() }
            .onEach {
                wars.clear()
                wars.addAll(it)
            }
            .flatMapLatest { createMapDetailsList(it) }
            .onEach {
                _sharedMapStats.value = it
                _sharedFilterList.emit(filterType)
            }
            .launchIn(viewModelScope)
    }

    fun onSearch (search: String) {
        val filteredWars = mutableListOf<MKWar>()
        when (search.isNotEmpty()) {
            true -> teams
                .filter { it.name?.lowercase()?.contains(search.lowercase()).isTrue || it.shortName?.lowercase()?.contains(search.lowercase()).isTrue }
                .mapNotNull { it.shortName }
                .forEach { tag -> filteredWars.addAll(wars.filter { it.name?.lowercase()?.contains(tag.lowercase()).isTrue }) }
            else -> filteredWars.addAll(wars)
        }
        createMapDetailsList(filteredWars)
            .onEach { _sharedMapStats.value = it }
            .launchIn(viewModelScope)
    }

    private fun applyFilters(list: List<MapDetails>, filters: List<FilterType>): List<MapDetails> {
        val filtered = mutableListOf<MapDetails>()
        filtered.addAll(list)
        if (filters.contains(WarFilterType.WEEK)) filtered.removeAll(list.filterNot { it.war.isThisWeek })
        if (filters.contains(WarFilterType.OFFICIAL)) filtered.removeAll(list.filterNot { it.war.war?.isOfficial.isTrue })
        if (filters.contains(WarFilterType.PLAY)) filtered.removeAll(list.filterNot { it.war.hasPlayer(authenticationRepository.user?.uid) })
        return filtered
    }


    private fun createMapDetailsList(list: List<MKWar>) = flow {
        val finalList = mutableListOf<MapDetails>()
        val mapDetailsList = mutableListOf<MapDetails>()
        val userId = _sharedUserId.value
        val teamId = _sharedTeamId.value
        val filters = _sharedFilterList.value
        val sort = _sharedSortTypeSelected.value
        val trackIndex = _sharedTrackIndex.value
        onlyIndiv = userId != null || preferencesRepository.currentTeam?.mid == null

        when {
            userId != null && teamId != null -> list.filter { war -> war.hasPlayer(authenticationRepository.user?.uid) && war.hasTeam(teamId) }
            onlyIndiv -> list.filter { war -> war.hasPlayer(userId ?: authenticationRepository.user?.uid) }
            else -> list.filter { war -> war.hasTeam(teamId ?: preferencesRepository.currentTeam?.mid) }
        }
        .filter { (onlyIndiv && it.hasPlayer(userId)) || !onlyIndiv && it.hasTeam(preferencesRepository.currentTeam?.mid) }
        .forEach { mkWar ->
            mkWar.warTracks?.filter { track -> track.index == trackIndex }?.forEach { track ->
                val position = track.track?.warPositions?.singleOrNull { it.playerId == userId }?.position?.takeIf { userId != null }
                finalList.add(MapDetails(mkWar, MKWarTrack(track.track), position))
            }
        }
        mapDetailsList.addAll(
            finalList
                .filter { !onlyIndiv || (onlyIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer(userId) }.isTrue) }
                .filter { teamId == null || it.war.hasTeam(teamId) }
                .sortedByDescending { it.warTrack.track?.mid }
        )
        val filteredMaps = applyFilters(list = mapDetailsList, filters)
        emit(when (sort) {
            WarSortType.DATE -> filteredMaps.sortedByDescending { it.war.war?.mid }
            WarSortType.SCORE -> filteredMaps.sortedByDescending { it.warTrack.diffScore }
            WarSortType.TEAM -> filteredMaps.sortedBy { it.war.name }
            else -> filteredMaps
        })
    }


    fun onSorted(sort: SortType) {
        init(_sharedTrackIndex.value, _sharedTeamId.value, _sharedUserId.value,  sort, _sharedFilterList.value)
    }

    fun onFiltered(filters: List<FilterType>) {
        init(_sharedTrackIndex.value, _sharedTeamId.value, _sharedUserId.value, _sharedSortTypeSelected.value, filters)
    }
}