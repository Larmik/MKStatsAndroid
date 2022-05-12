package fr.harmoniamk.statsmk.fragment.currentTournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.local.MKTournamentTrack
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
class CurrentTournamentViewModel @Inject constructor(
    private val playedTrackRepository: PlayedTrackRepositoryInterface,
    private val tournamentRepository: TournamentRepositoryInterface
) : ViewModel() {

    private val _sharedAddTrack = MutableSharedFlow<Unit>()
    private val _sharedEditTrack = MutableSharedFlow<MKTournamentTrack>()
    private val _sharedCancel = MutableSharedFlow<Unit>()
    private val _sharedQuit = MutableSharedFlow<Unit>()
    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _sharedScore = MutableSharedFlow<Int>()
    private val _sharedTracks = MutableSharedFlow<List<MKTournamentTrack>>()

    val sharedAddTrack = _sharedAddTrack.asSharedFlow()
    val sharedEditTrack = _sharedEditTrack.asSharedFlow()
    val sharedCancel = _sharedCancel.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()
    val sharedScore = _sharedScore.asSharedFlow()

    fun bind(tmId: Int, onAddTrackClick: Flow<Unit>, onCancel: Flow<Unit>, onTrackEdit: Flow<MKTournamentTrack>) {
        var points = 0
        playedTrackRepository.getByTmId(tmId)
            .map {
                _sharedTracks.emit(it)
                it.mapNotNull { track -> track.points }.forEach { pts -> points += pts }
                points }
            .flatMapLatest { tournamentRepository.update(tmId, it) }
            .onEach { _sharedScore.emit(points) }
            .launchIn(viewModelScope)
        onAddTrackClick.bind(_sharedAddTrack, viewModelScope)
        onTrackEdit.bind(_sharedEditTrack, viewModelScope)
        onCancel.bind(_sharedCancel, viewModelScope)
    }

    fun bindDialog(onQuit: Flow<Unit>, onBack: Flow<Unit>) {
        onQuit.bind(_sharedQuit, viewModelScope)
        onBack.bind(_sharedBack, viewModelScope)
    }
}