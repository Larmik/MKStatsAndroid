package fr.harmoniamk.statsmk.features.currentWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.War
import fr.harmoniamk.statsmk.database.firebase.model.WarTrack
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.getCurrent
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
@FlowPreview
class CurrentWarViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedButtonVisible = MutableSharedFlow<Boolean>()
    private val _sharedCurrentWar = MutableSharedFlow<War>()
    private val  _sharedQuit = MutableSharedFlow<Unit>()
    private val _sharedSelectTrack = MutableSharedFlow<Unit>()
    private val _sharedTracks = MutableSharedFlow<List<WarTrack>>()
    private val _sharedTrackClick = MutableSharedFlow<Pair<Int, WarTrack>>()
    private val _sharedPlayers = MutableSharedFlow<List<String>>()

    val sharedButtonVisible = _sharedButtonVisible.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedSelectTrack = _sharedSelectTrack.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedPlayers = _sharedPlayers.asSharedFlow()

    fun bind(onBack: Flow<Unit>, onNextTrack: Flow<Unit>, onTrackClick: Flow<Pair<Int, WarTrack>>, onBackDialog: Flow<Unit>, onQuitDialog: Flow<Unit>) {

        flowOf(firebaseRepository.getWars(), firebaseRepository.listenToWars())
            .flattenMerge()
            .mapNotNull { it.getCurrent(preferencesRepository.currentTeam?.mid) }
            .onEach { w ->
                val tracks = firebaseRepository.getWarTracks().first().filter { it.warId == w.mid && it.isOver.isTrue}
                val players = firebaseRepository.getUsers().first().filter { it.currentWar == w.mid }
                    .sortedBy { it.name?.toLowerCase(Locale.ROOT) }.mapNotNull { it.name }
                _sharedCurrentWar.emit(w)
                _sharedButtonVisible.emit(w.playerHostId == preferencesRepository.currentUser?.mid && !w.isOver)
                _sharedTracks.emit(tracks)
                _sharedPlayers.emit(players)
            }.launchIn(viewModelScope)

        onBack.bind(_sharedQuit, viewModelScope)
        onNextTrack.bind(_sharedSelectTrack, viewModelScope)
        onTrackClick.bind(_sharedTrackClick, viewModelScope)
    }

}