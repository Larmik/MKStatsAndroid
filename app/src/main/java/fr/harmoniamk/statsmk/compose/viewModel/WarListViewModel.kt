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
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class WarListViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    private val _sharedWars = MutableStateFlow<List<MKWar>>(listOf())
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedSortTypeSelected = MutableStateFlow<SortType>(WarSortType.DATE)
    private val _sharedUserId = MutableStateFlow<String?>(null)
    private val _sharedTeamId = MutableStateFlow<String?>(null)
    private val _sharedFilterList = MutableStateFlow<List<FilterType>>(listOf())
    private val _sharedBottomsheetValue = MutableStateFlow<MKBottomSheetState?>(null)

    val sharedWars = _sharedWars.asStateFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedBottomSheetValue = _sharedBottomsheetValue.asStateFlow()

    fun init(userId: String? = null, teamId: String? = null, sort: SortType, filterType: List<FilterType>) {
        _sharedSortTypeSelected.value = sort
        _sharedFilterList.value = filterType
        _sharedUserId.value = userId
        _sharedTeamId.value = teamId
        databaseRepository.getWars()
            .mapNotNull { list ->
                when  {
                    userId != null && teamId != null -> list.filter { war -> war.hasPlayer(userId) && war.hasTeam(teamId)}
                    userId != null -> list.filter { war -> war.hasPlayer(userId)}
                    teamId != null -> list.filter { war -> war.hasTeam(teamId)}
                    else -> list.filter { war -> war.war?.teamHost == preferencesRepository.currentTeam?.mid}
                }.sortedByDescending { it.war?.mid } }
            .onEach {
                val filteredWars = applyFilters(it, filterType)
                _sharedWars.emit(when (sort) {
                    WarSortType.DATE -> filteredWars.sortedByDescending { it.war?.mid }
                    WarSortType.SCORE -> filteredWars.sortedByDescending { it.scoreHost }
                    WarSortType.TEAM -> filteredWars.sortedBy { it.name }
                    else -> filteredWars
                })
                _sharedFilterList.emit(filterType)
            }.launchIn(viewModelScope)
    }

    private fun applyFilters(list: List<MKWar>, filters: List<FilterType>): List<MKWar> {
        val filtered = mutableListOf<MKWar>()
        filtered.addAll(list)
        if (filters.contains(WarFilterType.WEEK)) filtered.removeAll(list.filterNot { it.isThisWeek })
        if (filters.contains(WarFilterType.OFFICIAL)) filtered.removeAll(list.filterNot { it.war?.isOfficial.isTrue })
        if (filters.contains(WarFilterType.PLAY)) filtered.removeAll(list.filterNot { it.hasPlayer(authenticationRepository.user?.uid) })
        return filtered
    }

    fun onClickOptions() {
        _sharedBottomsheetValue.value = MKBottomSheetState.FilterSort(Sort.WarSort(), Filter.WarFilter())
    }

    fun dismissBottomSheet() {
        _sharedBottomsheetValue.value = null
    }

    fun onSorted(sort: SortType) {
        init(_sharedUserId.value, _sharedTeamId.value, sort, _sharedFilterList.value)
    }

    fun onFiltered(filters: List<FilterType>) {
        init(_sharedUserId.value, _sharedTeamId.value, _sharedSortTypeSelected.value, filters)
    }
}