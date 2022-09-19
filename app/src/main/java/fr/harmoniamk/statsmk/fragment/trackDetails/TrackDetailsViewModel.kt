package fr.harmoniamk.statsmk.fragment.trackDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.withPlayerName
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
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
class TrackDetailsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface): ViewModel() {

    private val _sharedPositions = MutableSharedFlow<List<MKWarPosition>>()
    private val _sharedEditTrackClick = MutableSharedFlow<Unit>()
    private val _sharedEditPositionsClick = MutableSharedFlow<Unit>()
    private val _sharedButtonsVisible = MutableSharedFlow<Boolean>()
    private val _sharedTrackRefreshed = MutableSharedFlow<MKWarTrack>()

    val sharedPositions = _sharedPositions.asSharedFlow()
    val sharedEditTrackClick = _sharedEditTrackClick.asSharedFlow()
    val sharedEditPositionsClick = _sharedEditPositionsClick.asSharedFlow()
    val sharedButtonsVisible = _sharedButtonsVisible.asSharedFlow()
    val sharedTrackRefreshed = _sharedTrackRefreshed.asSharedFlow()

    var warId: String = ""
    var index = 0
    var warTrackId: String = ""

    fun bind(war: NewWar, warTrack: NewWarTrack?, index: Int, onEditTrack: Flow<Unit>, onEditPositions: Flow<Unit>) {

        warId = war.mid ?: ""
        warTrackId = warTrack?.mid ?: ""
        this.index = index

        val positionsFlow = when (warTrackId.isEmpty()) {
            true -> flowOf(war.warTracks?.get(index)?.warPositions).filterNotNull()
            else -> firebaseRepository.getPositions(warId, warTrackId)
        }

        positionsFlow
            .flatMapLatest { it.withPlayerName(firebaseRepository) }
            .onEach {
                _sharedPositions.emit(it)
                _sharedButtonsVisible.emit(preferencesRepository.currentUser?.mid == war.playerHostId && !MKWar(war).isOver)
            }.launchIn(viewModelScope)

        onEditTrack.bind(_sharedEditTrackClick, viewModelScope)
        onEditPositions.bind(_sharedEditPositionsClick, viewModelScope)
    }

    fun refreshTrack() {
        firebaseRepository.getNewWar(warId)
            .mapNotNull { MKWarTrack(it?.warTracks?.get(index)) }
            .onEach { _sharedTrackRefreshed.emit(it) }
            .mapNotNull { it.track?.warPositions }
            .flatMapLatest { it.withPlayerName(firebaseRepository) }
            .bind(_sharedPositions, viewModelScope)
    }

}