package fr.harmoniamk.statsmk.fragment.editWarPositions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withPlayerName
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class EditWarPositionsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    private val _sharedDismiss = MutableSharedFlow<Unit>()
    private val _sharedPlayerLabel = MutableSharedFlow<String?>()
    private val _sharedSelectedPositions = MutableSharedFlow<List<Int>>()
    private val _sharedPos = MutableSharedFlow<Int>()

    val sharedDismiss = _sharedDismiss.asSharedFlow()
    val sharedPlayerLabel = _sharedPlayerLabel.asSharedFlow()
    val sharedSelectedPositions = _sharedSelectedPositions.asSharedFlow()

    fun bind(war: NewWar, index: Int,
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
             onPos12: Flow<Unit>) {

        val warPositions = war.warTracks?.get(index)?.warPositions
        val positions = mutableListOf<NewWarPositions>()
        var currentPlayer = warPositions?.get(positions.size)?.playerId

        val playerLabel = warPositions
            ?.withPlayerName(firebaseRepository)
            ?.map { it[positions.size].player?.name }


        playerLabel?.bind(_sharedPlayerLabel, viewModelScope)
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

        _sharedPos
            .map { NewWarPositions(mid = System.currentTimeMillis().toString(), position = it, playerId = currentPlayer) }
            .onEach {
                positions.add(it)
                _sharedSelectedPositions.emit(positions.mapNotNull { pos -> pos.position })
                if (positions.size == warPositions?.size) {
                    war.warTracks?.get(index)?.let { track ->
                        val newTrackList: MutableList<NewWarTrack>? = war.warTracks?.toMutableList()
                        newTrackList?.remove(track)
                        newTrackList?.add(index, track.apply { this.warPositions = positions })
                        firebaseRepository.writeNewWar(war.apply { this.warTracks = newTrackList }).first()
                        _sharedDismiss.emit(Unit)
                    }
                }
                else {
                    currentPlayer = warPositions?.get(positions.size)?.playerId
                    _sharedPlayerLabel.emit(playerLabel?.firstOrNull())
                }
            }.launchIn(viewModelScope)
    }

}