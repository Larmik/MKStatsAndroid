package fr.harmoniamk.statsmk.fragment.indivStats

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
class IndivStatsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedWarsPlayed = MutableSharedFlow<Int?>()
    private val _sharedWarsWon = MutableSharedFlow<Int?>()
    private val _sharedWinRate = MutableSharedFlow<Int?>()
    private val _sharedAveragePoints = MutableSharedFlow<Int?>()
    private val _sharedHighestScore = MutableSharedFlow<Int?>()
    private val _sharedLowestScore = MutableSharedFlow<Int?>()
    private val _sharedBestMap = MutableSharedFlow<Pair<Maps, Int>?>()
    private val _sharedWorstMap = MutableSharedFlow<Pair<Maps, Int>?>()
    private val _sharedHighestVictory = MutableSharedFlow<MKWar?>()
    private val _sharedHighestDefeat = MutableSharedFlow<MKWar?>()

    val sharedWarsPlayed = _sharedWarsPlayed.asSharedFlow()
    val sharedWarsWon = _sharedWarsWon.asSharedFlow()
    val sharedWinRate = _sharedWinRate.asSharedFlow()
    val sharedAveragePoints = _sharedAveragePoints.asSharedFlow()
    val sharedHighestScore = _sharedHighestScore.asSharedFlow()
    val sharedLowestScore = _sharedLowestScore.asSharedFlow()
    val sharedBestMap = _sharedBestMap.asSharedFlow()
    val sharedWorstMap = _sharedWorstMap.asSharedFlow()
    val sharedHighestVictory = _sharedHighestVictory.asSharedFlow()
    val sharedHighestDefeat = _sharedHighestDefeat.asSharedFlow()

    fun bind() {

        flowOf(preferencesRepository.currentTeam?.mid)
            .filterNotNull()
            .flatMapLatest { firebaseRepository.getNewWars() }
            .filter {
                it.mapNotNull { war -> war.teamHost}.contains(preferencesRepository.currentTeam?.mid)
                || it.map {war -> war.teamOpponent}.contains(preferencesRepository.currentTeam?.mid)
            }.mapNotNull { list -> list
                .map { MKWar(it) }
                .filter { it.hasPlayer(preferencesRepository.currentUser?.name) }
            }.onEach { list ->
                val warsPlayed = list.count()
                val warsWon = list.filterNot { war -> war.displayedDiff.contains('-') }.count()
                val maps = mutableListOf<Pair<Int?, Int?>>()
                val warScores = mutableListOf<Int>()
                val averageForMaps = mutableListOf<Pair<Maps, Int>>()

                list.mapNotNull { mkWar -> mkWar.war?.warTracks?.map { MKWarTrack(it) } }.forEach {
                    var currentPoints = 0
                    it.forEach { track ->
                        val scoreForTrack = track.track?.warPositions
                            ?.singleOrNull { pos -> pos.playerId == preferencesRepository.currentUser?.name }
                            ?.position.positionToPoints()
                        currentPoints += scoreForTrack
                        maps.add(Pair(track.track?.trackIndex, scoreForTrack))
                    }
                    warScores += currentPoints
                    currentPoints = 0
                }
                maps.groupBy { it.first }.forEach { entry ->
                    averageForMaps.add(
                        Pair(
                            Maps.values()[entry.key ?: -1],
                            (entry.value.map { it.second }.sum() / entry.value.map { it.second }.count())
                        )
                    )
                }
                _sharedWarsPlayed.emit(warsPlayed)
                _sharedWarsWon.emit(warsWon)
                _sharedWinRate.emit((warsWon*100) / warsPlayed)
                _sharedAveragePoints.emit(warScores.sum() / warScores.count())
                _sharedHighestScore.emit(warScores.maxByOrNull { it })
                _sharedLowestScore.emit(warScores.minByOrNull { it })
                _sharedBestMap.emit(averageForMaps.maxByOrNull { it.second })
                _sharedWorstMap.emit(averageForMaps.minByOrNull { it.second })
                _sharedHighestVictory.emit(list.maxByOrNull { war -> war.scoreHost })
                _sharedHighestDefeat.emit(list.minByOrNull { war -> war.scoreHost })
            }.launchIn(viewModelScope)
    }

}