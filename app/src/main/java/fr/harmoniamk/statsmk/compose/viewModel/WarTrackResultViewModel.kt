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
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.firebase.Shock
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class WarTrackResultViewModel @AssistedInject constructor(
    @Assisted val trackResultIndex: Int?,
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            trackResultIndex: Int?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(trackResultIndex) as T
            }
        }

        @Composable
        fun viewModel(trackResultIndex: Int?): WarTrackResultViewModel {
            val factory: Factory =
                EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).warTrackResultViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    trackResultIndex = trackResultIndex
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(trackResultIndex: Int?): WarTrackResultViewModel
    }


    private val _sharedWarPos = MutableStateFlow<List<MKWarPosition>?>(null)
    private val _sharedTrack = MutableStateFlow<MKWarTrack?>(null)
    private val _sharedWar = MutableStateFlow<MKWar?>(null)
    private val _sharedTrackNumber = MutableStateFlow<Int?>(null)
    private val _sharedCurrentMap = MutableStateFlow<Maps?>(null)
    private val _sharedGoToWarResume = MutableStateFlow<String?>(null)

    val sharedWarPos = _sharedWarPos.asStateFlow()
    val sharedTrack = _sharedTrack.asStateFlow()
    val sharedWar = _sharedWar.asStateFlow()
    val sharedTrackNumber = _sharedTrackNumber.asStateFlow()
    val sharedCurrentMap = _sharedCurrentMap.asStateFlow()
    val sharedGoToWarResume = _sharedGoToWarResume.asStateFlow()

    val positions = mutableListOf<MKWarPosition>()

    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _sharedBackToCurrent = MutableSharedFlow<Unit>()
    private val _sharedShocks = MutableStateFlow<List<Shock>?>(null)
    private val _sharedLoading = MutableStateFlow<Boolean>(false)

    val sharedBack = _sharedBack.asSharedFlow()
    val sharedBackToCurrent = _sharedBackToCurrent.asSharedFlow()
    val sharedShocks = _sharedShocks.asStateFlow()
    val sharedLoading = _sharedLoading.asStateFlow()

    private val users = mutableListOf<User>()
    private val finalList = mutableListOf<Shock>()
    val shocks = mutableMapOf<String?, Int>()


    init {
        val currentTrack =
            preferencesRepository.currentWar?.warTracks?.getOrNull(trackResultIndex ?: 0)
                ?: preferencesRepository.currentWarTrack
        _sharedCurrentMap.value = Maps.values()[currentTrack?.trackIndex ?: 0]
        preferencesRepository.currentWar
            ?.withName(databaseRepository)
            ?.onEach {
                _sharedWar.value = it
                it.warTracks?.let { list ->
                    _sharedTrackNumber.value = when (list.size + 1) {
                        12 -> R.string.track_12
                        11 -> R.string.track_11
                        10 -> R.string.track_10
                        9 -> R.string.track_9
                        8 -> R.string.track_8
                        7 -> R.string.track_7
                        6 -> R.string.track_6
                        5 -> R.string.track_5
                        4 -> R.string.track_4
                        3 -> R.string.track_3
                        2 -> R.string.track_2
                        else -> R.string.track_1
                    }
                } }
            ?.launchIn(viewModelScope)


        databaseRepository.getUsers()
            .onEach {
                delay(50)
                users.clear()
                users.addAll(it)
                refreshPos(initShockList = true)
                currentTrack?.takeIf { positions.size == 6 }?.let {
                    _sharedTrack.value = MKWarTrack(it)
                }
            }.launchIn(viewModelScope)

    }

    private fun refreshPos(initShockList: Boolean = false) {
        positions.clear()
        val currentTrack =
            preferencesRepository.currentWar?.warTracks?.getOrNull(trackResultIndex ?: 0)
                ?: preferencesRepository.currentWarTrack
        currentTrack?.warPositions.orEmpty().sortedBy { it.position }.forEach { pos ->
            val shocksForPlayer = currentTrack?.shocks.orEmpty().singleOrNull { it.playerId == pos.playerId }?.count
            positions.add(MKWarPosition(pos, users.singleOrNull { it.mid == pos.playerId }))
            if (initShockList) shocks[pos.playerId] = 0
            else if (trackResultIndex != null) shocks[pos.playerId] = shocksForPlayer ?: 0
        }
        val newPositions = mutableListOf<MKWarPosition>()
        newPositions.addAll(positions)
        _sharedWarPos.value = newPositions
    }

    fun onValid() {
        preferencesRepository.currentWar?.let { war ->
            _sharedLoading.value = true
            viewModelScope.launch {
                if (MKWar(war).isOver) {
                    firebaseRepository.writeNewWar(war).first()
                    firebaseRepository.deleteCurrentWar().first()
                    val mkWar = listOf(MKWar(war)).withName(databaseRepository).first()
                    mkWar.singleOrNull()?.let { databaseRepository.writeWar(it).first() }
                    databaseRepository.getUsers().first().filter { it.currentWar == war.mid }
                        .forEach {
                            val new = it.apply { this.currentWar = "-1" }
                            firebaseRepository.writeUser(new).first()
                        }
                    _sharedGoToWarResume.value = war.mid
                } else {
                    firebaseRepository.writeCurrentWar(war).first()
                    _sharedBackToCurrent.emit(Unit)
                }
            }
            preferencesRepository.currentWarTrack = null
        }
    }

    fun onAddShock(id: String) {
        shocks[id]?.let { shocks[id] = it + 1 }
        finalList.clear()
        shocks.forEach { shock ->
            shock.takeIf { map -> map.value > 0 }?.let {
                finalList.add(Shock(it.key, it.value))
            }
        }
        val tracks = mutableListOf<NewWarTrack>()
        val currentTrack =
            preferencesRepository.currentWar?.warTracks?.getOrNull(trackResultIndex ?: 0)
                ?: preferencesRepository.currentWarTrack
        tracks.addAll(preferencesRepository.currentWar?.warTracks?.filterNot { tr -> tr.mid == currentTrack?.mid }
            .orEmpty())

        currentTrack?.apply { this.shocks = finalList }?.let {
            tracks.add(trackResultIndex ?: tracks.size, it)
        }
        preferencesRepository.currentWar = preferencesRepository.currentWar.apply {
            this?.warTracks = tracks
        }
        _sharedShocks.value = finalList
        refreshPos(initShockList = false)

    }

    fun onRemoveShock(id: String) {
        shocks[id]?.takeIf { it > 0 }?.let { shocks[id] = it - 1 }
        finalList.clear()
        shocks.forEach { shock ->
            shock.takeIf { map -> map.value > 0 }?.let {
                finalList.add(Shock(it.key, it.value))
            }
        }
        val currentTrack =
            preferencesRepository.currentWar?.warTracks?.getOrNull(trackResultIndex ?: 0)
                ?: preferencesRepository.currentWarTrack
        currentTrack.apply { this?.shocks = finalList }?.let { newTrack ->
            preferencesRepository.currentWarTrack = newTrack
            val tracks = mutableListOf<NewWarTrack>()
            tracks.addAll(preferencesRepository.currentWar?.warTracks?.filterNot { tr -> tr.mid == newTrack.mid }
                .orEmpty())
            tracks.add(trackResultIndex ?: tracks.size, newTrack)
            preferencesRepository.currentWar = preferencesRepository.currentWar.apply {
                this?.warTracks = tracks
            }
        }
        _sharedShocks.value = finalList
        refreshPos(initShockList = false)

    }
}