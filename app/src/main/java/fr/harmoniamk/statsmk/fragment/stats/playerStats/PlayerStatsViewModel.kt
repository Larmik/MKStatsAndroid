package fr.harmoniamk.statsmk.fragment.stats.playerStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withFullTeamStats
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class PlayerStatsViewModel @Inject constructor(private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedTrackClick = MutableSharedFlow<Pair<String?, Int>>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedStats = MutableSharedFlow<PlayerRankingItemViewModel>()
    private val _sharedGoToDetails = MutableSharedFlow<PlayerRankingItemViewModel>()
    private val _sharedTeamClick = MutableSharedFlow<Pair<String?, OpponentRankingItemViewModel>>()

    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedStats = _sharedStats.asSharedFlow()
    val sharedGoToDetails = _sharedGoToDetails.asSharedFlow()
    val sharedTeamClick = _sharedTeamClick.asSharedFlow()

    private var item: PlayerRankingItemViewModel? = null

    private var mostPlayedTeam: OpponentRankingItemViewModel? = null
    private var mostDefeatedTeam: OpponentRankingItemViewModel? = null
    private var lessDefeatedTeam: OpponentRankingItemViewModel? = null

    fun bind(
        userStats: PlayerRankingItemViewModel,
        warList: List<MKWar>,
        onBestClick: Flow<Unit>,
        onWorstClick: Flow<Unit>,
        onMostPlayedClick: Flow<Unit>,
        onVictoryClick: Flow<Unit>,
        onDefeatClick: Flow<Unit>,
        onDetailsClick: Flow<Unit>,
        onHighestScore: Flow<Unit>,
        onLowestScore: Flow<Unit>,
        onMostPlayedTeamClick: Flow<Unit>,
        onMostDefeatedTeamClick: Flow<Unit>,
        onLessDefeatedTeamClick: Flow<Unit>
    ) {

        flowOf(userStats)
            .onEach { itemVM ->
                delay(500)
                item = itemVM
                val teamStats = listOfNotNull(
                    itemVM.stats.mostPlayedTeam?.team,
                    itemVM.stats.mostDefeatedTeam?.team,
                    itemVM.stats.lessDefeatedTeam?.team
                ).withFullTeamStats(warList, databaseRepository, itemVM.user.mid, isIndiv = true).first()
                mostPlayedTeam = teamStats.getOrNull(0)
                mostDefeatedTeam = teamStats.getOrNull(1)
                lessDefeatedTeam = teamStats.getOrNull(2)
                _sharedStats.emit(itemVM)
            }.launchIn(viewModelScope)

        flowOf(
            onBestClick.mapNotNull { item?.stats?.bestPlayerMap },
            onWorstClick.mapNotNull { item?.stats?.worstPlayerMap },
            onMostPlayedClick.mapNotNull { item?.stats?.mostPlayedMap },
        ).flattenMerge()
            .map { Maps.values().indexOf(it.map) }
            .map { Pair(item?.user?.mid, it ) }
            .bind(_sharedTrackClick, viewModelScope)

        flowOf(onVictoryClick.mapNotNull { item?.stats?.warStats?.highestVictory }, onDefeatClick.mapNotNull { item?.stats?.warStats?.loudestDefeat }, onHighestScore.mapNotNull { item?.stats?.highestScore?.war }, onLowestScore.mapNotNull { item?.stats?.lowestScore?.war })
            .flattenMerge()
            .bind(_sharedWarClick, viewModelScope)

        onDetailsClick
            .mapNotNull { item }
            .bind(_sharedGoToDetails, viewModelScope)

        flowOf(onMostPlayedTeamClick.mapNotNull { mostPlayedTeam }, onMostDefeatedTeamClick.mapNotNull { mostDefeatedTeam }, onLessDefeatedTeamClick.mapNotNull { lessDefeatedTeam })
            .flattenMerge()
            .map { Pair(item?.user?.mid, it) }
            .bind(_sharedTeamClick, viewModelScope)
    }
}