package fr.harmoniamk.statsmk.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.model.Tournament
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
class DeleteTournamentViewModel @Inject constructor(private val tournamentRepository: TournamentRepositoryInterface, private val playedTrackRepository: PlayedTrackRepositoryInterface) : ViewModel() {

    private val _onTournamentDeleted = MutableSharedFlow<Unit>()
    private val _onDismiss = MutableSharedFlow<Unit>()

    val onTournamentDeleted = _onTournamentDeleted.asSharedFlow()
    val onDismiss = _onDismiss.asSharedFlow()

    fun bind(tm: Tournament, onDelete: Flow<Unit>, onBack: Flow<Unit>) {

        onDelete
            .flatMapLatest { playedTrackRepository.deleteByTmId(tm.mid) }
            .flatMapLatest { tournamentRepository.delete(tm) }
            .onEach { _onTournamentDeleted.emit(Unit) }
            .launchIn(viewModelScope)

        onBack.bind(_onDismiss, viewModelScope)
    }

}