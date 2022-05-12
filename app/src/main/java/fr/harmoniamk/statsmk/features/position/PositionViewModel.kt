package fr.harmoniamk.statsmk.features.position

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.database.model.PlayedTrack
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PlayedTrackRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.repository.TournamentRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class PositionViewModel @Inject constructor(
    private val playedTrackRepository: PlayedTrackRepositoryInterface,
    private val tournamentRepository: TournamentRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface
) : ViewModel() {

    private val _sharedPos = MutableSharedFlow<Int>()
    private val _validateTrack = MutableSharedFlow<Unit>()
    private val _sharedQuit = MutableSharedFlow<Unit>()
    private val _sharedGoToResult = MutableSharedFlow<String?>()
    private val _sharedSelectedPositions = MutableSharedFlow<List<Int>>()
    private val _sharedPlayerLabel = MutableSharedFlow<String?>()

    val validateTrack = _validateTrack.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedGoToResult = _sharedGoToResult.asSharedFlow()
    val sharedSelectedPositions = _sharedSelectedPositions.asSharedFlow()
    val sharedPlayerLabel = _sharedPlayerLabel.asSharedFlow()

    fun bind(
        tournamentId: Int? = null, warTrackId: String? = null, chosenTrack: Int,
        onPos1: Flow<Unit>,
        onPos2: Flow<Unit>,
        onPos3: Flow<Unit>,
        onPos4: Flow<Unit>,
        onPos5: Flow<Unit>,
        onPos6: Flow<Unit>,
        onPos7: Flow<Unit>,
        onPos8: Flow<Unit>,
        onPos9: Flow<Unit>,
        onPos10: Flow<Unit>,
        onPos11: Flow<Unit>,
        onPos12: Flow<Unit>,
        onBack: Flow<Unit>,
    ) {

        onPos1.onEach { _sharedPos.emit(1) }.launchIn(viewModelScope)
        onPos2.onEach { _sharedPos.emit(2) }.launchIn(viewModelScope)
        onPos3.onEach { _sharedPos.emit(3) }.launchIn(viewModelScope)
        onPos4.onEach { _sharedPos.emit(4) }.launchIn(viewModelScope)
        onPos5.onEach { _sharedPos.emit(5) }.launchIn(viewModelScope)
        onPos6.onEach { _sharedPos.emit(6) }.launchIn(viewModelScope)
        onPos7.onEach { _sharedPos.emit(7) }.launchIn(viewModelScope)
        onPos8.onEach { _sharedPos.emit(8) }.launchIn(viewModelScope)
        onPos9.onEach { _sharedPos.emit(9) }.launchIn(viewModelScope)
        onPos10.onEach { _sharedPos.emit(10) }.launchIn(viewModelScope)
        onPos11.onEach { _sharedPos.emit(11) }.launchIn(viewModelScope)
        onPos12.onEach { _sharedPos.emit(12) }.launchIn(viewModelScope)

        tournamentId?.takeIf { it != -1 }?.let { id ->
            _sharedPos
                .map { PlayedTrack(trackIndex = chosenTrack, position = it, tmId = id) }
                .flatMapLatest { playedTrackRepository.insert(it) }
                .flatMapLatest { tournamentRepository.incrementTrackNumber(id) }
                .flatMapLatest { tournamentRepository.getbyId(id) }
                .onEach { _validateTrack.emit(Unit) }
                .launchIn(viewModelScope)

            _sharedPos
                .filter { it <= 3 }
                .onEach { tournamentRepository.incrementTops(id) }
                .launchIn(viewModelScope)
        }

        preferencesRepository.currentWar?.let { war ->
            val back = onBack.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
            val positions = mutableListOf<NewWarPositions>()
            var currentUser: User? = null
            var currentUsers: List<User> = listOf()

            firebaseRepository.getUsers()
                .onEach {
                    currentUsers = it.filter { user -> user.currentWar == war.mid }.sortedBy { it.name }
                    currentUser = currentUsers[0]
                    _sharedPlayerLabel.emit(currentUser?.name)
                }.launchIn(viewModelScope)

                back.filter { positions.isEmpty() }.bind(_sharedQuit, viewModelScope)

                back.filterNot { positions.isEmpty() }
                    .onEach {
                        positions.remove(positions.last())
                        _sharedSelectedPositions.emit(positions.mapNotNull { pos -> pos.position })
                        currentUser = currentUsers[positions.size]
                    }
                    .map { currentUser?.name }
                    .bind(_sharedPlayerLabel, viewModelScope)

                _sharedPos
                    .map { NewWarPositions(mid = System.currentTimeMillis().toString(), position = it, playerId = currentUser?.name) }
                    .onEach {
                        positions.add(it)
                        _sharedSelectedPositions.emit(positions.mapNotNull { pos -> pos.position })
                        if (positions.size == currentUsers.size) {
                            preferencesRepository.currentWarTrack.apply { this?.warPositions = positions }?.let { newTrack ->
                                preferencesRepository.currentWarTrack = newTrack
                                val tracks = mutableListOf<NewWarTrack>()
                                tracks.addAll(preferencesRepository.currentWar?.warTracks?.filterNot { tr -> tr.mid == newTrack.mid }.orEmpty())
                                tracks.add(newTrack)
                                preferencesRepository.currentWar = preferencesRepository.currentWar.apply {
                                    this?.warTracks = tracks
                                }
                                _sharedGoToResult.emit(newTrack.mid)
                            }
                        }
                        else {
                            currentUser = currentUsers[positions.indexOf(it)+1]
                            _sharedPlayerLabel.emit(currentUser?.name)
                        }
                    }.launchIn(viewModelScope)
        }
    }
}