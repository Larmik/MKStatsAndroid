package fr.harmoniamk.statsmk.fragment.stats.playerRanking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.PlayerSortType
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.withFullStats
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.StorageRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class PlayerRankingViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface
) : ViewModel()  {

    private val _sharedUserList = MutableSharedFlow<List<PlayerRankingItemViewModel>>()
    private val _sharedGoToStats = MutableSharedFlow<PlayerRankingItemViewModel>()
    private val _sharedLoading = MutableSharedFlow<Boolean>()
    private val _sharedSortTypeSelected = MutableStateFlow(PlayerSortType.NAME)

    val sharedGoToStats = _sharedGoToStats.asSharedFlow()
    val sharedUserList = _sharedUserList.asSharedFlow()
    val sharedSortTypeSelected = _sharedSortTypeSelected.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()
    private val itemsVM = mutableListOf<PlayerRankingItemViewModel>()


    fun bind(list: List<User>, onPlayerClick: Flow<PlayerRankingItemViewModel>, onSortClick: Flow<PlayerSortType>, onSearch: Flow<String>) {
        flowOf(true).bind(_sharedLoading, viewModelScope)
        flowOf(list)
            .map { it.sortedBy { it.name } }
            .flatMapLatest { it.withFullStats(firebaseRepository) }
            .onEach {
                itemsVM.clear()
                itemsVM.addAll(it)
                when (_sharedSortTypeSelected.value) {
                    PlayerSortType.NAME -> itemsVM.sortBy { it.user.name }
                    PlayerSortType.WINRATE -> itemsVM.sortByDescending { (it.stats.warStats.warsWon*100)/it.stats.warStats.warsPlayed}
                    PlayerSortType.TOTAL_WIN -> itemsVM.sortByDescending { it.stats.warStats.warsWon }
                    PlayerSortType.AVERAGE -> itemsVM.sortByDescending { it.stats.averagePoints }
                }
                _sharedLoading.emit(false)
                _sharedUserList.emit(itemsVM)
            }.launchIn(viewModelScope)

        onPlayerClick.bind(_sharedGoToStats, viewModelScope)

        onSortClick
            .onEach {
                when (it) {
                    PlayerSortType.NAME -> itemsVM.sortBy { it.user.name }
                    PlayerSortType.WINRATE -> itemsVM.sortByDescending { (it.stats.warStats.warsWon*100)/it.stats.warStats.warsPlayed}
                    PlayerSortType.TOTAL_WIN -> itemsVM.sortByDescending { it.stats.warStats.warsWon }
                    PlayerSortType.AVERAGE -> itemsVM.sortByDescending { it.stats.averagePoints }
                }
                _sharedUserList.emit(itemsVM)
                _sharedSortTypeSelected.emit(it)
            }.launchIn(viewModelScope)

        onSearch
            .map { searched -> itemsVM.filter { it.user.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)).isTrue }}
            .bind(_sharedUserList, viewModelScope)
    }

}