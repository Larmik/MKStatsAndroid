package fr.harmoniamk.statsmk.features.position

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.WarPosition
import fr.harmoniamk.statsmk.database.room.model.PlayedTrack
import fr.harmoniamk.statsmk.extension.bind
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
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface
) : ViewModel() {

    private val _sharedPos = MutableSharedFlow<Int>()
    private val _validateTrack = MutableSharedFlow<Unit>()
    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _sharedQuit = MutableSharedFlow<Unit>()
    private val _sharedCancel = MutableSharedFlow<Unit>()
    private val _sharedGoToResult = MutableSharedFlow<String>()
    private val _sharedSelectedPositions = MutableSharedFlow<List<Int>>()

    val validateTrack = _validateTrack.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedCancel = _sharedCancel.asSharedFlow()
    val sharedGoToResult = _sharedGoToResult.asSharedFlow()
    val sharedSelectedPositions = _sharedSelectedPositions.asSharedFlow()

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
        onBackDialog: Flow<Unit>,
        onQuit: Flow<Unit>
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

        tournamentId?.let { id ->
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

        warTrackId?.let { id ->
            onBack.bind(_sharedBack, viewModelScope)

            _sharedPos
                .map { WarPosition(mid = System.currentTimeMillis().toString(), warTrackId = id, position = it, playerId = preferencesRepository.currentUser?.name) }
                .flatMapLatest { firebaseRepository.writeWarPosition(it) }
                .mapNotNull { id }
                .bind(_sharedGoToResult, viewModelScope)

            firebaseRepository.listenToWarPositions()
                .mapNotNull { it.filter { position -> position.warTrackId == id }.mapNotNull { warPosition -> warPosition.position } }
                .bind(_sharedSelectedPositions, viewModelScope)

            onQuit.bind(_sharedQuit, viewModelScope)
        }
        onBackDialog.bind(_sharedCancel, viewModelScope)
    }
}