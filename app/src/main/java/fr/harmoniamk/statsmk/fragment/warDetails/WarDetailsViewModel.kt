package fr.harmoniamk.statsmk.fragment.warDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.fragment.currentWar.CurrentPlayerModel
import fr.harmoniamk.statsmk.model.firebase.Penalty
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarDetailsViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface) : ViewModel() {

    private val _sharedWarPlayers = MutableSharedFlow<List<CurrentPlayerModel>>()
    private val _sharedTracks = MutableSharedFlow<List<MKWarTrack>>()
    private val _sharedBestTrack = MutableSharedFlow<MKWarTrack>()
    private val _sharedWorstTrack = MutableSharedFlow<MKWarTrack>()
    private val _sharedTrackClick = MutableSharedFlow<Int>()
    private val _sharedWarDeleted = MutableSharedFlow<Unit>()
    private val _sharedDeleteWarVisible = MutableSharedFlow<Boolean>()
    private val _sharedPlayerHost = MutableSharedFlow<String>()
    private val _sharedWarName = MutableSharedFlow<String?>()
    private val _sharedShockCount = MutableSharedFlow<String?>()
    private val _sharedPenalties = MutableSharedFlow<List<Penalty>?>()

    val sharedWarPlayers = _sharedWarPlayers.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()
    val sharedBestTrack = _sharedBestTrack.asSharedFlow()
    val sharedWorstTrack = _sharedWorstTrack.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedWarDeleted = _sharedWarDeleted.asSharedFlow()
    val sharedDeleteWarVisible = _sharedDeleteWarVisible.asSharedFlow()
    val sharedPlayerHost = _sharedPlayerHost.asSharedFlow()
    val sharedWarName = _sharedWarName.asSharedFlow()
    val sharedPenalties = _sharedPenalties.asSharedFlow()
    val sharedShockCount = _sharedShockCount.asSharedFlow()

    fun bind(war: MKWar?, onTrackClick: Flow<Int>, onDeleteWar: Flow<Unit>) {
        war?.let {

            databaseRepository.getUser(authenticationRepository.user?.uid)
                .mapNotNull { it?.role }
                .mapNotNull { it >= UserRole.LEADER.ordinal && networkRepository.networkAvailable }
                .bind(_sharedDeleteWarVisible, viewModelScope)

            flowOf(it.war)
                .onEach { war ->
                    _sharedPlayerHost.emit("Créée par ${databaseRepository.getUser(war?.playerHostId).firstOrNull()?.name ?: ""}")
                    _sharedWarName.emit(it.name)
                    war?.penalties?.let { penalty ->
                        _sharedPenalties.emit(penalty.withTeamName(databaseRepository).firstOrNull())
                    }
                }
                .mapNotNull { it?.warTracks?.map { MKWarTrack(it) } }
                .onEach {
                    val positions = mutableListOf<Pair<User?, Int>>()
                    val players = databaseRepository.getUsers().firstOrNull()
                    _sharedTracks.emit(it)
                    _sharedBestTrack.emit(it.maxByOrNull { track -> track.teamScore }!!)
                    _sharedWorstTrack.emit(it.minByOrNull { track -> track.teamScore }!!)
                    it.forEach {
                        val trackPositions = mutableListOf<MKWarPosition>()
                        it.track?.warPositions?.let { warPositions ->
                            warPositions.forEach { position ->
                                trackPositions.add(MKWarPosition(position, players?.singleOrNull { it.mid ==  position.playerId }))
                            }
                            trackPositions.groupBy { it.player }.entries.forEach { entry ->
                                positions.add(Pair(entry.key, entry.value.map { pos -> pos.position.position.positionToPoints() }.sum()))
                            }
                        }
                    }
                    val temp = positions.groupBy { it.first }.map { Pair(it.key, it.value.map { it.second }.sum()) }.sortedByDescending { it.second }
                    val finalList = mutableListOf<CurrentPlayerModel>()
                    temp.forEach { pair ->
                        val isSubPlayer = it.size > it.filter { track -> track.hasPlayer(pair.first?.mid) }.size
                        val isOld = isSubPlayer && it.firstOrNull()?.hasPlayer(pair.first?.mid).isTrue
                        val isNew = isSubPlayer && it.lastOrNull()?.hasPlayer(pair.first?.mid).isTrue
                        finalList.add(CurrentPlayerModel(pair.first, pair.second, isOld, isNew))
                    }
                    _sharedWarPlayers.emit(finalList)
                }
                .onEach { list ->
                    var count = 0
                    list.forEach { track ->
                        track.track?.shocks?.map { it.count }?.forEach {
                            count += it
                        }
                    }
                    count.takeIf { it > 0 }?.let {
                        _sharedShockCount.emit("x$it")
                    }
                }
                .launchIn(viewModelScope)
            onTrackClick.bind(_sharedTrackClick, viewModelScope)
            onDeleteWar
                .mapNotNull { war }
                .flatMapLatest { firebaseRepository.deleteNewWar(it) }
                .bind(_sharedWarDeleted, viewModelScope)
        }
    }

}