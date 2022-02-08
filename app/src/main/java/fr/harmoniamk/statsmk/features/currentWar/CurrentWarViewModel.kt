package fr.harmoniamk.statsmk.features.currentWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.War
import fr.harmoniamk.statsmk.database.firebase.model.WarTrack
import fr.harmoniamk.statsmk.extension.bind
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

    private val _sharedHost = MutableSharedFlow<Unit>()
    private val _sharedCurrentWar = MutableSharedFlow<War>()
    private val  _sharedBack = MutableSharedFlow<Unit>()
    private val  _sharedQuit = MutableSharedFlow<Unit>()
    private val  _sharedCancel = MutableSharedFlow<Unit>()
    private val  _sharedWaitingPlayers = MutableSharedFlow<Unit>()
    private val _sharedSelectTrack = MutableSharedFlow<Unit>()
    private val _sharedGoToPos = MutableSharedFlow<WarTrack>()

    val sharedHost = _sharedHost.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedCancel = _sharedCancel.asSharedFlow()
    val sharedWaitingPlayers = _sharedWaitingPlayers.asSharedFlow()
    val sharedSelectTrack = _sharedSelectTrack.asSharedFlow()
    val sharedGoToPos = _sharedGoToPos.asSharedFlow()


    fun bind(onBack: Flow<Unit>, onNextTrack: Flow<Unit>) {
        preferencesRepository.currentUser?.currentWar?.let {
            firebaseRepository.getWar(it)
                .filterNotNull()
                .onEach {
                        war -> _sharedCurrentWar.emit(war)
                    if (war.playerHostId == preferencesRepository.currentUser?.mid)
                        _sharedHost.emit(Unit)
                }.launchIn(viewModelScope)

            firebaseRepository.listenToUsers()
                .onEach {
                if (it.filter { user -> user.currentWar == preferencesRepository.currentUser?.currentWar }.size < 2)
                    _sharedWaitingPlayers.emit(Unit)
            }.launchIn(viewModelScope)

            onBack.bind(_sharedBack, viewModelScope)
            onNextTrack.bind(_sharedSelectTrack, viewModelScope)

        }

        firebaseRepository.listenToWarTracks()
            .mapNotNull { it.filter { track -> track.warId == preferencesRepository.currentUser?.currentWar }.lastOrNull() }
            .bind(_sharedGoToPos, viewModelScope)

    }


    fun bindDialog(onQuit: Flow<Unit>, onBack: Flow<Unit>) {
        onQuit.bind(_sharedQuit, viewModelScope)
        onBack.bind(_sharedCancel, viewModelScope)
    }

}