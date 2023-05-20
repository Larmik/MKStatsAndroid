package fr.harmoniamk.statsmk.fragment.stats.opponentRanking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.PlayerSortType
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.withFullTeamStats
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class OpponentRankingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel()  {

    private val _sharedTeamList = MutableSharedFlow<List<OpponentRankingItemViewModel>>()
    private val _sharedGoToStats = MutableSharedFlow<Pair<String?, OpponentRankingItemViewModel>>()
    private val _sharedLoading = MutableSharedFlow<Boolean>()
    private val _sharedSortTypeSelected = MutableStateFlow(PlayerSortType.NAME)
    private val _sharedIndivStatsEnabled = MutableStateFlow(true)

    val sharedGoToStats = _sharedGoToStats.asSharedFlow()
    val sharedTeamList = _sharedTeamList.asSharedFlow()
    val sharedSortTypeSelected = _sharedSortTypeSelected.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()
    val sharedIndivStatsEnabled = _sharedIndivStatsEnabled.asStateFlow()

    private val itemsVM = mutableListOf<OpponentRankingItemViewModel>()

    fun bind(list: List<Team>, warList: List<MKWar>, onTeamClick: Flow<OpponentRankingItemViewModel>, onSortClick: Flow<PlayerSortType>, onSearch: Flow<String>,  onIndivStatsSelected: Flow<Boolean>) {
        refresh(list, warList)
        onTeamClick
            .map { Pair(authenticationRepository.user?.uid, it) }
            .bind(_sharedGoToStats, viewModelScope)

        onSortClick
            .onEach {
                when (it) {
                    PlayerSortType.NAME -> itemsVM.sortBy { it.teamName }
                    PlayerSortType.WINRATE -> itemsVM.sortByDescending { (it.stats.warStats.warsWon*100)/it.stats.warStats.warsPlayed}
                    PlayerSortType.TOTAL_WIN -> itemsVM.sortByDescending { it.stats.warStats.warsPlayed }
                    PlayerSortType.AVERAGE -> itemsVM.sortByDescending { it.stats.averagePoints }
                }
                _sharedTeamList.emit(itemsVM)
                _sharedSortTypeSelected.emit(it)
            }.launchIn(viewModelScope)

        onSearch
            .map { searched -> itemsVM.filter { it.teamName?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(
                Locale.ROOT)).isTrue }}
            .bind(_sharedTeamList, viewModelScope)

        onIndivStatsSelected.onEach { indivEnabled ->
            _sharedIndivStatsEnabled.emit(indivEnabled)
            refresh(list, warList)
        }.launchIn(viewModelScope)


    }

    private fun refresh(list: List<Team>, warList: List<MKWar>) {
        flowOf(list)
            .filterNotNull()
            .map { it.sortedBy { it.name }.filterNot { it.mid == preferencesRepository.currentTeam?.mid } }
            .flatMapLatest { it.withFullTeamStats(warList, databaseRepository, authenticationRepository.user?.uid, isIndiv = _sharedIndivStatsEnabled.value) }
            .mapNotNull { it.filter { vm -> (!vm.isIndiv && vm.stats.warStats.list.any { war -> war.hasTeam(preferencesRepository.currentTeam?.mid) }) || vm.isIndiv } }
            .onEach {
                itemsVM.clear()
                itemsVM.addAll(it.filter { vm -> vm.stats.warStats.warsPlayed > 1 })
                when (_sharedSortTypeSelected.value) {
                    PlayerSortType.NAME -> itemsVM.sortBy { it.teamName }
                    PlayerSortType.WINRATE -> itemsVM.sortByDescending { (it.stats.warStats.warsWon*100)/it.stats.warStats.warsPlayed}
                    PlayerSortType.TOTAL_WIN -> itemsVM.sortByDescending { it.stats.warStats.warsPlayed }
                    PlayerSortType.AVERAGE -> itemsVM.sortByDescending { it.stats.averagePoints }
                }
                _sharedLoading.emit(false)
                _sharedTeamList.emit(itemsVM)
            }.launchIn(viewModelScope)
    }



}