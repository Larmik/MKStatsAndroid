package fr.harmoniamk.statsmk.features.currentWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.War
import fr.harmoniamk.statsmk.database.firebase.model.WarTrack
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
@FlowPreview
class CurrentWarViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedButtonVisible = MutableSharedFlow<Boolean>()
    private val _sharedWaitingVisible = MutableSharedFlow<Boolean>()
    private val _sharedCurrentWar = MutableSharedFlow<War>()
    private val  _sharedBack = MutableSharedFlow<Unit>()
    private val  _sharedQuit = MutableSharedFlow<Unit>()
    private val  _sharedCancel = MutableSharedFlow<Unit>()
    private val _sharedSelectTrack = MutableSharedFlow<Unit>()
    private val _sharedGoToPos = MutableSharedFlow<WarTrack>()
    private val _sharedTracks = MutableSharedFlow<List<WarTrack>>()
    private val _sharedTrackClick = MutableSharedFlow<Pair<Int, WarTrack>>()
    private val _sharedPlayersConnected = MutableSharedFlow<Int>()

    val sharedButtonVisible = _sharedButtonVisible.asSharedFlow()
    val sharedWaitingVisible = _sharedWaitingVisible.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedCancel = _sharedCancel.asSharedFlow()
    val sharedSelectTrack = _sharedSelectTrack.asSharedFlow()
    val sharedGoToPos = _sharedGoToPos.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedPlayersConnected = _sharedPlayersConnected.asSharedFlow()

    fun bind(war: War? = null, onBack: Flow<Unit>, onNextTrack: Flow<Unit>, onTrackClick: Flow<Pair<Int, WarTrack>>) {
        firebaseRepository.getWarTracks()
            .mapNotNull { list -> list.filter { track -> track.warId == war?.mid } }
            .onEach { _sharedTracks.emit(it) }
            .mapNotNull { war }
            .onEach {
                _sharedButtonVisible.emit(false)
                _sharedWaitingVisible.emit(false)
            }
            .bind(_sharedCurrentWar, viewModelScope)

        preferencesRepository.currentUser?.currentWar?.takeIf { it != "-1" }?.let { it ->
            val currentWar = firebaseRepository.getWar(it)
                .filterNotNull()
                .onEach { _sharedCurrentWar.emit(it) }
                .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

            currentWar
                .filterNot { it.isOver }
                .onEach {
                    _sharedButtonVisible.emit(it.playerHostId == preferencesRepository.currentUser?.mid)
                    _sharedWaitingVisible.emit(it.playerHostId != preferencesRepository.currentUser?.mid)
                    onBack.bind(_sharedBack, viewModelScope)
                }.launchIn(viewModelScope)

            currentWar
                .filter { it.isOver }
                .onEach {
                    _sharedButtonVisible.emit(false)
                    _sharedWaitingVisible.emit(false)
                    onBack
                        .mapNotNull { preferencesRepository.currentUser?.apply { this.currentWar = "-1" } }
                        .onEach { preferencesRepository.currentUser = it }
                        .flatMapLatest { firebaseRepository.writeUser(it) }
                        .bind(_sharedQuit, viewModelScope)
                }
                .launchIn(viewModelScope)


            firebaseRepository.getWarTracks()
                .mapNotNull {list -> list.filter { track -> track.warId == preferencesRepository.currentUser?.currentWar } }
                .bind(_sharedTracks, viewModelScope)

            firebaseRepository.listenToWarTracks()
                .mapNotNull {
                    it.lastOrNull().takeIf { track -> !track?.isOver.isTrue && track?.warId == preferencesRepository.currentUser?.currentWar }
                }
                .bind(_sharedGoToPos, viewModelScope)

            firebaseRepository.listenToUsers()
                .map { it.filter { user -> user.currentWar == preferencesRepository.currentUser?.currentWar }.size }
                .bind(_sharedPlayersConnected, viewModelScope)

            onNextTrack.bind(_sharedSelectTrack, viewModelScope)
        }
        onTrackClick.bind(_sharedTrackClick, viewModelScope)

    }

    fun bindDialog(onQuit: Flow<Unit>, onBack: Flow<Unit>) {
        onQuit
            .mapNotNull { preferencesRepository.currentUser?.apply { this.currentWar = "-1" } }
            .onEach { preferencesRepository.currentUser = it }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .bind(_sharedQuit, viewModelScope)
        onBack.bind(_sharedCancel, viewModelScope)
    }

}