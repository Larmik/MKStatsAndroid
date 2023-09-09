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
import fr.harmoniamk.statsmk.compose.ViewModelFactoryProvider
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
    @Assisted val trackResultIndex: Int,
    @Assisted val editing: Boolean,
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            trackResultIndex: Int, editing: Boolean
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(trackResultIndex, editing) as T
            }
        }

        @Composable
        fun viewModel(trackResultIndex: Int, editing: Boolean): WarTrackResultViewModel {
            val factory: Factory =
                EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).warTrackResultViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    trackResultIndex = trackResultIndex,
                    editing = editing
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(trackResultIndex: Int, editing: Boolean): WarTrackResultViewModel
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


    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _sharedBackToCurrent = MutableSharedFlow<Unit>()
    private val _sharedShocks = MutableStateFlow<List<Shock>?>(null)
    private val _sharedLoading = MutableStateFlow<Boolean>(false)

    val sharedBack = _sharedBack.asSharedFlow()
    val sharedBackToCurrent = _sharedBackToCurrent.asSharedFlow()
    val sharedShocks = _sharedShocks.asStateFlow()
    val sharedLoading = _sharedLoading.asStateFlow()
    val positions = mutableListOf<MKWarPosition>()


    private val users = mutableListOf<User>()
    private val finalList = mutableListOf<Shock>()
    val shocks = mutableMapOf<String?, Int>()


    init {
        val trackIndexInMapList = when (editing) {
            true -> preferencesRepository.currentWar?.warTracks?.getOrNull(trackResultIndex)?.trackIndex ?: 0
            else -> trackResultIndex
        }
        val currentTrack = preferencesRepository.currentWar?.warTracks?.getOrNull(trackResultIndex) ?: preferencesRepository.currentWarTrack

        _sharedCurrentMap.value = Maps.values()[currentTrack?.trackIndex ?: trackIndexInMapList]
        preferencesRepository.currentWar
            ?.withName(databaseRepository)
            ?.onEach {
                users.clear()
                users.addAll(databaseRepository.getUsers().first())
                _sharedWar.value = it
                it.warTracks?.let { list ->
                    val trackIndexInWarTracks = when (editing) {
                        true -> trackResultIndex + 1
                        else -> list.size + 1
                    }
                    _sharedTrackNumber.value = when (trackIndexInWarTracks) {
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
                    makeShockList()
                    refreshPos()
                    currentTrack?.takeIf { positions.size == 6 }?.let {
                        _sharedTrack.value = MKWarTrack(it)
                    }
                } }
            ?.launchIn(viewModelScope)


    }

    private fun refreshPos() {
        positions.clear()
        val currentTrack = preferencesRepository.currentWar?.warTracks?.getOrNull(trackResultIndex) ?: preferencesRepository.currentWarTrack
        currentTrack?.warPositions.orEmpty().sortedBy { it.position }.forEach { pos ->
            val shocksForPlayer = shocks[pos.playerId] ?: currentTrack?.shocks.orEmpty().singleOrNull { it.playerId == pos.playerId }?.count
            positions.add(MKWarPosition(pos, users.singleOrNull { it.mid == pos.playerId }))
            shocks[pos.playerId] = shocksForPlayer ?: 0
        }
        val newPositions = mutableListOf<MKWarPosition>()
        newPositions.addAll(positions)
        _sharedWarPos.value = newPositions
        _sharedShocks.value = finalList
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
        makeShockList()
    }

    fun onRemoveShock(id: String) {
        shocks[id]?.takeIf { it > 0 }?.let { shocks[id] = it - 1 }
        makeShockList()
    }

    private fun makeShockList() {
        finalList.clear()
        shocks.forEach { finalList.add(Shock(it.key, it.value)) }
        val tracks = mutableListOf<NewWarTrack>()
        val currentTrack = preferencesRepository.currentWar?.warTracks?.getOrNull(trackResultIndex) ?: preferencesRepository.currentWarTrack
        tracks.addAll(preferencesRepository.currentWar?.warTracks?.filterNot { tr -> tr.mid == currentTrack?.mid }
            .orEmpty())

        currentTrack?.apply { this.shocks = finalList }?.let {
            when (editing) {
                true -> tracks.add(trackResultIndex, it)
                else -> tracks.add(it)
            }
        }
        preferencesRepository.currentWar = preferencesRepository.currentWar.apply { this?.warTracks = tracks }
        refreshPos()
    }
}