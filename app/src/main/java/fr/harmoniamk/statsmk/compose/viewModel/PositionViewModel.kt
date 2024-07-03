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
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKPlayer
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PositionViewModel @AssistedInject constructor(
    @Assisted private val index: Int,
    @Assisted private val editing: Boolean,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface
) : ViewModel() {

    fun clearPos() {
        positions.clear()
        _sharedSelectedPositions.value = listOf()
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            index: Int,
            editing: Boolean
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(index, editing) as T
            }
        }

        @Composable
        fun viewModel(index: Int, editing: Boolean): PositionViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).positionViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    index = index,
                    editing = editing
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(index: Int, editing: Boolean): PositionViewModel
    }

    private val _sharedCurrentMap = MutableStateFlow<Maps?>(null)
    private val _sharedWar = MutableStateFlow<MKWar?>(null)
    private val _sharedPlayerLabel = MutableStateFlow<String?>(null)
    private val _sharedSelectedPositions = MutableStateFlow<List<Int>?>(null)
    private val _sharedGoToResult = MutableStateFlow<String?>(null)
    private val _sharedTrackNumber = MutableStateFlow<Int?>(null)
    private val _sharedQuit = MutableSharedFlow<Unit>()

    val sharedCurrentMap = _sharedCurrentMap.asStateFlow()
    val sharedWar = _sharedWar.asStateFlow()
    val sharedPlayerLabel = _sharedPlayerLabel.asStateFlow()
    val sharedSelectedPositions = _sharedSelectedPositions.asStateFlow()
    val sharedGoToResult = _sharedGoToResult.asStateFlow()
    val sharedTrackNumber = _sharedTrackNumber.asStateFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()

    private var currentUser: MKPlayer? = null
    private var currentUsers: List<MKPlayer> = listOf()
    private val positions = mutableListOf<NewWarPositions>()

    fun onPositionClick(position: Int) {
       val pos = NewWarPositions(mid = System.currentTimeMillis().toString(), position = position, playerId = currentUser?.mkcId)
        positions.add(pos)
        _sharedSelectedPositions.value = positions.mapNotNull { it.position }
        if (positions.size == currentUsers.size) {
            val warTrack = when (editing) {
                true -> preferencesRepository.currentWar?.warTracks?.getOrNull(index)
                else ->  preferencesRepository.currentWarTrack
            }.apply { this?.warPositions = positions }
           warTrack?.let { newTrack ->
               if (!editing) preferencesRepository.currentWarTrack = newTrack
                val tracks = mutableListOf<NewWarTrack>()
                when (editing) {
                    true ->  {
                        tracks.addAll(preferencesRepository.currentWar?.warTracks?.filterNot { tr -> tr.mid == newTrack.mid }.orEmpty())
                        tracks.add(index, newTrack)
                    }
                    else -> {
                        tracks.addAll(preferencesRepository.currentWar?.warTracks.orEmpty())
                        tracks.add(newTrack)
                    }
                }
                preferencesRepository.currentWar = preferencesRepository.currentWar.apply {
                    this?.warTracks = tracks
                }
                when (editing) {
                    true -> {
                        preferencesRepository.currentWar?.let {
                            firebaseRepository.writeCurrentWar(it)
                                .onEach { positions.clear() }
                                .bind(_sharedQuit, viewModelScope)
                        }
                    }
                    else -> _sharedGoToResult.value = newTrack.mid
                }
            }
        }
        else {
            currentUser = currentUsers.getOrNull(positions.indexOf(pos)+1)
            _sharedPlayerLabel.value = currentUser?.name
        }
    }

    fun onBack() {
        when (positions.isEmpty()) {
            false -> {
                positions.remove(positions.last())
                _sharedSelectedPositions.value = positions.mapNotNull { pos -> pos.position }
                currentUser = currentUsers.getOrNull(positions.size)
                _sharedPlayerLabel.value = currentUser?.name
            }
            else -> viewModelScope.launch {
                preferencesRepository.currentWarTrack = null
                _sharedQuit.emit(Unit)
            }
        }
    }

    init {
        val trackIndexInMapList = when (editing) {
            true -> preferencesRepository.currentWar?.warTracks?.getOrNull(index)?.trackIndex ?: 0
            else -> index
        }
        val currentTrack = when (editing) {
            true -> preferencesRepository.currentWar?.warTracks?.getOrNull(index)
            else -> preferencesRepository.currentWarTrack
        }
        _sharedCurrentMap.value = Maps.entries.toTypedArray()[currentTrack?.trackIndex ?: trackIndexInMapList]
        preferencesRepository.currentWar
            ?.withName(databaseRepository)
            ?.onEach {
                _sharedWar.value = it
                    val trackIndexInWarTracks = when (editing) {
                        true -> index + 1
                        else -> it?.warTracks.orEmpty().size + 1
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
            }?.flatMapLatest { databaseRepository.getPlayers() }
            ?.onEach {
                currentUsers = it.filter { user ->
                    user.currentWar == _sharedWar.value?.war?.mid
                }.sortedBy { it.name }
                currentUser = currentUsers.getOrNull(0)
                _sharedPlayerLabel.emit(currentUser?.name)
            }?.launchIn(viewModelScope)
    }

}