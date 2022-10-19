package fr.harmoniamk.statsmk.fragment.currentWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.Penalty
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
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
    private val _sharedCurrentWar = MutableSharedFlow<MKWar>()
    private val  _sharedQuit = MutableSharedFlow<Unit>()
    private val  _sharedBackToWars = MutableSharedFlow<Unit>()
    private val _sharedSelectTrack = MutableSharedFlow<Unit>()
    private val _sharedTracks = MutableSharedFlow<List<MKWarTrack>>()
    private val _sharedTrackClick = MutableSharedFlow<Int>()
    private val _sharedPlayers = MutableSharedFlow<List<String>>()
    private val _sharedPopupShowing = MutableSharedFlow<Boolean>()
    private val _sharedAddPenalty = MutableSharedFlow<NewWar>()
    private val _sharedPenalties = MutableSharedFlow<List<Penalty>?>()

    val sharedButtonVisible = _sharedButtonVisible.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedBackToWars = _sharedBackToWars.asSharedFlow()
    val sharedSelectTrack = _sharedSelectTrack.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedPlayers = _sharedPlayers.asSharedFlow()
    val sharedPopupShowing = _sharedPopupShowing.asSharedFlow()
    val sharedAddPenalty = _sharedAddPenalty.asSharedFlow()
    val sharedPenalties = _sharedPenalties.asSharedFlow()

    fun bind(onBack: Flow<Unit>, onNextTrack: Flow<Unit>, onTrackClick: Flow<Int>, onDelete: Flow<Unit>, onPopup: Flow<Boolean>, onPenalty: Flow<Unit>) {

        flowOf(firebaseRepository.getNewWars(), firebaseRepository.listenToNewWars())
            .flattenMerge()
            .mapNotNull { it.map { w -> MKWar(w) }.getCurrent(preferencesRepository.currentTeam?.mid) }
            .flatMapLatest { listOf(it).withName(firebaseRepository) }
            .mapNotNull { it.singleOrNull() }
            .onEach { war ->
                preferencesRepository.currentWar = war.war
                val players = firebaseRepository.getUsers().first().filter { it.currentWar == war.war?.mid }.sortedBy { it.name?.toLowerCase(Locale.ROOT) }.mapNotNull { it.name }
                _sharedCurrentWar.emit(war)
                _sharedButtonVisible.emit(preferencesRepository.currentUser?.isAdmin.isTrue && !war.isOver)
                _sharedTracks.emit(war.war?.warTracks.orEmpty().map { MKWarTrack(it) })
                _sharedPlayers.emit(players)
            }
            .mapNotNull { it.war?.penalties }
            .flatMapLatest { it.withTeamName(firebaseRepository) }
            .bind(_sharedPenalties, viewModelScope)

        onBack.bind(_sharedQuit, viewModelScope)
        onNextTrack.bind(_sharedSelectTrack, viewModelScope)
        onTrackClick.bind(_sharedTrackClick, viewModelScope)
        onPopup.bind(_sharedPopupShowing, viewModelScope)

        onDelete
            .flatMapLatest { firebaseRepository.getUsers() }
            .map { list -> list.filter { user -> user.currentWar == preferencesRepository.currentWar?.mid } }
            .onEach { list ->
                list.forEach { firebaseRepository.writeUser(it.apply { this.currentWar = "-1" }).first() }
            }.mapNotNull { preferencesRepository.currentWar?.mid }
            .flatMapLatest { firebaseRepository.deleteNewWar(it) }
            .bind(_sharedBackToWars, viewModelScope)

        onPenalty
            .mapNotNull {
                preferencesRepository.currentWar
            }
            .bind(_sharedAddPenalty, viewModelScope)


    }

}