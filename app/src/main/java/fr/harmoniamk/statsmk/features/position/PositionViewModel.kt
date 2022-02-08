package fr.harmoniamk.statsmk.features.position

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.WarTrack
import fr.harmoniamk.statsmk.database.room.model.PlayedTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PlayedTrackRepositoryInterface
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
    private val firebaseRepository: FirebaseRepositoryInterface
) : ViewModel() {

    private val _sharedPos = MutableSharedFlow<Int>()
    val sharedPos = _sharedPos.asSharedFlow()


    private val _validateTrack = MutableSharedFlow<Unit>()
    val validateTrack = _validateTrack.asSharedFlow()


    fun bind(
        tournamentId: Int? = null, warId: String? = null, chosenTrack: Int,
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
        onPos12: Flow<Unit>
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

    }
}