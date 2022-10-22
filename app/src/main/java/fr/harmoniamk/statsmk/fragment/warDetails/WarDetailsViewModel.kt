package fr.harmoniamk.statsmk.fragment.warDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.Penalty
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
class WarDetailsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedWarPlayers = MutableSharedFlow<List<Pair<String?, Int>>>()
    private val _sharedTracks = MutableSharedFlow<List<MKWarTrack>>()
    private val _sharedBestTrack = MutableSharedFlow<MKWarTrack>()
    private val _sharedWorstTrack = MutableSharedFlow<MKWarTrack>()
    private val _sharedTrackClick = MutableSharedFlow<Int>()
    private val _sharedWarDeleted = MutableSharedFlow<Unit>()
    private val _sharedDeleteWarVisible = MutableSharedFlow<Boolean>()
    private val _sharedPlayerHost = MutableSharedFlow<String>()
    private val _sharedWarName = MutableSharedFlow<String?>()
    private val _sharedPenalties = MutableSharedFlow<List<Penalty>?>()

    val sharedWarPlayers = _sharedWarPlayers.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()
    val sharedBestTrack = _sharedBestTrack.asSharedFlow()
    val sharedWorstTrack = _sharedWorstTrack.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedWarDeleted = _sharedWarDeleted.asSharedFlow()
    val sharedDeleteWarVisible = _sharedDeleteWarVisible.asSharedFlow()
    val sharedPlayerHost = _sharedPlayerHost.asSharedFlow()
    val sharedWarName = _sharedWarName.asSharedFlow()
    val sharedPenalties = _sharedPenalties.asSharedFlow()

    fun bind(warId: String?, onTrackClick: Flow<Int>, onDeleteWar: Flow<Unit>) {
        warId?.let { id ->
            firebaseRepository.getNewWar(id)
                .onEach {
                    _sharedPlayerHost.emit("Créée par ${firebaseRepository.getUser(it?.playerHostId ?: "").firstOrNull()?.name ?: ""}")
                    _sharedDeleteWarVisible.emit(preferencesRepository.currentUser?.mid == "1645093376108")
                    _sharedWarName.emit(listOf(MKWar(it)).withName(firebaseRepository).firstOrNull()?.singleOrNull()?.name)
                    it?.penalties?.let { penalty ->
                        _sharedPenalties.emit(penalty.withTeamName(firebaseRepository).firstOrNull())
                    }
                }
                .mapNotNull { it?.warTracks?.map { MKWarTrack(it) } }
                .onEach {
                    val positions = mutableListOf<Pair<String?, Int>>()
                    _sharedTracks.emit(it)
                    _sharedBestTrack.emit(it.sortedByDescending { track -> track.teamScore }.first())
                    _sharedWorstTrack.emit(it.sortedBy { track -> track.teamScore }.first())
                    it.forEach {
                        it.track?.warPositions?.let {
                            val trackPositions = it.withPlayerName(firebaseRepository).firstOrNull()
                            trackPositions?.groupBy { it.player?.name }?.entries?.forEach { entry ->
                                positions.add(Pair(entry.key, entry.value.map { pos -> pos.position.position.positionToPoints() }.sum()))
                            }
                        }
                    }
                    _sharedWarPlayers.emit(positions.groupBy { it.first }.map { Pair(it.key, it.value.map { it.second }.sum()) }.sortedByDescending { it.second })
                }
                .launchIn(viewModelScope)
            onTrackClick.bind(_sharedTrackClick, viewModelScope)
            onDeleteWar
                .flatMapLatest { firebaseRepository.deleteNewWar(id) }
                .bind(_sharedWarDeleted, viewModelScope)
        }
    }

}