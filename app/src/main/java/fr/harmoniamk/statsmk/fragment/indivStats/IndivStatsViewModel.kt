package fr.harmoniamk.statsmk.fragment.indivStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.*
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
class IndivStatsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedMostPlayedTeam = MutableSharedFlow<TeamStats?>()
    private val _sharedMostDefeatedTeam = MutableSharedFlow<Pair<String?, Int?>?>()
    private val _sharedLessDefeatedTeam = MutableSharedFlow<Pair<String?, Int?>?>()
    private val _sharedTrackClick = MutableSharedFlow<Int>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedStats = MutableSharedFlow<Stats>()

    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedStats = _sharedStats.asSharedFlow()
    val sharedMostDefeatedTeam = _sharedMostDefeatedTeam.asSharedFlow()
    val sharedLessDefeatedTeam = _sharedLessDefeatedTeam.asSharedFlow()

    private var bestMap: TrackStats? = null
    private var worstMap: TrackStats? = null
    private var mostPlayedMap: TrackStats? = null
    private var highestVicory: MKWar? = null
    private var loudestDefeat: MKWar? = null

    fun bind(
        onBestClick: Flow<Unit>,
        onWorstClick: Flow<Unit>,
        onMostPlayedClick: Flow<Unit>,
        onVictoryClick: Flow<Unit>,
        onDefeatClick: Flow<Unit>) {

        flowOf(preferencesRepository.currentTeam?.mid)
            .filterNotNull()
            .flatMapLatest { firebaseRepository.getNewWars() }
            .filter {
                it.mapNotNull { war -> war.teamHost}.contains(preferencesRepository.currentTeam?.mid)
                || it.map {war -> war.teamOpponent}.contains(preferencesRepository.currentTeam?.mid)
            }.mapNotNull { list -> list
                .map { MKWar(it) }
                .filter { it.hasPlayer(preferencesRepository.currentUser?.mid) }
            }
            .flatMapLatest { it.withName(firebaseRepository) }
            .onEach { list ->
                val maps = mutableListOf<TrackStats>()
                val warScores = mutableListOf<WarScore>()
                val averageForMaps = mutableListOf<TrackStats>()

                val mostPlayedTeamId = warsWithOpponentName(list.filterNot { it.war?.teamOpponent == "1652270659565" })
                val mostDefeatedTeamId = warsWithOpponentName(list.filterNot { it.war?.teamOpponent == "1652270659565" }.filterNot { it.displayedDiff.contains('-') })
                val lessDefeatedTeamId = warsWithOpponentName(list.filterNot { it.war?.teamOpponent == "1652270659565" }.filter { it.displayedDiff.contains('-') })

                val mostPlayedTeamData = firebaseRepository.getTeam(mostPlayedTeamId?.first ?: "")
                    .mapNotNull { TeamStats(it?.name, mostPlayedTeamId?.second?.size) }
                    .firstOrNull()
                val mostDefeatedTeamData = firebaseRepository.getTeam(mostDefeatedTeamId?.first ?: "")
                    .mapNotNull { Pair(it?.name, mostDefeatedTeamId?.second?.size) }
                    .firstOrNull()
                val lessDefeatedTeamData = firebaseRepository.getTeam(lessDefeatedTeamId?.first ?: "")
                    .mapNotNull { Pair(it?.name, lessDefeatedTeamId?.second?.size) }
                    .firstOrNull()

                list.map { Pair(it, it.war?.warTracks?.map { MKWarTrack(it) }) }
                    .forEach {
                        var currentPoints = 0
                        it.second?.forEach { track ->
                            val scoreForTrack = track.track?.warPositions
                                ?.singleOrNull { pos -> pos.playerId == preferencesRepository.currentUser?.mid }
                                ?.position.positionToPoints()
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
                bestMap = averageForMaps.maxByOrNull { it.score }
                worstMap = averageForMaps.minByOrNull { it.score }
                mostPlayedMap = averageForMaps.maxByOrNull { it.totalPlayed }
                highestVicory = list.maxByOrNull { war -> war.scoreHost }
                loudestDefeat = list.minByOrNull { war -> war.scoreHost }

                val newStats = Stats(
                    warStats = WarStats(list),
                    warScores = warScores,
                    maps = maps,
                    averageForMaps = averageForMaps,
                    mostPlayedTeam = mostPlayedTeamData
                )
                _sharedMostDefeatedTeam.emit(mostDefeatedTeamData)
                _sharedLessDefeatedTeam.emit(lessDefeatedTeamData)
                _sharedStats.emit(newStats)
                _sharedMostPlayedTeam.emit(mostPlayedTeamData)
            }.launchIn(viewModelScope)

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
    }

    private fun warsWithOpponentName(list: List<MKWar>): Pair<String?, List<MKWar>>? = list.groupBy { it.war?.teamOpponent }
        .toList()
        .sortedByDescending { it.second.size }
        .firstOrNull()

}