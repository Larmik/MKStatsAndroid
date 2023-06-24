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
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.firebase.Shock
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TrackDetailsViewModel @AssistedInject constructor(
    @Assisted("warId") private val warId: String,
    @Assisted("warTrackId") private val warTrackId: String,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface
): ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            warId: String?,
            warTrackId: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(warId, warTrackId) as T
            }
        }

        @Composable
        fun viewModel(warId: String?, warTrackId: String?): TrackDetailsViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).trackDetailsViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    warId = warId,
                    warTrackId = warTrackId
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("warId") warId: String?, @Assisted("warTrackId") warTrackId: String?): TrackDetailsViewModel
    }

    private val _sharedPositions = MutableStateFlow<List<MKWarPosition>?>(null)
    private val _sharedButtonsVisible = MutableStateFlow(false)
    private val _sharedWar = MutableStateFlow<MKWar?>(null)
    private val _sharedCurrentTrack = MutableStateFlow<MKWarTrack?>(null)
    private val _sharedBottomSheetValue = MutableSharedFlow<String?>()

    val sharedPositions = _sharedPositions.asStateFlow()
    val sharedButtonsVisible = _sharedButtonsVisible.asStateFlow()
    val sharedWar = _sharedWar.asStateFlow()
    val sharedCurrentTrack = _sharedCurrentTrack.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asSharedFlow()




    private val _sharedEditTrackClick = MutableSharedFlow<Unit>()
    private val _sharedEditPositionsClick = MutableSharedFlow<Unit>()
    private val _sharedEditShocksClick = MutableSharedFlow<Unit>()
    private val _sharedTrackRefreshed = MutableSharedFlow<MKWarTrack>()
    private val _sharedShocks = MutableSharedFlow<List<Pair<String?, Shock>>>()

    val sharedEditTrackClick = _sharedEditTrackClick.asSharedFlow()
    val sharedEditPositionsClick = _sharedEditPositionsClick.asSharedFlow()
    val sharedEditShocksClick = _sharedEditShocksClick.asSharedFlow()
    val sharedTrackRefreshed = _sharedTrackRefreshed.asSharedFlow()
    val sharedShocks = _sharedShocks.asSharedFlow()

    var index = 0
    private val users = mutableListOf<User>()

    init {
        databaseRepository.getUsers()
            .onEach {
                users.clear()
                users.addAll(it)
            }
            .flatMapLatest { databaseRepository.getWar(warId) }
            .filterNotNull()
            .onEach {
                _sharedWar.value = it
                _sharedCurrentTrack.value = it.warTracks?.singleOrNull { track -> track.track?.mid == warTrackId  }
                val shocks = mutableListOf<Pair<String?, Shock>>()
                it.war?.warTracks?.singleOrNull {it.mid == warTrackId}?.shocks?.forEach { shock ->
                    shocks.add(Pair(users.singleOrNull { it.mid == shock.playerId }?.name, Shock(shock.playerId, shock.count)))
                }
                _sharedShocks.emit(shocks)
            }
            .flatMapLatest {
                when (warTrackId.isEmpty()) {
                    true -> flowOf(it.warTracks?.get(index)?.track?.warPositions).filterNotNull()
                    else -> flowOf(it.warTracks?.singleOrNull { track -> track.track?.mid == warTrackId }?.track?.warPositions).filterNotNull()
                }
            }
            .onEach {
                val role = authenticationRepository.userRole.firstOrNull() ?: 0
                val isAdmin = role >= UserRole.ADMIN.ordinal
                val isLeader = role >= UserRole.LEADER.ordinal
                val positions = mutableListOf<MKWarPosition>()
                it.forEach { pos ->
                    positions.add(MKWarPosition(pos, users.singleOrNull { it.mid == pos.playerId }))
                }
                _sharedPositions.emit(positions.sortedBy { it.position.position })
                _sharedButtonsVisible.value = networkRepository.networkAvailable && (isAdmin.isTrue || isLeader)
            }.launchIn(viewModelScope)
    }

    fun bind(war: NewWar, warTrack: NewWarTrack?, index: Int, onEditTrack: Flow<Unit>, onEditPositions: Flow<Unit>, onEditShocks: Flow<Unit>) {
        this.index = index
        onEditTrack.bind(_sharedEditTrackClick, viewModelScope)
        onEditPositions.bind(_sharedEditPositionsClick, viewModelScope)
        onEditShocks.bind(_sharedEditShocksClick, viewModelScope)
    }

    fun onEditTrack() {
        viewModelScope.launch {
            _sharedBottomSheetValue.emit("EditTrack")

        }
    }
    fun onEditPositions() {

        viewModelScope.launch {
            _sharedBottomSheetValue.emit("EditPositions")
        }

    }
    fun onEditShocks() {

        viewModelScope.launch {
            _sharedBottomSheetValue.emit("EditShocks")

        }
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