package fr.harmoniamk.statsmk.fragment.warTrackResult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
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
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarTrackResultViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedWarPos = MutableSharedFlow<List<MKWarPosition>>()
    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _sharedBackToCurrent = MutableSharedFlow<Unit>()
    private val _sharedGoToWarResume = MutableSharedFlow<MKWar>()
    private val _sharedScore = MutableSharedFlow<MKWarTrack>()
    private val _sharedShocks = MutableSharedFlow<List<Pair<String?, Shock>>>()
    private val _sharedLoading = MutableSharedFlow<Boolean>()

    val sharedWarPos = _sharedWarPos.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedBackToCurrent = _sharedBackToCurrent.asSharedFlow()
    val sharedScore = _sharedScore.asSharedFlow()
    val sharedGoToWarResume = _sharedGoToWarResume.asSharedFlow()
    val sharedShocks = _sharedShocks.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()

    private val users = mutableListOf<User>()
    private val finalList = mutableListOf<Pair<String?, Shock>>()

    fun bind(onBack: Flow<Unit>, onValid: Flow<Unit>, onShockAdded: Flow<String>, onShockRemoved: Flow<String>) {
        val shocks = mutableMapOf<String?, Int>()
        databaseRepository.getUsers()
            .onEach {
                users.clear()
                users.addAll(it)
            }.launchIn(viewModelScope)

        preferencesRepository.currentWarTrack?.let { track ->
            flowOf(track.warPositions.orEmpty().sortedBy { it.position })
                .map {
                    delay(100)
                    val positions = mutableListOf<MKWarPosition>()
                    it.forEach { pos ->
                        positions.add(MKWarPosition(pos, users.singleOrNull { it.mid == pos.playerId }))
                        shocks[pos.playerId] = 0
                    }
                    positions
                }
                .onEach { _sharedWarPos.emit(it) }
                .filter { it.size == 6 }
                .map { MKWarTrack(track) }
                .bind(_sharedScore, viewModelScope)

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

            onValid
                .mapNotNull { preferencesRepository.currentWar }
                .onEach { war ->
                    _sharedLoading.emit(true)
                    firebaseRepository.writeNewWar(war).first()
                    if (MKWar(war).isOver) {
                        databaseRepository.getUsers().first().filter { it.currentWar == war.mid }.forEach {
                            val new = it.apply { this.currentWar = "-1" }
                            firebaseRepository.writeUser(new).first()
                        }
                        _sharedGoToWarResume.emit(MKWar(war))
                    } else _sharedBackToCurrent.emit(Unit)
                    preferencesRepository.currentWarTrack = null
                }.launchIn(viewModelScope)
        }
        onBack.bind(_sharedBack, viewModelScope)
    }
}