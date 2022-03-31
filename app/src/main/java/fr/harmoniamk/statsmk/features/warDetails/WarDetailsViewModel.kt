package fr.harmoniamk.statsmk.features.warDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.model.WarTrack
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.model.MKWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WarDetailsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    private val _sharedWarPlayers = MutableSharedFlow<List<Pair<String?, Int>>>()
    private val _sharedTracks = MutableSharedFlow<List<MKWarTrack>>()
    private val _sharedBestTrack = MutableSharedFlow<MKWarTrack>()
    private val _sharedWorstTrack = MutableSharedFlow<MKWarTrack>()
    private val _sharedTrackClick = MutableSharedFlow<Pair<Int, WarTrack>>()

    val sharedWarPlayers = _sharedWarPlayers.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()
    val sharedBestTrack = _sharedBestTrack.asSharedFlow()
    val sharedWorstTrack = _sharedWorstTrack.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()


    fun bind(warId: String?, onTrackClick: Flow<Pair<Int, WarTrack>>) {
        warId?.let { id ->
            firebaseRepository.getWarTracks()
                .map { it.filter { track -> track.warId == id } }
                .onEach {
                    val positions = mutableListOf<Pair<String?, Int>>()
                    _sharedTracks.emit(it.map {track -> MKWarTrack(track) })
                    _sharedBestTrack.emit(it.sortedByDescending { track -> track.teamScore }.map {track -> MKWarTrack(track) }.first())
                    _sharedWorstTrack.emit(it.sortedBy { track -> track.teamScore }.map {track -> MKWarTrack(track) }.first())
                    it.forEach {
                        val trackPositions = firebaseRepository.getWarPositions().map { list -> list.filter { pos -> pos.warTrackId == it.mid } }.firstOrNull()?.groupBy { pos -> pos.playerId }
                        trackPositions?.entries?.forEach { entry ->
                            positions.add(Pair(entry.key, entry.value.map { pos -> pos.points }.sum()))
                        }
                    }
                    _sharedWarPlayers.emit(positions.groupBy { it.first }.map { Pair(it.key, it.value.map { it.second }.sum()) })
                }.launchIn(viewModelScope)
            onTrackClick.bind(_sharedTrackClick, viewModelScope)
        }
    }

}