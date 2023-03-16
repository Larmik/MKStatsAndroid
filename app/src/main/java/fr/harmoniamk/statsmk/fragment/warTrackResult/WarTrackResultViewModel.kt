package fr.harmoniamk.statsmk.fragment.warTrackResult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
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
class WarTrackResultViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedWarPos = MutableSharedFlow<List<MKWarPosition>>()
    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _sharedBackToCurrent = MutableSharedFlow<Unit>()
    private val _sharedGoToWarResume = MutableSharedFlow<MKWar>()
    private val _sharedScore = MutableSharedFlow<MKWarTrack>()

    val sharedWarPos = _sharedWarPos.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedBackToCurrent = _sharedBackToCurrent.asSharedFlow()
    val sharedScore = _sharedScore.asSharedFlow()
    val sharedGoToWarResume = _sharedGoToWarResume.asSharedFlow()

    private val users = mutableListOf<User>()

    fun bind(onBack: Flow<Unit>, onValid: Flow<Unit>) {

        firebaseRepository.getUsers()
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
                    }
                    positions
                }
                .onEach { _sharedWarPos.emit(it) }
                .filter { it.size == 6 }
                .map { MKWarTrack(track) }
                .bind(_sharedScore, viewModelScope)

            onValid
                .mapNotNull { preferencesRepository.currentWar }
                .onEach { war ->
                    firebaseRepository.writeNewWar(war).first()
                    if (MKWar(war).isOver) {
                        firebaseRepository.getUsers().first().filter { it.currentWar == war.mid }.forEach {
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