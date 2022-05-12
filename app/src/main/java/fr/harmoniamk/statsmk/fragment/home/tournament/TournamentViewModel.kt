package fr.harmoniamk.statsmk.fragment.home.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.local.MKTournament
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
class TournamentViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepositoryInterface,
    private val playedTrackRepository: PlayedTrackRepositoryInterface
) : ViewModel() {

    private val _currentTournament = MutableSharedFlow<MKTournament?>()
    private val _sharedLastTournaments = MutableSharedFlow<List<MKTournament>>()
    private val _sharedBestTournaments = MutableSharedFlow<List<MKTournament>>()
    private val _sharedAdd = MutableSharedFlow<Unit>()
    private val _sharedRemainingTracks = MutableSharedFlow<Int>()
    private val _sharedGoToTM = MutableSharedFlow<MKTournament>()

    val sharedLastTournaments = _sharedLastTournaments.asSharedFlow()
    val sharedBestTournaments = _sharedBestTournaments.asSharedFlow()
    val currentTournament = _currentTournament.asSharedFlow()
    val sharedAdd = _sharedAdd.asSharedFlow()
    val sharedRemainingTracks = _sharedRemainingTracks.asSharedFlow()
    val sharedGoToTM = _sharedGoToTM.asSharedFlow()

    fun bind(addTournamentClick: Flow<Unit>, onClickMKTournament: Flow<MKTournament>) {

        val current =
            tournamentRepository.getCurrent().shareIn(viewModelScope, SharingStarted.Eagerly)
        current.bind(_currentTournament, viewModelScope)
        val tournaments = tournamentRepository.getAll()
            .mapNotNull { it.filter { tm -> tm.isOver } }
            .shareIn(viewModelScope, SharingStarted.Eagerly)

        tournaments.map { list -> list.sortedBy { it.createdDate } }
            .bind(_sharedLastTournaments, viewModelScope)
        tournaments.filter { it.size >= 3 }
            .mapNotNull { list -> list.sortedWith(compareBy<MKTournament> { it.ratio }.thenBy { it.points }).reversed().subList(0, 3) }
            .bind(_sharedBestTournaments, viewModelScope)
        addTournamentClick.bind(_sharedAdd, viewModelScope)

        current
            .filterNotNull()
            .flatMapLatest { playedTrackRepository.getByTmId(it.mid) }
            .map { it.size }
            .bind(_sharedRemainingTracks, viewModelScope)

        onClickMKTournament.bind(_sharedGoToTM, viewModelScope)
    }

}
