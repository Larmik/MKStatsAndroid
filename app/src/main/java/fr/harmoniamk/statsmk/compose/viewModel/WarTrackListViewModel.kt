package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import fr.harmoniamk.statsmk.compose.ViewModelFactoryProvider
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.enums.FilterType
import fr.harmoniamk.statsmk.enums.SortType
import fr.harmoniamk.statsmk.enums.WarFilterType
import fr.harmoniamk.statsmk.enums.WarSortType
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.MapDetails
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class WarTrackListViewModel @AssistedInject constructor(
    @Assisted("periodic") val periodic: String,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            periodic: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(periodic) as T
            }
        }

        @Composable
        fun viewModel(periodic: String): WarTrackListViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).warTrackListViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    periodic = periodic
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("periodic") periodic: String): WarTrackListViewModel
    }
    var onlyIndiv = preferencesRepository.mkcTeam?.id == null

    private val teams = mutableListOf<MKCTeam>()

    private val _sharedMapStats = MutableStateFlow<List<MapDetails>?>(null)
    private val _sharedTrackIndex = MutableStateFlow(-1)
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
        databaseRepository.getNewTeams()
            .onEach {
                teams.clear()
                teams.addAll(it)
            }
            .flatMapLatest { databaseRepository.getWars() }
            .map { it.filter { war ->
                periodic == Periodics.All.name
                        || (periodic == Periodics.Week.name && war.isThisWeek)
                        || (periodic == Periodics.Month.name && war.isThisMonth)
            } }
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
                .filter { it.team_name.lowercase().contains(search.lowercase()) || it.team_tag.lowercase().contains(search.lowercase()) }
                .map { it.team_tag }
                .forEach { tag -> filteredWars.addAll(wars.filter { it.name?.lowercase()?.contains(tag.lowercase()).isTrue }) }
            else -> filteredWars.addAll(wars)
        }
        createMapDetailsList(filteredWars)
            .onEach { _sharedMapStats.value = it }
            .launchIn(viewModelScope)
    }

    private fun applyFilters(list: Set<MapDetails>, filters: List<FilterType>): List<MapDetails> {
        val filtered = mutableListOf<MapDetails>()
        filtered.addAll(list)
        if (filters.contains(WarFilterType.WEEK)) filtered.removeAll(list.filterNot { it.war.isThisWeek })
        if (filters.contains(WarFilterType.OFFICIAL)) filtered.removeAll(list.filterNot { it.war.war?.isOfficial.isTrue })
        if (filters.contains(WarFilterType.PLAY)) filtered.removeAll(list.filterNot { it.war.hasPlayer(preferencesRepository.mkcPlayer?.id.toString()) })
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
        onlyIndiv = userId != null || preferencesRepository.mkcTeam?.id == null

        when {
            userId != null && teamId != null -> list.filter { war -> war.hasPlayer(userId) && war.hasTeam(teamId) }
            onlyIndiv && teamId != null-> list.filter { war -> war.hasPlayer(preferencesRepository.mkcPlayer?.id.toString()) && war.hasTeam(teamId)}
            onlyIndiv -> list.filter { war -> war.hasPlayer(userId ?: preferencesRepository.mkcPlayer?.id.toString()) }
            else -> list.filter { war -> (teamId != null && war.hasTeam(teamId)) || war.hasTeam(preferencesRepository.mkcTeam, preferencesRepository.rosterOnly) }
        }
        .filter { (onlyIndiv && it.hasPlayer(userId)) || !onlyIndiv && it.hasTeam(preferencesRepository.mkcTeam, preferencesRepository.rosterOnly) }
        .forEach { mkWar ->
            mkWar.warTracks?.filter { track -> track.index == trackIndex }?.forEach { track ->
                val position = track.track?.warPositions?.singleOrNull { it.playerId == userId }?.position?.takeIf { userId != null }
                finalList.add(MapDetails(mkWar, MKWarTrack(track.track), position))
            }
        }
        mapDetailsList.addAll(
            finalList
                .filter { (onlyIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer(userId) }.isTrue) || !onlyIndiv }
                .filter { teamId == null || it.war.hasTeam(teamId) }
                .sortedByDescending { it.warTrack.track?.mid }
        )
        val filteredMaps = applyFilters(list = mapDetailsList.toSet(), filters)
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