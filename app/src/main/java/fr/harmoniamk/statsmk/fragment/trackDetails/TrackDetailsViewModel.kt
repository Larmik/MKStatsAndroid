package fr.harmoniamk.statsmk.fragment.trackDetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.firebase.Shock
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class TrackDetailsViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface
): ViewModel() {

    private val _sharedPositions = MutableSharedFlow<List<MKWarPosition>>()
    private val _sharedEditTrackClick = MutableSharedFlow<Unit>()
    private val _sharedEditPositionsClick = MutableSharedFlow<Unit>()
    private val _sharedEditShocksClick = MutableSharedFlow<Unit>()
    private val _sharedButtonsVisible = MutableSharedFlow<Boolean>()
    private val _sharedTrackRefreshed = MutableSharedFlow<MKWarTrack>()
    private val _sharedWarName = MutableSharedFlow<String?>()
    private val _sharedShocks = MutableSharedFlow<List<Pair<String?, Shock>>>()

    val sharedPositions = _sharedPositions.asSharedFlow()
    val sharedEditTrackClick = _sharedEditTrackClick.asSharedFlow()
    val sharedEditPositionsClick = _sharedEditPositionsClick.asSharedFlow()
    val sharedEditShocksClick = _sharedEditShocksClick.asSharedFlow()
    val sharedButtonsVisible = _sharedButtonsVisible.asSharedFlow()
    val sharedTrackRefreshed = _sharedTrackRefreshed.asSharedFlow()
    val sharedWarName = _sharedWarName.asSharedFlow()
    val sharedShocks = _sharedShocks.asSharedFlow()

    private var warId: String = ""
    var index = 0
    private var warTrackId: String = ""
    private val users = mutableListOf<User>()

    fun bind(war: NewWar, warTrack: NewWarTrack?, index: Int, onEditTrack: Flow<Unit>, onEditPositions: Flow<Unit>, onEditShocks: Flow<Unit>) {

        warId = war.mid ?: ""
        warTrackId = warTrack?.mid ?: ""
        this.index = index

        databaseRepository.getUsers()
            .onEach {
                users.clear()
                users.addAll(it)
            }.launchIn(viewModelScope)

        databaseRepository.getWar(warId)
            .onEach {
                _sharedWarName.emit(it?.name)
                val shocks = mutableListOf<Pair<String?, Shock>>()
                it?.war?.warTracks?.singleOrNull {it.mid == warTrackId}?.shocks?.forEach { shock ->
                    shocks.add(Pair(users.singleOrNull { it.mid == shock.playerId }?.name, Shock(shock.playerId, shock.count)))
                }
                _sharedShocks.emit(shocks)
            }.launchIn(viewModelScope)
        val positionsFlow = when (warTrackId.isEmpty()) {
            true -> flowOf(war.warTracks?.get(index)?.warPositions).filterNotNull()
            else -> {
                databaseRepository.getWar(warId)
                    .mapNotNull { it?.warTracks?.singleOrNull { track -> track.track?.mid == warTrackId }?.track?.warPositions }
            }
        }

        positionsFlow
            .onEach {
                val role = authenticationRepository.userRole.firstOrNull() ?: 0
                val isAdmin = role >= UserRole.ADMIN.ordinal
                val isLeader = role >= UserRole.LEADER.ordinal
                val positions = mutableListOf<MKWarPosition>()
                it.forEach { pos ->
                    positions.add(MKWarPosition(pos, users.singleOrNull { it.mid == pos.playerId }))

                }
                _sharedPositions.emit(positions)
                _sharedButtonsVisible.emit(networkRepository.networkAvailable && (isAdmin.isTrue && !MKWar(war).isOver
                        || isLeader))
            }.launchIn(viewModelScope)

        onEditTrack.bind(_sharedEditTrackClick, viewModelScope)
        onEditPositions.bind(_sharedEditPositionsClick, viewModelScope)
        onEditShocks.bind(_sharedEditShocksClick, viewModelScope)
    }


    fun refresh(warTrack: NewWarTrack) {
        flowOf(Unit)
            .onEach {
                val shocks = mutableListOf<Pair<String?, Shock>>()
               warTrack.shocks?.forEach { shock ->
                    shocks.add(Pair(users.singleOrNull { it.mid == shock.playerId }?.name, Shock(shock.playerId, shock.count)))
                }
                _sharedShocks.emit(shocks)
                val positions = mutableListOf<MKWarPosition>()
                warTrack.warPositions?.forEach { pos ->
                    positions.add(MKWarPosition(pos, users.singleOrNull{it.mid == pos.playerId}))
                }
                _sharedPositions.emit(positions)
                _sharedTrackRefreshed.emit(MKWarTrack(warTrack))
            }.launchIn(viewModelScope)
    }




}