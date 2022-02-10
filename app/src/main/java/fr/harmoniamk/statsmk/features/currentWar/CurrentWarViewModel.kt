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
    private val  _sharedWaitingPlayers = MutableSharedFlow<Unit>()
    private val _sharedSelectTrack = MutableSharedFlow<Unit>()
    private val _sharedGoToPos = MutableSharedFlow<WarTrack>()
    private val _sharedTracks = MutableSharedFlow<List<WarTrack>>()

    val sharedButtonVisible = _sharedButtonVisible.asSharedFlow()
    val sharedWaitingVisible = _sharedWaitingVisible.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedCancel = _sharedCancel.asSharedFlow()
    val sharedWaitingPlayers = _sharedWaitingPlayers.asSharedFlow()
    val sharedSelectTrack = _sharedSelectTrack.asSharedFlow()
    val sharedGoToPos = _sharedGoToPos.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()


    fun bind(onBack: Flow<Unit>, onNextTrack: Flow<Unit>) {
        preferencesRepository.currentUser?.currentWar?.let { it ->
            firebaseRepository.getWar(it)
                .filterNotNull()
                .onEach { war ->
                    _sharedCurrentWar.emit(war)
                    _sharedButtonVisible.emit(war.playerHostId == preferencesRepository.currentUser?.mid && !war.isOver)
                    _sharedWaitingVisible.emit(war.playerHostId != preferencesRepository.currentUser?.mid && !war.isOver)
                }.launchIn(viewModelScope)

            firebaseRepository.listenToUsers()
                .onEach {
                if (it.filter { user -> user.currentWar == preferencesRepository.currentUser?.currentWar }.size < 1)
                    _sharedWaitingPlayers.emit(Unit)
            }.launchIn(viewModelScope)

            firebaseRepository.getWarTracks()
                .mapNotNull {list -> list.filter { track -> track.warId == preferencesRepository.currentUser?.currentWar } }
                .bind(_sharedTracks, viewModelScope)

            onBack.bind(_sharedBack, viewModelScope)
            onNextTrack.bind(_sharedSelectTrack, viewModelScope)

        }

        firebaseRepository.listenToWarTracks()
            .mapNotNull {
                it.lastOrNull().takeIf { track -> !track?.isOver.isTrue && track?.warId == preferencesRepository.currentUser?.currentWar }
            }
            .bind(_sharedGoToPos, viewModelScope)

    }


    fun bindDialog(onQuit: Flow<Unit>, onBack: Flow<Unit>) {
        onQuit.bind(_sharedQuit, viewModelScope)
        onBack.bind(_sharedCancel, viewModelScope)
    }

}