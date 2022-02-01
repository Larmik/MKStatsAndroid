package fr.harmoniamk.statsmk.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.room.model.PlayedTrack
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.PlayedTrackRepositoryInterface
import fr.harmoniamk.statsmk.repository.TournamentRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class EditTrackViewModel@Inject constructor(
    private val playedTrackRepository: PlayedTrackRepositoryInterface,
    private val tournamentRepository: TournamentRepositoryInterface
) : ViewModel() {

    private val _sharedGoToPos = MutableSharedFlow<Unit>()
    private val _sharedGoToTrackEdit = MutableSharedFlow<Unit>()
    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _validateTrack = MutableSharedFlow<Unit>()

    val sharedGoToPos = _sharedGoToPos.asSharedFlow()
    val sharedGoToTrackEdit = _sharedGoToTrackEdit.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val validateTrack = _validateTrack.asSharedFlow()

    fun bind(track: PlayedTrack,
             onTrackAdded: Flow<Int>,
             onPositionAdded: Flow<Int>,
             onTrackClick: Flow<Unit>,
             onPositionClick: Flow<Unit>,
             onBack: Flow<Unit>) {

        onTrackClick.bind(_sharedGoToTrackEdit, viewModelScope)
        onPositionClick.bind(_sharedGoToPos, viewModelScope)
        onBack.bind(_sharedBack, viewModelScope)
        onTrackAdded
            .flatMapLatest { playedTrackRepository.updateTrack(track.mid, it) }
            .flatMapLatest { playedTrackRepository.getById(track.mid) }
            .onEach { _validateTrack.emit(Unit) }
            .launchIn(viewModelScope)
        onPositionAdded
            .flatMapLatest { playedTrackRepository.updatePosition(track.mid, it) }
            .flatMapLatest { playedTrackRepository.getById(track.mid) }
            .onEach { _validateTrack.emit(Unit) }
            .launchIn(viewModelScope)
        onPositionAdded
            .filter { it <= 3 }
            .mapNotNull { track.tmId }
            .onEach { tournamentRepository.incrementTops(it) }
            .launchIn(viewModelScope)
    }

}