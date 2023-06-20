package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class WarTrackResultViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

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
    private val _sharedShocks = MutableSharedFlow<List<Pair<String?, Shock>>>()
    private val _sharedLoading = MutableStateFlow<Boolean>(false)

    val sharedBack = _sharedBack.asSharedFlow()
    val sharedBackToCurrent = _sharedBackToCurrent.asSharedFlow()
    val sharedShocks = _sharedShocks.asSharedFlow()
    val sharedLoading = _sharedLoading.asStateFlow()

    private val users = mutableListOf<User>()
    private val finalList = mutableListOf<Pair<String?, Shock>>()
    val shocks = mutableMapOf<String?, Int>()


    init {
        _sharedCurrentMap.value = Maps.values()[preferencesRepository.currentWarTrack?.trackIndex ?: 0]
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

        val positions = mutableListOf<MKWarPosition>()

        databaseRepository.getUsers()
            .onEach {
                delay(50)
                users.clear()
                users.addAll(it)
                preferencesRepository.currentWarTrack?.warPositions.orEmpty().sortedBy { it.position }.forEach { pos ->
                    positions.add(MKWarPosition(pos, users.singleOrNull { it.mid == pos.playerId }))
                    shocks[pos.playerId] = 0
                }
                _sharedWarPos.value = positions
                preferencesRepository.currentWarTrack?.takeIf { positions.size == 6 }?.let {
                    _sharedTrack.value = MKWarTrack(it)
                }
            }.launchIn(viewModelScope)

    }

    fun onValid() {
        preferencesRepository.currentWar?.let {war ->
            _sharedLoading.value = true
            viewModelScope.launch {
                if (MKWar(war).isOver) {
                    firebaseRepository.writeNewWar(war).first()
                    firebaseRepository.deleteCurrentWar().first()
                    val mkWar = listOf(MKWar(war)).withName(databaseRepository).first()
                    mkWar.singleOrNull()?.let { databaseRepository.writeWar(it).first() }
                    databaseRepository.getUsers().first().filter { it.currentWar == war.mid }.forEach {
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

    fun bind(onBack: Flow<Unit>, onValid: Flow<Unit>, onShockAdded: Flow<String>, onShockRemoved: Flow<String>) {


        preferencesRepository.currentWarTrack?.let { track ->

            onShockRemoved
                .onEach { id -> shocks[id]?.takeIf { it > 0 }?.let { shocks[id] = it-1 } }
                .map { id ->
                    finalList.clear()
                    shocks.forEach { shock ->
                        shock.takeIf { map -> map.value > 0 }?.let {
                            val name = databaseRepository.getUser(it.key).firstOrNull()?.name
                            finalList.add(Pair(name, Shock(it.key, it.value)))
                        }
                    }
                    finalList
                }
                .onEach {
                    preferencesRepository.currentWarTrack.apply { this?.shocks = finalList.map { it.second } }?.let { newTrack ->
                        preferencesRepository.currentWarTrack = newTrack
                        val tracks = mutableListOf<NewWarTrack>()
                        tracks.addAll(preferencesRepository.currentWar?.warTracks?.filterNot { tr -> tr.mid == newTrack.mid }.orEmpty())
                        tracks.add(newTrack)
                        preferencesRepository.currentWar = preferencesRepository.currentWar.apply {
                            this?.warTracks = tracks
                        }
                    }
                }.bind(_sharedShocks, viewModelScope)

            onShockAdded
                .onEach { id -> shocks[id]?.let { shocks[id] = it+1 } }
                .map { id ->
                    finalList.clear()
                    shocks.forEach { shock ->
                        shock.takeIf { map -> map.value > 0 }?.let {
                            val name = databaseRepository.getUser(it.key).firstOrNull()?.name
                            finalList.add(Pair(name, Shock(it.key, it.value)))
                        }
                    }
                    finalList
                }
                .onEach {
                    val tracks = mutableListOf<NewWarTrack>()
                    tracks.addAll(preferencesRepository.currentWar?.warTracks?.filterNot { tr -> tr.mid == preferencesRepository.currentWarTrack?.mid }.orEmpty())
                    preferencesRepository.currentWarTrack?.apply { this.shocks = finalList.map { it.second } }?.let {
                        tracks.add(it)
                    }
                    preferencesRepository.currentWar = preferencesRepository.currentWar.apply {
                        this?.warTracks = tracks
                    }
                }.bind(_sharedShocks, viewModelScope)

        }
        onBack.bind(_sharedBack, viewModelScope)
    }
}