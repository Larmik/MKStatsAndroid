package fr.harmoniamk.statsmk.fragment.mapRanking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.sortByDefeat
import fr.harmoniamk.statsmk.extension.sortBySize
import fr.harmoniamk.statsmk.extension.sortByVictory
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
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
class MapRankingViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    private val _sharedMostPlayedMaps = MutableSharedFlow<List<Pair<Maps, Pair<Int, Int>>>>()
    private val _sharedMostWonMaps = MutableSharedFlow<List<Pair<Maps, Pair<Int, Int>>>>()
    private val _sharedMostLossMaps = MutableSharedFlow<List<Pair<Maps, Pair<Int, Int>>>>()
    private val _sharedGoToStats = MutableSharedFlow<Int>()
    private val _sharedGoToTrackList = MutableSharedFlow<Unit>()

    val sharedMostPlayedMaps = _sharedMostPlayedMaps.asSharedFlow()
    val sharedMostWonMaps = _sharedMostWonMaps.asSharedFlow()
    val sharedMostLossMaps = _sharedMostLossMaps.asSharedFlow()
    val sharedGoToStats = _sharedGoToStats.asSharedFlow()
    val sharedGoToTrackList = _sharedGoToTrackList.asSharedFlow()

    val temp = mutableListOf<NewWarTrack>()

    fun bind(onTrackClick: Flow<Int>, onAllTrackClick: Flow<Unit>) {
        flowOf(preferencesRepository.currentTeam?.mid)
            .filterNotNull()
            .flatMapLatest { firebaseRepository.getNewWars() }
            .filter { it.mapNotNull { war -> war.teamHost}.contains(preferencesRepository.currentTeam?.mid)
                    || it.map {war -> war.teamOpponent}.contains(preferencesRepository.currentTeam?.mid) }
            .mapNotNull { list -> list.map { MKWar(it) }.filter { it.isOver } }
            .map { list ->
                val allTracksPlayed = mutableListOf<NewWarTrack>()
                list.mapNotNull { it.war?.warTracks }.forEach {
                    allTracksPlayed.addAll(it)
                }
                allTracksPlayed
            }
            .onEach { list ->
                temp.addAll(list)
                _sharedMostPlayedMaps.emit(
                    list.sortBySize()
                        .map {
                            Pair(
                                Maps.values()[it.first ?: -1],
                                Pair(
                                    it.second.size,
                                    (it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100) / it.second.size)
                            )
                        }.subList(0,3))


                _sharedMostWonMaps.emit(
                    list
                        .sortByVictory()
                        .map {
                            Pair(
                                Maps.values()[it.first ?: -1],
                                Pair(
                                    it.second.size,
                                    (it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100) / it.second.size)
                            )
                        }.subList(0,3)
                )
                _sharedMostLossMaps.emit(
                    list
                        .sortByDefeat()
                        .map {
                            Pair(
                                Maps.values()[it.first ?: -1],
                                Pair(
                                    it.second.size,
                                    (it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100) / it.second.size)
                            )
                        }.subList(0,3)
                )
            }
            .launchIn(viewModelScope)

        onTrackClick.bind(_sharedGoToStats, viewModelScope)
        onAllTrackClick.bind(_sharedGoToTrackList, viewModelScope)
    }

}