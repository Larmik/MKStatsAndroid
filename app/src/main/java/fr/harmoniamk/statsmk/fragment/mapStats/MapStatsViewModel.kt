package fr.harmoniamk.statsmk.fragment.mapStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
@FlowPreview
class MapStatsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    private val _sharedTrackList = MutableSharedFlow<List<Pair<MKWar, MKWarTrack>>>()
    private val _sharedTrackPlayed = MutableSharedFlow<Int>()
    private val _sharedTrackWon = MutableSharedFlow<Int>()
    private val _sharedWinRate = MutableSharedFlow<Int>()
    private val _sharedTeamScore = MutableSharedFlow<Int>()
    private val _sharedPlayerScore = MutableSharedFlow<Int>()
    private val _sharedHighestVictory = MutableSharedFlow<Pair<MKWar, MKWarTrack>?>()
    private val _sharedLoudestDefeat = MutableSharedFlow<Pair<MKWar, MKWarTrack>?>()
    private val _sharedMapClick = MutableSharedFlow<Pair<MKWar, MKWarTrack>>()

    val sharedTrackList = _sharedTrackList.asSharedFlow()
    val sharedTrackPlayed = _sharedTrackPlayed.asSharedFlow()
    val sharedTrackWon = _sharedTrackWon.asSharedFlow()
    val sharedWinRate = _sharedWinRate.asSharedFlow()
    val sharedTeamScore = _sharedTeamScore.asSharedFlow()
    val sharedPlayerScore = _sharedPlayerScore.asSharedFlow()
    val sharedHighestVictory = _sharedHighestVictory.asSharedFlow()
    val sharedLoudestDefeat = _sharedLoudestDefeat.asSharedFlow()
    val sharedMapClick = _sharedMapClick.asSharedFlow()


    fun bind(trackIndex: Int,
             onMapClick: Flow<Pair<MKWar, MKWarTrack>>,
             onVictoryClick: Flow<Unit>,
             onDefeatClick: Flow<Unit>
    ) {
        var list: List<Pair<MKWar, MKWarTrack>>? = null
        flowOf(preferencesRepository.currentTeam?.mid)
            .filterNotNull()
            .flatMapLatest { firebaseRepository.getNewWars() }
            .filter { it.mapNotNull { war -> war.teamHost}.contains(preferencesRepository.currentTeam?.mid)
                        || it.map {war -> war.teamOpponent}.contains(preferencesRepository.currentTeam?.mid) }
            .mapNotNull {
                val finalList = mutableListOf<Pair<MKWar, MKWarTrack>>()
                it.forEach { war ->
                   listOf(MKWar(war)).withName(firebaseRepository).first().singleOrNull()?.let { finalWar ->
                       war.warTracks?.filter { track -> track.trackIndex == trackIndex }?.forEach { track ->
                           finalList.add(Pair(finalWar, MKWarTrack(track)))
                       }
                   }
                }
                finalList
            }
            .filter { it.isNotEmpty() }
            .onEach {
                list = it
                val mapPlayed = it.size
                val mapWon = it.filter { pair -> pair.second.displayedDiff.contains('+')}.size
                val playerScore = it
                    .filter { pair -> pair.first.hasPlayer(preferencesRepository.currentUser?.mid) }
                    .mapNotNull { it.second.track?.warPositions }
                    .map { it.singleOrNull { it.playerId == preferencesRepository.currentUser?.mid } }
                    .mapNotNull { it?.position.positionToPoints() }
                _sharedTrackList.emit(it)
                _sharedTrackPlayed.emit(mapPlayed)
                _sharedTrackWon.emit(mapWon)
                _sharedWinRate.emit((mapWon*100) / mapPlayed)
                _sharedTeamScore.emit(it.map { pair -> pair.second }.map { it.teamScore }.sum() / it.size)
                _sharedPlayerScore.emit(playerScore.takeIf { it.isNotEmpty() }?.let {  (playerScore.sum() / playerScore.size).pointsToPosition() } ?: 0)
                _sharedHighestVictory.emit(it.getVictory())
                _sharedLoudestDefeat.emit(it.getDefeat())

            }
            .launchIn(viewModelScope)

        flowOf(
            onMapClick,
            onVictoryClick.mapNotNull { list?.getVictory() },
            onDefeatClick.mapNotNull { list?.getDefeat() }
        )
            .flattenMerge()
            .bind(_sharedMapClick, viewModelScope)

    }

}