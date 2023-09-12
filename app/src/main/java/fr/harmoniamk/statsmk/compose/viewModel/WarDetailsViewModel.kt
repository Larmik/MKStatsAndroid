package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import fr.harmoniamk.statsmk.model.local.CurrentPlayerModel
import fr.harmoniamk.statsmk.compose.ViewModelFactoryProvider
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.extension.withTeamName
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach


@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class WarDetailsViewModel  @AssistedInject constructor(
    @Assisted private val id: String,
    private val firebaseRepository: FirebaseRepositoryInterface,
    authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface
) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            id: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(id) as T
            }
        }

        @Composable
        fun viewModel(id: String?): WarDetailsViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).warDetailsViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    id = id
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(id: String?): WarDetailsViewModel
    }
    private val _sharedWar = MutableStateFlow<MKWar?>(null)
    private val _sharedWarPlayers = MutableStateFlow<List<CurrentPlayerModel>?>(null)
    private val _sharedTracks = MutableStateFlow<List<MKWarTrack>?>(null)
    private val _sharedBestTrack = MutableSharedFlow<MKWarTrack>()
    private val _sharedWorstTrack = MutableSharedFlow<MKWarTrack>()
    private val _sharedTrackClick = MutableSharedFlow<Int>()
    private val _sharedWarDeleted = MutableSharedFlow<Unit>()
    private val _sharedDeleteWarVisible = MutableSharedFlow<Boolean>()
    private val _sharedPlayerHost = MutableSharedFlow<String>()
    private val _sharedShockCount = MutableSharedFlow<String?>()
    private val _sharedPenalties = MutableSharedFlow<List<Penalty>?>()

    val sharedWar = _sharedWar.asStateFlow()
    val sharedTracks = _sharedTracks.asStateFlow()
    val sharedWarPlayers = _sharedWarPlayers.asStateFlow()
    val sharedBestTrack = _sharedBestTrack.asSharedFlow()
    val sharedWorstTrack = _sharedWorstTrack.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedWarDeleted = _sharedWarDeleted.asSharedFlow()
    val sharedDeleteWarVisible = _sharedDeleteWarVisible.asSharedFlow()
    val sharedPlayerHost = _sharedPlayerHost.asSharedFlow()
    val sharedPenalties = _sharedPenalties.asSharedFlow()
    val sharedShockCount = _sharedShockCount.asSharedFlow()

    init {

        databaseRepository.getUser(authenticationRepository.user?.uid)
            .mapNotNull { it?.role }
            .mapNotNull { it > UserRole.LEADER.ordinal && networkRepository.networkAvailable }
            .bind(_sharedDeleteWarVisible, viewModelScope)

        databaseRepository.getWar(id)
            .onEach { war ->
                _sharedPlayerHost.emit(databaseRepository.getUser(war?.war?.playerHostId).firstOrNull()?.name ?: "")
                _sharedWar.value = war
                war?.war?.penalties?.let { penalty ->
                    _sharedPenalties.emit(penalty.withTeamName(databaseRepository).firstOrNull())
                }
            }
            .mapNotNull { it?.warTracks }
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
    }

    fun bind(war: MKWar?, onTrackClick: Flow<Int>, onDeleteWar: Flow<Unit>) {
        onTrackClick.bind(_sharedTrackClick, viewModelScope)
        onDeleteWar
            .mapNotNull { war }
            .flatMapLatest { firebaseRepository.deleteNewWar(it) }
            .bind(_sharedWarDeleted, viewModelScope)
    }

}