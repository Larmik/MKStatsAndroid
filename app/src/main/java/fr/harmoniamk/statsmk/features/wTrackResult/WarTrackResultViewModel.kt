package fr.harmoniamk.statsmk.features.wTrackResult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.model.War
import fr.harmoniamk.statsmk.database.model.WarPosition
import fr.harmoniamk.statsmk.database.model.WarTrack
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.MKWar
import fr.harmoniamk.statsmk.model.MKWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarTrackResultViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedWarPos = MutableSharedFlow<List<WarPosition>>()
    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _sharedQuit = MutableSharedFlow<Unit>()
    private val _sharedCancel = MutableSharedFlow<Unit>()
    private val _sharedBackToCurrent = MutableSharedFlow<Unit>()
    private val _sharedGoToWarResume = MutableSharedFlow<War>()
    private val _sharedScore = MutableSharedFlow<MKWarTrack>()

    val sharedWarPos = _sharedWarPos.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedCancel = _sharedCancel.asSharedFlow()
    val sharedBackToCurrent = _sharedBackToCurrent.asSharedFlow()
    val sharedScore = _sharedScore.asSharedFlow()
    val sharedGoToWarResume = _sharedGoToWarResume.asSharedFlow()

    fun bind(warTrackId: String? = null, onBack: Flow<Unit>, onQuit: Flow<Unit>, onBackDialog: Flow<Unit>, onValid: Flow<Unit>) {
        warTrackId?.let { id ->
            var score: Int? = null
            var track: WarTrack? = null

            firebaseRepository.getWarPositions()
                .map { list -> list.filter { pos -> pos.warTrackId ==  warTrackId }.sortedBy { it.position } }
                .onEach { _sharedWarPos.emit(it) }
                .filter { it.size == 6 }
                .mapNotNull { list ->
                    score = list.mapNotNull { pos -> pos.points }.sumOf { it }
                    score
                }
                .flatMapLatest { firebaseRepository.getWarTrack(id) }
                .mapNotNull {
                    track = it.apply { this?.teamScore = score }
                    track
                }
                .flatMapLatest { firebaseRepository.writeWarTrack(it) }
                .map { MKWarTrack(track) }
                .bind(_sharedScore, viewModelScope)

            onValid
                .mapNotNull { track.apply { this?.isOver = true } }
                .flatMapLatest { firebaseRepository.writeWarTrack(it) }
                .mapNotNull { track?.warId }
                .flatMapLatest { firebaseRepository.getWar(it) }
                .filterNotNull()
                .mapNotNull {
                    it.apply {
                        this.scoreHost += track?.teamScore ?: 0
                        this.scoreOpponent += MKWarTrack(track).opponentScore ?: 0
                        this.trackPlayed += 1
                    }
                }
                .onEach { war ->
                    firebaseRepository.writeWar(war).first()
                    if (MKWar(war).isOver) {
                        firebaseRepository.getUsers().first().filter { it.currentWar == war.mid }.forEach {
                            val new = it.apply { this.currentWar = "-1" }
                            firebaseRepository.writeUser(new).first()
                        }
                        _sharedGoToWarResume.emit(war)
                    } else _sharedBackToCurrent.emit(Unit)
                }.launchIn(viewModelScope)

            onQuit
                .flatMapLatest { firebaseRepository.deleteWarTrack(id) }
                .flatMapLatest { firebaseRepository.getWarPositions() }
                .onEach { it.filter { pos -> pos.warTrackId == id }
                            .forEach { pos -> firebaseRepository.deleteWarPosition(pos).first() }
                    _sharedQuit.emit(Unit)
                }.launchIn(viewModelScope)
        }
        onBack.bind(_sharedBack, viewModelScope)
        onBackDialog.bind(_sharedCancel, viewModelScope)
    }
}