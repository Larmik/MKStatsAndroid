package fr.harmoniamk.statsmk.fragment.stats.periodicStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.local.*
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class PeriodicStatsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedTrackClick = MutableSharedFlow<Int>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedWeekStatsEnabled = MutableStateFlow(true)
    private val _sharedStats = MutableSharedFlow<Stats>()

    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedStats = _sharedStats.asSharedFlow()
    val sharedWeekStatsEnabled = _sharedWeekStatsEnabled.asStateFlow()

    private var bestMap: TrackStats? = null
    private var worstMap: TrackStats? = null
    private var mostPlayedMap: TrackStats? = null
    private var highestVicory: MKWar? = null
    private var loudestDefeat: MKWar? = null

    fun bind(
        list: List<MKWar>?,
        onBestClick: Flow<Unit>,
        onWorstClick: Flow<Unit>,
        onMostPlayedClick: Flow<Unit>,
        onVictoryClick: Flow<Unit>,
        onDefeatClick: Flow<Unit>,
        onWeekStatsSelected: Flow<Boolean>
    ) {

        refresh(list, hebdo = true)
        flowOf(
            onBestClick.mapNotNull { bestMap },
            onWorstClick.mapNotNull { worstMap },
            onMostPlayedClick.mapNotNull { mostPlayedMap },
        ).flattenMerge()
            .map { Maps.values().indexOf(it.map) }
            .bind(_sharedTrackClick, viewModelScope)

        flowOf(onVictoryClick.mapNotNull { highestVicory }, onDefeatClick.mapNotNull { loudestDefeat })
            .flattenMerge()
            .bind(_sharedWarClick, viewModelScope)

        onWeekStatsSelected.onEach { weekEnabled ->
            _sharedWeekStatsEnabled.emit(weekEnabled)
            refresh(list, weekEnabled)
        }.launchIn(viewModelScope)
    }

    fun refresh(warList: List<MKWar>?, hebdo: Boolean) {
        flowOf(preferencesRepository.currentTeam?.mid)
            .filterNotNull()
            .mapNotNull { warList }
            .filter { it.mapNotNull { war -> war.war?.teamHost}.contains(preferencesRepository.currentTeam?.mid)
                    || it.map {war -> war.war?.teamOpponent}.contains(preferencesRepository.currentTeam?.mid) }
            .mapNotNull { wars -> wars.filter { it.isOver }.filter {
                when (hebdo) {
                    true -> it.isThisWeek
                    else -> it.isThisMonth
                }
            } }
            .flatMapLatest { it.withName(firebaseRepository) }
            .onEach { list ->
                val maps = mutableListOf<TrackStats>()
                val warScores = mutableListOf<WarScore>()
                val averageForMaps = mutableListOf<TrackStats>()
                //Hardcoded ID is Japs random teams
                val mostPlayedTeamId = warsWithOpponentName(list.filterNot { it.war?.teamOpponent == "1652270659565" })
                val mostDefeatedTeamId = warsWithOpponentName(list.filterNot { it.war?.teamOpponent == "1652270659565" }.filterNot { it.displayedDiff.contains('-') })
                val lessDefeatedTeamId = warsWithOpponentName(list.filterNot { it.war?.teamOpponent == "1652270659565" }.filter { it.displayedDiff.contains('-') })

                val mostPlayedTeamData = firebaseRepository.getTeam(mostPlayedTeamId?.first ?: "")
                    .mapNotNull { TeamStats(it?.name, mostPlayedTeamId?.second?.size) }
                    .firstOrNull()
                val mostDefeatedTeamData = firebaseRepository.getTeam(mostDefeatedTeamId?.first ?: "")
                    .mapNotNull { TeamStats(it?.name, mostDefeatedTeamId?.second?.size) }
                    .firstOrNull()
                val lessDefeatedTeamData = firebaseRepository.getTeam(lessDefeatedTeamId?.first ?: "")
                    .mapNotNull { TeamStats(it?.name, lessDefeatedTeamId?.second?.size) }
                    .firstOrNull()

                list.map { Pair(it, it.war?.warTracks?.map { MKWarTrack(it) }) }
                    .forEach {
                        var currentPoints = 0
                        it.second?.forEach { track ->
                            var scoreForTrack = 0
                            track.track?.warPositions?.map { it.position.positionToPoints() }?.forEach {
                                scoreForTrack += it
                            }
                            currentPoints += scoreForTrack
                            maps.add(TrackStats(trackIndex = track.track?.trackIndex, score = scoreForTrack))
                        }
                        warScores.add(WarScore(it.first, currentPoints))
                        currentPoints = 0
                    }

                maps.groupBy { it.trackIndex }
                    .filter { it.value.isNotEmpty() }
                    .forEach { entry ->
                        averageForMaps.add(
                            TrackStats(
                                map = Maps.values()[entry.key ?: -1],
                                score = (entry.value.map { it.score }.sum() / entry.value.map { it.score }.count()),
                                totalPlayed = entry.value.size
                            )
                        )
                    }

                bestMap = averageForMaps.filter { it.totalPlayed >= 2 }.maxByOrNull { it.score ?: 0}
                worstMap = averageForMaps.filter { it.totalPlayed >= 2 }.minByOrNull { it.score ?: 0}
                mostPlayedMap = averageForMaps.maxByOrNull { it.totalPlayed }
                highestVicory = list.maxByOrNull { war -> war.scoreHost }
                loudestDefeat = list.minByOrNull { war -> war.scoreHost }

                val newStats = Stats(
                    warStats = WarStats(list),
                    warScores = warScores,
                    maps = maps,
                    averageForMaps = averageForMaps,
                    mostPlayedTeam = mostPlayedTeamData,
                    mostDefeatedTeam = mostDefeatedTeamData,
                    lessDefeatedTeam = lessDefeatedTeamData
                )
                _sharedStats.emit(newStats)
            }
            .launchIn(viewModelScope)
    }

    private fun warsWithOpponentName(list: List<MKWar>): Pair<String?, List<MKWar>>? = list
        .groupBy { it.war?.teamOpponent }
        .toList()
        .maxByOrNull { it.second.size }
}