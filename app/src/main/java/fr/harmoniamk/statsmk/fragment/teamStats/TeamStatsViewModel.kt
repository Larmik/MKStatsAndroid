package fr.harmoniamk.statsmk.fragment.teamStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class TeamStatsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedWarsPlayed = MutableSharedFlow<Int?>()
    private val _sharedWarsWon = MutableSharedFlow<Int?>()
    private val _sharedWinRate = MutableSharedFlow<Int?>()
    private val _sharedAveragePoints = MutableSharedFlow<Int?>()
    private val _sharedAverageMapPoints = MutableSharedFlow<Int?>()
    //First int of second pair is score, and last is total of played
    private val _sharedBestMap = MutableSharedFlow<Pair<Maps, Pair<Int, Int>>?>()
    private val _sharedMostPlayedMap = MutableSharedFlow<Pair<Maps, Pair<Int, Int>>?>()
    private val _sharedWorstMap = MutableSharedFlow<Pair<Maps, Pair<Int, Int>>?>()
    private val _sharedLessPlayedMap = MutableSharedFlow<Pair<Maps, Pair<Int, Int>>?>()
    private val _sharedHighestVictory = MutableSharedFlow<MKWar?>()
    private val _sharedHighestDefeat = MutableSharedFlow<MKWar?>()
    private val _sharedMostPlayedTeam = MutableSharedFlow<Pair<String?, Int?>?>()
    private val _sharedMostDefeatedTeam = MutableSharedFlow<Pair<String?, Int?>?>()
    private val _sharedLessDefeatedTeam = MutableSharedFlow<Pair<String?, Int?>?>()

    val sharedWarsPlayed = _sharedWarsPlayed.asSharedFlow()
    val sharedWarsWon = _sharedWarsWon.asSharedFlow()
    val sharedWinRate = _sharedWinRate.asSharedFlow()
    val sharedAveragePoints = _sharedAveragePoints.asSharedFlow()
    val sharedAverageMapPoints = _sharedAverageMapPoints.asSharedFlow()
    val sharedBestMap = _sharedBestMap.asSharedFlow()
    val sharedMostPlayedMap = _sharedMostPlayedMap.asSharedFlow()
    val sharedWorstMap = _sharedWorstMap.asSharedFlow()
    val sharedLessPlayedMap = _sharedLessPlayedMap.asSharedFlow()
    val sharedHighestVictory = _sharedHighestVictory.asSharedFlow()
    val sharedHighestDefeat = _sharedHighestDefeat.asSharedFlow()
    val sharedMostPlayedTeam = _sharedMostPlayedTeam.asSharedFlow()
    val sharedMostDefeatedTeam = _sharedMostDefeatedTeam.asSharedFlow()
    val sharedLessDefeatedTeam = _sharedLessDefeatedTeam.asSharedFlow()

    fun bind() {

        flowOf(preferencesRepository.currentTeam?.mid)
            .filterNotNull()
            .flatMapLatest { firebaseRepository.getNewWars() }
            .filter { it.mapNotNull { war -> war.teamHost}.contains(preferencesRepository.currentTeam?.mid)
                        || it.map {war -> war.teamOpponent}.contains(preferencesRepository.currentTeam?.mid) }
            .mapNotNull { list -> list.map { MKWar(it) }.filter { it.isOver } }
            .onEach { list ->
                val warsPlayed = list.count()
                val warsWon = list.filterNot { war -> war.displayedDiff.contains('-') }.count()
                val maps = mutableListOf<Pair<Int?, Int?>>()
                val warScores = mutableListOf<Pair<MKWar?, Int>>()
                val averageForMaps = mutableListOf<Pair<Maps, Pair<Int, Int>>>()
                val mostPlayedTeamId = warsWithOpponentName(list)
                val mostDefeatedTeamId = warsWithOpponentName(list.filterNot { it.displayedDiff.contains('-') })
                val lessDefeatedTeamId = warsWithOpponentName(list.filter { it.displayedDiff.contains('-') })
                val mostPlayedTeamData = firebaseRepository.getTeam(mostPlayedTeamId?.first ?: "")
                    .mapNotNull { Pair(it?.name, mostPlayedTeamId?.second?.size) }
                    .firstOrNull()
                val mostDefeatedTeamData = firebaseRepository.getTeam(mostDefeatedTeamId?.first ?: "")
                    .mapNotNull { Pair(it?.name, mostPlayedTeamId?.second?.size) }
                    .firstOrNull()
                val lessDefeatedTeamData = firebaseRepository.getTeam(lessDefeatedTeamId?.first ?: "")
                    .mapNotNull { Pair(it?.name, mostPlayedTeamId?.second?.size) }
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
                            maps.add(Pair(track.track?.trackIndex, scoreForTrack))
                        }
                        warScores.add(Pair(it.first, currentPoints))
                        currentPoints = 0
                    }

                maps.groupBy { it.first }
                    .filter { it.value.isNotEmpty() }
                    .forEach { entry ->
                        averageForMaps.add(
                            Pair(
                                Maps.values()[entry.key ?: -1],
                                Pair(
                                    (entry.value.map { it.second }.sum() / entry.value.map { it.second }.count()),
                                    entry.value.size
                                )
                            )
                        )
                    }

                _sharedWarsPlayed.emit(warsPlayed)
                _sharedWarsWon.emit(warsWon)
                _sharedWinRate.emit((warsWon*100) / warsPlayed)
                _sharedAveragePoints.emit(warScores.map { it.second }.sum() / warScores.count())
                _sharedAverageMapPoints.emit(maps.map { it.second }.sum() / maps.size)
                _sharedBestMap.emit(averageForMaps.maxByOrNull { it.second.first })
                _sharedWorstMap.emit(averageForMaps.minByOrNull { it.second.first })
                _sharedHighestVictory.emit(list.maxByOrNull { war -> war.scoreHost })
                _sharedHighestDefeat.emit(list.minByOrNull { war -> war.scoreHost })
                _sharedMostPlayedTeam.emit(mostPlayedTeamData)
                _sharedMostDefeatedTeam.emit(mostDefeatedTeamData)
                _sharedLessDefeatedTeam.emit(lessDefeatedTeamData)
                _sharedMostPlayedMap.emit(averageForMaps.maxByOrNull { it.second.second })
                _sharedLessPlayedMap.emit(averageForMaps.minByOrNull { it.second.second })
            }.launchIn(viewModelScope)
    }

    private fun warsWithOpponentName(list: List<MKWar>): Pair<String?, List<MKWar>>? = list
        .groupBy { it.war?.teamOpponent }
        .toList()
        .maxByOrNull { it.second.size }
}