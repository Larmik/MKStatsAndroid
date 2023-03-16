package fr.harmoniamk.statsmk.fragment.stats.opponentRanking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.enums.PlayerSortType
import fr.harmoniamk.statsmk.enums.TrackSortType
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.extension.withFullTeamStats
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.TrackStats
import fr.harmoniamk.statsmk.model.local.WarStats
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class OpponentRankingViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface
) : ViewModel()  {

    private val _sharedTeamList = MutableSharedFlow<List<OpponentRankingItemViewModel>>()
    private val _sharedGoToStats = MutableSharedFlow<OpponentRankingItemViewModel>()
    private val _sharedLoading = MutableSharedFlow<Boolean>()
    private val _sharedSortTypeSelected = MutableStateFlow(PlayerSortType.NAME)
    private val _sharedIndivStatsEnabled = MutableStateFlow(true)

    val sharedGoToStats = _sharedGoToStats.asSharedFlow()
    val sharedTeamList = _sharedTeamList.asSharedFlow()
    val sharedSortTypeSelected = _sharedSortTypeSelected.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()
    val sharedIndivStatsEnabled = _sharedIndivStatsEnabled.asStateFlow()

    private val itemsVM = mutableListOf<OpponentRankingItemViewModel>()


    fun bind(list: List<Team>, onTeamClick: Flow<OpponentRankingItemViewModel>, onSortClick: Flow<PlayerSortType>, onSearch: Flow<String>,  onIndivStatsSelected: Flow<Boolean>) {
        flowOf(true).bind(_sharedLoading, viewModelScope)
        flowOf(list)
            .map { it.sortedBy { it.name }.filterNot { it.mid == preferencesRepository.currentTeam?.mid } }
            .flatMapLatest { it.withFullTeamStats(firebaseRepository, authenticationRepository.takeIf { _sharedIndivStatsEnabled.value }?.user?.uid) }
            .onEach {
                itemsVM.clear()
                itemsVM.addAll(it)
                when (_sharedSortTypeSelected.value) {
                    PlayerSortType.NAME -> itemsVM.sortBy { it.teamName }
                    PlayerSortType.WINRATE -> itemsVM.sortByDescending { (it.stats.warStats.warsWon*100)/it.stats.warStats.warsPlayed}
                    PlayerSortType.TOTAL_WIN -> itemsVM.sortByDescending { it.stats.warStats.warsPlayed }
                    PlayerSortType.AVERAGE -> itemsVM.sortByDescending { it.stats.averagePoints }
                }
                _sharedLoading.emit(false)
                _sharedTeamList.emit(itemsVM)
            }.launchIn(viewModelScope)

        onTeamClick.bind(_sharedGoToStats, viewModelScope)

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
            _sharedLoading.emit(true)
            _sharedIndivStatsEnabled.emit(indivEnabled)
            itemsVM.clear()
            itemsVM.addAll(list.sortedBy { it.name }.filterNot { it.mid == preferencesRepository.currentTeam?.mid }.withFullTeamStats(firebaseRepository, authenticationRepository.takeIf { indivEnabled }?.user?.uid).first())
            when (_sharedSortTypeSelected.value) {
                PlayerSortType.NAME -> itemsVM.sortBy { it.teamName }
                PlayerSortType.WINRATE -> itemsVM.sortByDescending { (it.stats.warStats.warsWon*100)/it.stats.warStats.warsPlayed}
                PlayerSortType.TOTAL_WIN -> itemsVM.sortByDescending { it.stats.warStats.warsPlayed }
                PlayerSortType.AVERAGE -> itemsVM.sortByDescending { it.stats.averagePoints }
            }
            _sharedTeamList.emit(itemsVM)
            _sharedLoading.emit(false)
        }.launchIn(viewModelScope)


    }



}