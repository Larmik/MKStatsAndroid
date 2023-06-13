package fr.harmoniamk.statsmk.fragment.stats.indivStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withFullStats
import fr.harmoniamk.statsmk.extension.withFullTeamStats
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.Stats
import fr.harmoniamk.statsmk.model.local.TrackStats
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class IndivStatsViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    private val _sharedTrackClick = MutableSharedFlow<Pair<String?, Int>>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedTeamClick = MutableSharedFlow<Pair<String?, OpponentRankingItemViewModel>>()
    private val _sharedStats = MutableSharedFlow<Stats>()

    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedTeamClick = _sharedTeamClick.asSharedFlow()
    val sharedStats = _sharedStats.asSharedFlow()

    private var bestMap: TrackStats? = null
    private var worstMap: TrackStats? = null
    private var mostPlayedMap: TrackStats? = null
    private var highestVicory: MKWar? = null
    private var loudestDefeat: MKWar? = null
    private var highestScore: MKWar? = null
    private var lowestScore: MKWar? = null
    private var mostPlayedTeam: OpponentRankingItemViewModel? = null
    private var mostDefeatedTeam: OpponentRankingItemViewModel? = null
    private var lessDefeatedTeam: OpponentRankingItemViewModel? = null

    fun bind(
        onBestClick: Flow<Unit>,
        onWorstClick: Flow<Unit>,
        onMostPlayedClick: Flow<Unit>,
        onVictoryClick: Flow<Unit>,
        onDefeatClick: Flow<Unit>,
        onHighestScore: Flow<Unit>,
        onLowestScore: Flow<Unit>,
        onMostPlayedTeamClick: Flow<Unit>,
        onMostDefeatedTeamClick: Flow<Unit>,
        onLessDefeatedTeamClick: Flow<Unit>
        ) {
            val list = mutableListOf<MKWar>()
             databaseRepository.getWars()
                 .map { it.filter { war -> war.hasPlayer(authenticationRepository.user?.uid)  } }
                 .filterNot { it.isEmpty() }
                 .onEach { list.addAll(it) }
                 .flatMapLatest { it.withFullStats(databaseRepository, authenticationRepository.user?.uid, isIndiv = true) }
                 .onEach { stats ->
                    bestMap = stats.bestPlayerMap
                    worstMap = stats.worstPlayerMap
                    mostPlayedMap = stats.mostPlayedMap
                    highestVicory = stats.warStats.highestVictory
                    loudestDefeat = stats.warStats.loudestDefeat
                     highestScore = stats.highestScore?.war
                     lowestScore = stats.lowestScore?.war
                     val teamStats = listOfNotNull(
                         stats.mostPlayedTeam?.team,
                         stats.mostDefeatedTeam?.team,
                         stats.lessDefeatedTeam?.team
                     ).withFullTeamStats(list, databaseRepository, authenticationRepository.user?.uid, isIndiv = true).first()
                     mostPlayedTeam = teamStats.getOrNull(0)
                     mostDefeatedTeam = teamStats.getOrNull(1)
                     lessDefeatedTeam = teamStats.getOrNull(2)
                    _sharedStats.emit(stats)
                }.launchIn(viewModelScope)

        flowOf(
            onBestClick.mapNotNull { bestMap },
            onWorstClick.mapNotNull { worstMap },
            onMostPlayedClick.mapNotNull { mostPlayedMap },
        ).flattenMerge()
            .map { Maps.values().indexOf(it.map) }
            .map { Pair(authenticationRepository.user?.uid, it) }
            .bind(_sharedTrackClick, viewModelScope)

        flowOf(onVictoryClick.mapNotNull { highestVicory }, onDefeatClick.mapNotNull { loudestDefeat }, onHighestScore.mapNotNull { highestScore }, onLowestScore.mapNotNull { lowestScore })
            .flattenMerge()
            .bind(_sharedWarClick, viewModelScope)
        flowOf(onMostPlayedTeamClick.mapNotNull { mostPlayedTeam }, onMostDefeatedTeamClick.mapNotNull { mostDefeatedTeam }, onLessDefeatedTeamClick.mapNotNull { lessDefeatedTeam })
            .flattenMerge()
            .map { Pair(authenticationRepository.user?.uid, it) }
            .bind(_sharedTeamClick, viewModelScope)
    }

}