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

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class WarListViewModel @AssistedInject constructor(
    @Assisted("userId") val userId: String?,
    @Assisted("teamId") val teamId: String?,
    @Assisted("periodic") val periodic: String,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            userId: String?,
            teamId: String?,
            periodic: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(userId, teamId, periodic) as T
            }
        }

        @Composable
        fun viewModel(userId: String?,
                       teamId: String?,
                       periodic: String): WarListViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).warListViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    userId = userId,
                    teamId = teamId,
                    periodic = periodic
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: String?, @Assisted("teamId") teamId: String?, @Assisted("periodic") periodic: String): WarListViewModel
    }

    private val _sharedWars = MutableStateFlow<List<MKWar>>(listOf())
    private val _sharedSortTypeSelected = MutableStateFlow<SortType>(WarSortType.DATE)
    private val _sharedUserId = MutableStateFlow<String?>(null)
    private val _sharedUserName = MutableStateFlow<String?>(null)
    private val _sharedTeamId = MutableStateFlow<String?>(null)
    private val _sharedTeamName = MutableStateFlow<String?>(null)
    private val _sharedPeriodic = MutableStateFlow<String?>(null)
    private val _sharedFilterList = MutableStateFlow<List<FilterType>>(listOf())
    private val _sharedBottomsheetValue = MutableStateFlow<MKBottomSheetState?>(null)

    val sharedWars = _sharedWars.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomsheetValue.asStateFlow()
    val sharedUserName = _sharedUserName.asStateFlow()
    val sharedTeamName = _sharedTeamName.asStateFlow()

    private val wars = mutableListOf<MKWar>()
    private val teams = mutableListOf<MKCTeam>()

    init {
        init(_sharedSortTypeSelected.value, _sharedFilterList.value)
    }

    fun init(sort: SortType, filterType: List<FilterType>) {
        _sharedSortTypeSelected.value = sort
        _sharedFilterList.value = filterType
        _sharedPeriodic.value = periodic
        _sharedUserId.value = userId
        _sharedTeamId.value = teamId
        databaseRepository.getNewTeam(teamId)
            .onEach { _sharedTeamName.value = it?.team_name }
            .launchIn(viewModelScope)
        databaseRepository.getPlayers()
            .onEach { _sharedUserName.value = it.singleOrNull { it.mkcId == userId }?.name }
            .launchIn(viewModelScope)
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
            }.flatMapLatest { createWarList(it) }
            .onEach {
                _sharedWars.value = it
                _sharedFilterList.emit(filterType)
            }.launchIn(viewModelScope)
    }

    fun onSearch(search: String) {
        val filteredWars = mutableListOf<MKWar>()
        when (search.isNotEmpty()) {
            true -> teams
                .filter { it.team_name.lowercase().contains(search.lowercase()) || it.team_tag.lowercase().contains(search.lowercase())}
                .map { it.team_tag }
                .forEach { tag -> filteredWars.addAll(wars.filter { it.name?.lowercase()?.contains(tag.lowercase()).isTrue }) }
            else -> filteredWars.addAll(wars)
        }
        createWarList(filteredWars)
            .onEach { _sharedWars.value = it }
            .launchIn(viewModelScope)
    }

    private fun applyFilters(list: Set<MKWar>, filters: List<FilterType>): List<MKWar> {
        val filtered = mutableListOf<MKWar>()
        filtered.addAll(list)
        if (filters.contains(WarFilterType.WEEK)) filtered.removeAll(list.filterNot { it.isThisWeek })
        if (filters.contains(WarFilterType.OFFICIAL)) filtered.removeAll(list.filterNot { it.war?.isOfficial.isTrue })
        if (filters.contains(WarFilterType.PLAY)) filtered.removeAll(list.filterNot { it.hasPlayer(preferencesRepository.mkcPlayer?.id.toString()) })
        return filtered
    }

    private fun createWarList(list: List<MKWar>) = flow {
        val userId = _sharedUserId.value
        val teamId = _sharedTeamId.value
        val periodic = _sharedPeriodic.value
        val filters = _sharedFilterList.value
        val sort = _sharedSortTypeSelected.value
       val wars =  when  {
            userId != null && teamId != null -> list.filter { war -> war.hasPlayer(userId) && war.hasTeam(teamId)}
            userId != null -> list.filter { war -> war.hasPlayer(userId)}
            teamId != null -> list.filter { war -> war.hasTeam(teamId)}
            periodic == Periodics.Week.name -> list.filter { war -> war.hasTeam(preferencesRepository.mkcTeam, preferencesRepository.rosterOnly) && war.isThisWeek }
            periodic == Periodics.Month.name ->  list.filter { war -> war.hasTeam(preferencesRepository.mkcTeam, preferencesRepository.rosterOnly) && war.isThisMonth }
            else -> list.filter { war -> war.hasTeam(preferencesRepository.mkcTeam, preferencesRepository.rosterOnly)}
        }.sortedByDescending { it.war?.mid }
        val filteredWars = applyFilters(list = wars.toSet(), filters)
        emit(when (sort) {
            WarSortType.DATE -> filteredWars.sortedByDescending { it.war?.mid }
            WarSortType.SCORE -> filteredWars.sortedByDescending { it.scoreHost }
            WarSortType.TEAM -> filteredWars.sortedBy { it.name }
            else -> filteredWars
        })
    }

    fun onClickOptions() {
        _sharedBottomsheetValue.value = MKBottomSheetState.FilterSort(Sort.WarSort(), Filter.WarFilter())
    }

    fun dismissBottomSheet() {
        _sharedBottomsheetValue.value = null
    }

    fun onSorted(sort: SortType) {
        init(sort, _sharedFilterList.value)
    }

    fun onFiltered(filters: List<FilterType>) {
        init(_sharedSortTypeSelected.value, filters)
    }
}