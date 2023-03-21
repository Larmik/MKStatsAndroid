package fr.harmoniamk.statsmk.fragment.stats.playerStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.TrackStats
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class PlayerStatsViewModel @Inject constructor() : ViewModel() {

    private val _sharedTrackClick = MutableSharedFlow<Int>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedStats = MutableSharedFlow<PlayerRankingItemViewModel>()
    private val _sharedGoToDetails = MutableSharedFlow<PlayerRankingItemViewModel>()

    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedStats = _sharedStats.asSharedFlow()
    val sharedGoToDetails = _sharedGoToDetails.asSharedFlow()

    private var item: PlayerRankingItemViewModel? = null

    fun bind(
        userStats: PlayerRankingItemViewModel,
        onBestClick: Flow<Unit>,
        onWorstClick: Flow<Unit>,
        onMostPlayedClick: Flow<Unit>,
        onVictoryClick: Flow<Unit>,
        onDefeatClick: Flow<Unit>,
        onDetailsClick: Flow<Unit>,
        onHighestScore: Flow<Unit>,
        onLowestScore: Flow<Unit>,
    ) {

        flowOf(userStats)
            .onEach { itemVM ->
                delay(500)
                item = itemVM
                _sharedStats.emit(itemVM)
            }.launchIn(viewModelScope)

        flowOf(
            onBestClick.mapNotNull { item?.stats?.averageForMaps?.filter { it.totalPlayed >= 2 }?.maxByOrNull { it.teamScore ?: 0 } },
            onWorstClick.mapNotNull { item?.stats?.averageForMaps?.filter { it.totalPlayed >= 2 }?.minByOrNull { it.teamScore ?: 0 } },
            onMostPlayedClick.mapNotNull { item?.stats?.averageForMaps?.maxByOrNull { it.totalPlayed } },
        ).flattenMerge()
            .map { Maps.values().indexOf(it.map) }
            .bind(_sharedTrackClick, viewModelScope)

        flowOf(onVictoryClick.mapNotNull { item?.stats?.warStats?.highestVictory }, onDefeatClick.mapNotNull { item?.stats?.warStats?.loudestDefeat }, onHighestScore.mapNotNull { item?.stats?.highestScore?.war }, onLowestScore.mapNotNull { item?.stats?.lowestScore?.war })
            .flattenMerge()
            .bind(_sharedWarClick, viewModelScope)

        onDetailsClick
            .mapNotNull { item }
            .bind(_sharedGoToDetails, viewModelScope)
    }
}