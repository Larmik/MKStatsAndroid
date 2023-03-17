package fr.harmoniamk.statsmk.fragment.trackDetails

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
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class TrackDetailsViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface
): ViewModel() {

    private val _sharedPositions = MutableSharedFlow<List<MKWarPosition>>()
    private val _sharedEditTrackClick = MutableSharedFlow<Unit>()
    private val _sharedEditPositionsClick = MutableSharedFlow<Unit>()
    private val _sharedButtonsVisible = MutableSharedFlow<Boolean>()
    private val _sharedTrackRefreshed = MutableSharedFlow<MKWarTrack>()
    private val _sharedWarName = MutableSharedFlow<String?>()
    private val _sharedShocks = MutableSharedFlow<List<Pair<String?, Shock>>>()

    val sharedPositions = _sharedPositions.asSharedFlow()
    val sharedEditTrackClick = _sharedEditTrackClick.asSharedFlow()
    val sharedEditPositionsClick = _sharedEditPositionsClick.asSharedFlow()
    val sharedButtonsVisible = _sharedButtonsVisible.asSharedFlow()
    val sharedTrackRefreshed = _sharedTrackRefreshed.asSharedFlow()
    val sharedWarName = _sharedWarName.asSharedFlow()
    val sharedShocks = _sharedShocks.asSharedFlow()

    private var warId: String = ""
    var index = 0
    private var warTrackId: String = ""
    private val users = mutableListOf<User>()

    fun bind(war: NewWar, warTrack: NewWarTrack?, index: Int, onEditTrack: Flow<Unit>, onEditPositions: Flow<Unit>) {

        warId = war.mid ?: ""
        warTrackId = warTrack?.mid ?: ""
        this.index = index

        firebaseRepository.getUsers()
            .onEach {
                users.clear()
                users.addAll(it)
            }.launchIn(viewModelScope)

        firebaseRepository.getNewWar(warId)
            .onEach {
                _sharedWarName.emit(listOf(MKWar(it)).withName(firebaseRepository).firstOrNull()?.singleOrNull()?.name)
                val shocks = mutableListOf<Pair<String?, Shock>>()
                it?.warTracks?.getOrNull(index)?.shocks?.forEach { shock ->
                    shocks.add(Pair(users.singleOrNull { it.mid == shock.playerId }?.name, Shock(shock.playerId, shock.count)))
                }
                _sharedShocks.emit(shocks)
            }.launchIn(viewModelScope)
        val positionsFlow = when (warTrackId.isEmpty()) {
            true -> flowOf(war.warTracks?.get(index)?.warPositions).filterNotNull()
            else -> firebaseRepository.getPositions(warId, warTrackId)
        }

        positionsFlow
            .onEach {
                val role = authenticationRepository.userRole.firstOrNull() ?: 0
                val isAdmin = role >= UserRole.ADMIN.ordinal
                val isGod = role == UserRole.GOD.ordinal
                val positions = mutableListOf<MKWarPosition>()
                it.forEach { pos ->
                    positions.add(MKWarPosition(pos, users.singleOrNull { it.mid == pos.playerId }))

                }
                _sharedPositions.emit(positions)
                _sharedButtonsVisible.emit(isAdmin.isTrue && !MKWar(war).isOver
                        || isGod)
            }.launchIn(viewModelScope)

        onEditTrack.bind(_sharedEditTrackClick, viewModelScope)
        onEditPositions.bind(_sharedEditPositionsClick, viewModelScope)
    }

    fun refreshTrack() {
        firebaseRepository.getNewWar(warId)
            .mapNotNull { MKWarTrack(it?.warTracks?.get(index)) }
            .onEach { _sharedTrackRefreshed.emit(it) }
            .mapNotNull { it.track?.warPositions }
            .map {
                val positions = mutableListOf<MKWarPosition>()
                it.forEach { pos ->
                    positions.add(MKWarPosition(pos, users.singleOrNull{it.mid == pos.playerId}))
                }
                positions
            }
            .bind(_sharedPositions, viewModelScope)
    }

}