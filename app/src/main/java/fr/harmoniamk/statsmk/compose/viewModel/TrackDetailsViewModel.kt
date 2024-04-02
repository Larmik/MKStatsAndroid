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
import fr.harmoniamk.statsmk.compose.ViewModelFactoryProvider
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.Shock
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.network.MKPlayer
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TrackDetailsViewModel @AssistedInject constructor(
    @Assisted("warId") private val warId: String,
    @Assisted("warTrackId") private val warTrackId: String,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
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
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)
    private val _sharedShocks = MutableStateFlow<List<Shock>?>(null)

    val sharedPositions = _sharedPositions.asStateFlow()
    val sharedButtonsVisible = _sharedButtonsVisible.asStateFlow()
    val sharedWar = _sharedWar.asStateFlow()
    val sharedCurrentTrack = _sharedCurrentTrack.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asStateFlow()
    val sharedShocks = _sharedShocks.asStateFlow()

    private var index = 0
    private val users = mutableListOf<MKPlayer>()

    init {
        databaseRepository.getRoster()
            .onEach {
                users.clear()
                users.addAll(it)
            }
            .flatMapLatest {
                when (warId) {
                    "Current" -> preferencesRepository.currentWar?.withName(databaseRepository)!!
                    else -> databaseRepository.getWar(warId)
                }
            }
            .filterNotNull()
            .onEach {
                _sharedWar.value = it
                _sharedCurrentTrack.value = it.warTracks?.singleOrNull { track -> track.track?.mid == warTrackId  }
                val shocks = mutableListOf<Shock>()
                it.war?.warTracks?.singleOrNull {it.mid == warTrackId}?.shocks?.forEach { shock ->
                    shocks.add(Shock(shock.playerId, shock.count))
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
                val isAdmin = authenticationRepository.userRole >= UserRole.ADMIN.ordinal
                val isLeader = authenticationRepository.userRole >= UserRole.LEADER.ordinal
                val positions = mutableListOf<MKWarPosition>()
                it.forEach { pos ->
                    positions.add(MKWarPosition(position = pos, mkcPlayer = users.singleOrNull { it.mkcId == pos.playerId }))
                }
                _sharedPositions.emit(positions.sortedBy { it.position.position })
                _sharedButtonsVisible.value = warId == "Current" && networkRepository.networkAvailable && (isAdmin.isTrue || isLeader)
            }.launchIn(viewModelScope)
    }

    fun onEditTrack() {
        _sharedBottomSheetValue.value = MKBottomSheetState.EditTrack()
    }
    fun onEditPositions() {
        _sharedBottomSheetValue.value = MKBottomSheetState.EditPositions()
    }
    fun onEditShocks() {
        _sharedBottomSheetValue.value = MKBottomSheetState.EditShocks()
    }

    fun refresh(trackIndex: Int) {
        preferencesRepository.currentWar
            .withName(databaseRepository)
            .onEach {
                _sharedWar.value = it
                val warTrack = it?.warTracks?.getOrNull(trackIndex)
                _sharedCurrentTrack.value = warTrack
                val positions = mutableListOf<MKWarPosition>()
                it?.warTracks?.getOrNull(trackIndex)?.track?.warPositions?.forEach { pos ->
                    positions.add(MKWarPosition(position = pos, mkcPlayer = users.singleOrNull{ it.mkcId == pos.playerId }))
                }
                _sharedPositions.value = positions.sortedBy { it.position.position }
                val shocks = mutableListOf<Shock>()
                it?.warTracks?.getOrNull(trackIndex)?.track?.shocks?.forEach { shock ->
                    shocks.add(Shock(shock.playerId, shock.count))
                }
                _sharedShocks.value = shocks
            }.launchIn(viewModelScope)
    }

    fun dismissBottomSheet(trackIndex: Int) {
        _sharedBottomSheetValue.value = null
        refresh(trackIndex)
    }

}