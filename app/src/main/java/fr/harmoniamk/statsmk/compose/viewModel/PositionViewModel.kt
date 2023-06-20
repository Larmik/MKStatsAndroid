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
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
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
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            index: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(index) as T
            }
        }

        @Composable
        fun viewModel(index: Int): PositionViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).positionViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    index = index
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(index: Int): PositionViewModel
    }
    private val _sharedCurrentMap = MutableStateFlow<Maps?>(null)
    private val _sharedWar = MutableStateFlow<MKWar?>(null)
    private val _sharedPlayerLabel = MutableStateFlow<String?>(null)
    private val _sharedPos = MutableStateFlow<Int?>(null)
    private val _sharedSelectedPositions = MutableStateFlow<List<Int>?>(null)
    private val _sharedGoToResult = MutableStateFlow<String?>(null)
    private val _sharedTrackNumber = MutableStateFlow<Int?>(null)

    val sharedCurrentMap = _sharedCurrentMap.asStateFlow()
    val sharedWar = _sharedWar.asStateFlow()
    val sharedPlayerLabel = _sharedPlayerLabel.asStateFlow()
    val sharedSelectedPositions = _sharedSelectedPositions.asStateFlow()
    val sharedGoToResult = _sharedGoToResult.asStateFlow()
    val sharedTrackNumber = _sharedTrackNumber.asStateFlow()

    private val _validateTrack = MutableSharedFlow<Unit>()
    private val _sharedQuit = MutableSharedFlow<Unit>()
    private val _sharedWarName = MutableSharedFlow<String?>()

    val validateTrack = _validateTrack.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()

    var currentUser: User? = null
    var currentUsers: List<User> = listOf()
    val positions = mutableListOf<NewWarPositions>()

    fun onPositionClick(position: Int) {
       val pos = NewWarPositions(mid = System.currentTimeMillis().toString(), position = position, playerId = currentUser?.mid)
        positions.add(pos)
        _sharedSelectedPositions.value = positions.mapNotNull { it.position }
        if (positions.size == currentUsers.size) {
            preferencesRepository.currentWarTrack.apply { this?.warPositions = positions }?.let { newTrack ->
                preferencesRepository.currentWarTrack = newTrack
                val tracks = mutableListOf<NewWarTrack>()
                tracks.addAll(preferencesRepository.currentWar?.warTracks?.filterNot { tr -> tr.mid == newTrack.mid }.orEmpty())
                tracks.add(newTrack)
                preferencesRepository.currentWar = preferencesRepository.currentWar.apply {
                    this?.warTracks = tracks
                }
                _sharedGoToResult.value = newTrack.mid
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
            else -> viewModelScope.launch { _sharedQuit.emit(Unit) }
        }
    }

    init {
        _sharedCurrentMap.value = Maps.values()[index]
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
                }
            }
            ?.flatMapLatest {  databaseRepository.getUsers() }
            ?.onEach {
                currentUsers = it.filter { user -> user.currentWar == _sharedWar.value?.war?.mid }.sortedBy { it.name }
                currentUser = currentUsers.getOrNull(0)
                _sharedPlayerLabel.emit(currentUser?.name)
            }?.launchIn(viewModelScope)
    }

}