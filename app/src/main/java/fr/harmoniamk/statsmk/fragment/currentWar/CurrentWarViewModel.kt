package fr.harmoniamk.statsmk.fragment.currentWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.Penalty
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
@FlowPreview
class CurrentWarViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedButtonVisible = MutableSharedFlow<Boolean>()
    private val _sharedCurrentWar = MutableSharedFlow<MKWar>()
    private val  _sharedQuit = MutableSharedFlow<Unit>()
    private val  _sharedBackToWars = MutableSharedFlow<Unit>()
    private val _sharedSelectTrack = MutableSharedFlow<Unit>()
    private val _sharedTracks = MutableSharedFlow<List<MKWarTrack>>()
    private val _sharedTrackClick = MutableSharedFlow<Int>()
    private val _sharedPopupShowing = MutableSharedFlow<Boolean>()
    private val _sharedAddPenalty = MutableSharedFlow<NewWar>()
    private val _sharedPenalties = MutableSharedFlow<List<Penalty>?>()
    private val _sharedWarPlayers = MutableSharedFlow<List<CurrentPlayerModel>>()
    private val _sharedSubPlayer = MutableSharedFlow<Unit>()
    private val _sharedShockCount = MutableSharedFlow<String>()
    private val _sharedLoading = MutableSharedFlow<Boolean>()

    val sharedButtonVisible = _sharedButtonVisible.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedBackToWars = _sharedBackToWars.asSharedFlow()
    val sharedSelectTrack = _sharedSelectTrack.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedPopupShowing = _sharedPopupShowing.asSharedFlow()
    val sharedAddPenalty = _sharedAddPenalty.asSharedFlow()
    val sharedPenalties = _sharedPenalties.asSharedFlow()
    val sharedWarPlayers = _sharedWarPlayers.asSharedFlow()
    val sharedSubPlayer = _sharedSubPlayer.asSharedFlow()
    val sharedShockCount = _sharedShockCount.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()

    fun bind(onBack: Flow<Unit>, onNextTrack: Flow<Unit>, onTrackClick: Flow<Int>, onPopup: Flow<Unit>, onPenalty: Flow<Unit>, onSub: Flow<Unit>, onSubDismiss: Flow<Unit>) {
        val currentWar = firebaseRepository.listenToNewWars()
            .map { it.map { w -> MKWar(w) }.getCurrent(preferencesRepository.currentTeam?.mid) }
            .shareIn(viewModelScope, SharingStarted.Lazily)


        val warFlow = currentWar
            .filterNotNull()
            .flatMapLatest { listOf(it).withName(databaseRepository) }
            .mapNotNull { it.singleOrNull() }

            warFlow.onEach { war ->
                val isAdmin = (authenticationRepository.userRole.firstOrNull() ?: 0) >= UserRole.ADMIN.ordinal
                preferencesRepository.currentWar = war.war
                _sharedCurrentWar.emit(war)
                _sharedButtonVisible.emit(isAdmin.isTrue && !war.isOver)
                _sharedTracks.emit(war.war?.warTracks.orEmpty().map { MKWarTrack(it) })
                val players = databaseRepository.getUsers().first().filter { it.currentWar == preferencesRepository.currentWar?.mid }
                    .sortedBy { it.name?.toLowerCase(Locale.ROOT) }
                _sharedWarPlayers.takeIf { war.war?.warTracks == null }?.emit(players.map { CurrentPlayerModel(it, 0) })

            }
            .mapNotNull { it.war?.penalties }
            .flatMapLatest { it.withTeamName(databaseRepository) }
            .bind(_sharedPenalties, viewModelScope)

        val trackPlayersFlow = warFlow
            .mapNotNull { it.war?.warTracks?.map { MKWarTrack(it) } }
            .map {
                var shockCount = 0
                val positions = mutableListOf<Pair<User?, Int>>()
                val players = databaseRepository.getUsers().firstOrNull()
                _sharedTracks.emit(it)
                it.forEach {
                    it.track?.warPositions?.let { warPositions ->
                        val trackPositions = mutableListOf<MKWarPosition>()
                        warPositions.forEach { position ->
                            trackPositions.add(MKWarPosition(position, players?.singleOrNull { it.mid ==  position.playerId }))
                        }
                        trackPositions.groupBy { it.player }.entries.forEach { entry ->
                            positions.add(Pair(entry.key, entry.value.map { pos -> pos.position.position.positionToPoints() }.sum()))
                        }

                    }
                    it.track?.shocks?.map { it.count }?.forEach {
                        shockCount += it
                    }
                }
                val temp = positions.groupBy { it.first }.map { Pair(it.key, it.value.map { it.second }.sum()) }.sortedByDescending { it.second }
                val finalList = mutableListOf<CurrentPlayerModel>()
                temp.forEach { pair ->
                    val isOld = pair.first?.currentWar == "-1"
                    val isNew = it.size > it.filter { track -> track.hasPlayer(pair.first?.mid) }.size && pair.first?.currentWar == preferencesRepository.currentWar?.mid
                    finalList.add(CurrentPlayerModel(pair.first, pair.second, isOld, isNew))
                }
                databaseRepository.getUsers().firstOrNull()
                    ?.filter { it.currentWar == preferencesRepository.currentWar?.mid && !finalList.map { it.player?.mid }.contains(it.mid)}
                    ?.forEach { finalList.add(CurrentPlayerModel(it, 0, isNew = true)) }
                _sharedWarPlayers.emit(finalList)
                shockCount.takeIf { it > 0 }?.let {
                    _sharedShockCount.emit("x$it")
                }
            }
            .shareIn(viewModelScope, SharingStarted.Lazily)

        flowOf(flowOf(Unit), onSubDismiss)
            .flattenMerge()
            .flatMapLatest { trackPlayersFlow }
            .launchIn(viewModelScope)

        onBack.bind(_sharedQuit, viewModelScope)
        onNextTrack.bind(_sharedSelectTrack, viewModelScope)
        onTrackClick.bind(_sharedTrackClick, viewModelScope)
        onPopup
            .onEach { _sharedPopupShowing.emit(true) }
            .launchIn(viewModelScope)


        onPenalty
            .mapNotNull { preferencesRepository.currentWar }
            .bind(_sharedAddPenalty, viewModelScope)
        onSub.bind(_sharedSubPlayer, viewModelScope)


    }

    fun bindPopup(onDelete: Flow<Unit>, onDismiss: Flow<Unit>) {
        onDelete
            .flatMapLatest { databaseRepository.getUsers() }
            .map { list -> list.filter { user -> user.currentWar == preferencesRepository.currentWar?.mid } }
            .onEach { list ->
                list.forEach {
                    val newUser = it.apply { this.currentWar = "-1" }
                    firebaseRepository.writeUser(newUser).first()
                }
            }.mapNotNull {
                MKWar(preferencesRepository.currentWar)
            }
            .flatMapLatest { firebaseRepository.deleteNewWar(it) }
            .bind(_sharedBackToWars, viewModelScope)

        onDismiss
            .onEach { _sharedPopupShowing.emit(false) }
            .launchIn(viewModelScope)
    }
}