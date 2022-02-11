package fr.harmoniamk.statsmk.features.wTrackResult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.WarPosition
import fr.harmoniamk.statsmk.database.firebase.model.WarTrack
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
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
    private val _sharedHost = MutableSharedFlow<Unit>()
    private val _sharedBackToCurrent = MutableSharedFlow<Unit>()
    private val _sharedScore = MutableSharedFlow<WarTrack>()
    private val _sharedWaitingVisibility = MutableSharedFlow<Boolean>()


    val sharedWarPos = _sharedWarPos.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedCancel = _sharedCancel.asSharedFlow()
    val sharedBackToCurrent = _sharedBackToCurrent.asSharedFlow()
    val sharedScore = _sharedScore.asSharedFlow()
    val sharedHost = _sharedHost.asSharedFlow()
    val sharedWaitingVisibility = _sharedWaitingVisibility.asSharedFlow()

    fun bind(warTrackId: String? = null, onBack: Flow<Unit>, onQuit: Flow<Unit>, onBackDialog: Flow<Unit>, onValid: Flow<Unit>) {
        warTrackId?.let { id ->

            var hasEmitted = false
            var score: Int? = null
            var track: WarTrack? = null

            val positionListener = firebaseRepository.listenToWarPositions()
                .map { it.filter { pos -> pos.warTrackId ==  warTrackId } }
                .onEach { _sharedWarPos.emit(it) }
                .shareIn(viewModelScope, SharingStarted.Eagerly)

            positionListener
                .filter { it.size < 6 }
                .onEach { hasEmitted = false
                _sharedWaitingVisibility.emit(true)}.launchIn(viewModelScope)

            //Mock
            positionListener
                .filter { it.size == 1 }
                .onEach { delay(50) }
                .map { WarPosition(mid = System.currentTimeMillis().toString(), warTrackId = warTrackId, playerId = "NSD", position = 6) }
                .flatMapLatest { firebaseRepository.writeWarPosition(it) }
                .onEach { delay(50) }
                .map { WarPosition(mid = System.currentTimeMillis().toString(), warTrackId = warTrackId, playerId = "RemDtz", position = 4) }
                .flatMapLatest { firebaseRepository.writeWarPosition(it) }
                .onEach { delay(50) }
                .map { WarPosition(mid = System.currentTimeMillis().toString(), warTrackId = warTrackId, playerId = "Square", position = 5) }
                .flatMapLatest { firebaseRepository.writeWarPosition(it) }
                .onEach { delay(50) }
                .map { WarPosition(mid = System.currentTimeMillis().toString(), warTrackId = warTrackId, playerId = "Guyal", position = 7) }
                .flatMapLatest { firebaseRepository.writeWarPosition(it) }
                .onEach { delay(50) }
                .map { WarPosition(mid = System.currentTimeMillis().toString(), warTrackId = warTrackId, playerId = "Nulls", position = 8) }
                .flatMapLatest { firebaseRepository.writeWarPosition(it) }
                .launchIn(viewModelScope)

            positionListener
                .filter { it.size == 6  && !hasEmitted}
                .onEach { _sharedWaitingVisibility.emit(false) }
                .mapNotNull { list ->
                    hasEmitted = true
                    score = list.mapNotNull { pos -> pos.points }.sumOf { it }
                    score
                }
                .flatMapLatest { firebaseRepository.getWarTrack(id) }
                .mapNotNull {
                    track = it.apply { this?.teamScore = score }
                    track
                }
                .flatMapLatest { firebaseRepository.writeWarTrack(it) }
                .mapNotNull { track }
                .onEach { _sharedScore.emit(it) }
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
                        this.scoreOpponent += track?.opponentScore ?: 0
                        this.trackPlayed += 1
                    }
                }
                .flatMapLatest { firebaseRepository.writeWar(it) }
                .launchIn(viewModelScope)



            firebaseRepository.listenToWarTracks()
                .mapNotNull {
                    it.singleOrNull { tr -> tr.isOver.isTrue && tr.mid == id }
                }
                .map {}
                .bind(_sharedBackToCurrent, viewModelScope)

            preferencesRepository.currentUser?.currentWar?.let { it ->
                firebaseRepository.getWar(it)
                    .filterNotNull()
                    .onEach {
                        if (it.playerHostId == preferencesRepository.currentUser?.mid)
                            _sharedHost.emit(Unit)
                    }.launchIn(viewModelScope)


                onQuit
                .flatMapLatest { firebaseRepository.getWarPositions() }
                .mapNotNull {
                    it.lastOrNull {
                            pos -> pos.warTrackId == id && pos.playerId == preferencesRepository.currentUser?.name
                    }
                }
                .flatMapLatest { firebaseRepository.deleteWarPosition(it) }
                .bind(_sharedQuit, viewModelScope)
        }

        onBack.bind(_sharedBack, viewModelScope)
        onBackDialog.bind(_sharedCancel, viewModelScope)


    }



}}