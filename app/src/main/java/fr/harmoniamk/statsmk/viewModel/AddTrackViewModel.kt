package fr.harmoniamk.statsmk.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.room.model.PlayedTrack
import fr.harmoniamk.statsmk.repository.PlayedTrackRepositoryInterface
import fr.harmoniamk.statsmk.repository.TournamentRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AddTrackViewModel @Inject constructor(
    private val playedTrackRepository: PlayedTrackRepositoryInterface,
    private val tournamentRepository: TournamentRepositoryInterface
) : ViewModel() {

    private val _sharedGoToPos = MutableSharedFlow<Unit>()
    private val _validateTrack = MutableSharedFlow<Unit>()

    val sharedGoToPos = _sharedGoToPos.asSharedFlow()
    val validateTrack = _validateTrack.asSharedFlow()

    fun bind(tournamentId: Int, onTrackAdded: Flow<Int>, onPositionAdded: Flow<Int>) {

        var chosenTrack = -1

        onTrackAdded
            .onEach {
                chosenTrack = it
                _sharedGoToPos.emit(Unit)
            }
            .launchIn(viewModelScope)

        onPositionAdded
            .map { PlayedTrack(trackIndex = chosenTrack, position = it, tmId = tournamentId) }
            .flatMapLatest { playedTrackRepository.insert(it) }
            .flatMapLatest { tournamentRepository.incrementTrackNumber(tournamentId) }
            .flatMapLatest { tournamentRepository.getbyId(tournamentId) }
            .onEach { _validateTrack.emit(Unit) }
            .launchIn(viewModelScope)

        onPositionAdded
            .filter { it <= 3 }
            .onEach { tournamentRepository.incrementTops(tournamentId) }
            .launchIn(viewModelScope)
    }

}