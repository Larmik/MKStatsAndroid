package fr.harmoniamk.statsmk.features.currentWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.War
import fr.harmoniamk.statsmk.database.firebase.model.WarTrack
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.getCurrent
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
    private val _sharedCurrentWar = MutableSharedFlow<War>()
    private val  _sharedBack = MutableSharedFlow<Unit>()
    private val  _sharedQuit = MutableSharedFlow<Unit>()
    private val  _sharedCancel = MutableSharedFlow<Unit>()
    private val _sharedSelectTrack = MutableSharedFlow<Unit>()
    private val _sharedTracks = MutableSharedFlow<List<WarTrack>>()
    private val _sharedTrackClick = MutableSharedFlow<Pair<Int, WarTrack>>()

    val sharedButtonVisible = _sharedButtonVisible.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedCancel = _sharedCancel.asSharedFlow()
    val sharedSelectTrack = _sharedSelectTrack.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()

    fun bind(onBack: Flow<Unit>, onNextTrack: Flow<Unit>, onTrackClick: Flow<Pair<Int, WarTrack>>) {

        val currentWar = firebaseRepository.getWars()
            .mapNotNull { it.getCurrent(preferencesRepository.currentTeam?.mid) }
            .onEach { w ->
                _sharedCurrentWar.emit(w)
                val tracks = firebaseRepository.getWarTracks().first().filter { it.warId == w.mid }
                _sharedTracks.emit(tracks)
            }
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

        currentWar
            .filterNot { it.isOver }
            .onEach {
                _sharedButtonVisible.emit(it.playerHostId == preferencesRepository.currentUser?.mid)
                onBack.bind(_sharedBack, viewModelScope)
            }.launchIn(viewModelScope)

        currentWar
            .filter { it.isOver }
            .onEach {
                _sharedButtonVisible.emit(false)
                onBack
                    .mapNotNull { preferencesRepository.currentUser?.apply { this.currentWar = "-1" } }
                    .onEach { preferencesRepository.currentUser = it }
                    .flatMapLatest { firebaseRepository.writeUser(it) }
                    .bind(_sharedQuit, viewModelScope)
            }
            .launchIn(viewModelScope)

        onNextTrack.bind(_sharedSelectTrack, viewModelScope)
        onTrackClick.bind(_sharedTrackClick, viewModelScope)

    }

    fun bindDialog(onQuit: Flow<Unit>, onBack: Flow<Unit>) {
        onQuit.bind(_sharedQuit, viewModelScope)
        onBack.bind(_sharedCancel, viewModelScope)
    }

}