package fr.harmoniamk.statsmk.features.wTrackResult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarTrackResultViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedWarPos = MutableSharedFlow<List<NewWarPositions>>()
    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _sharedBackToCurrent = MutableSharedFlow<Unit>()
    private val _sharedGoToWarResume = MutableSharedFlow<MKWar>()
    private val _sharedScore = MutableSharedFlow<MKWarTrack>()

    val sharedWarPos = _sharedWarPos.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedBackToCurrent = _sharedBackToCurrent.asSharedFlow()
    val sharedScore = _sharedScore.asSharedFlow()
    val sharedGoToWarResume = _sharedGoToWarResume.asSharedFlow()

    fun bind(warTrackId: String? = null, onBack: Flow<Unit>, onValid: Flow<Unit>) {
        warTrackId?.let { id ->

            preferencesRepository.currentWar?.warTracks?.singleOrNull { it.mid == id }?.let { track ->
                flowOf(track.warPositions.orEmpty().sortedBy { it.position })
                    .onEach { _sharedWarPos.emit(it) }
                    .filter { it.size == 6 }
                    .map { MKWarTrack(track) }
                    .bind(_sharedScore, viewModelScope)

                onValid
                    .mapNotNull {
                        val trackList = mutableListOf<NewWarTrack>()
                        trackList.addAll(preferencesRepository.currentWar?.warTracks.orEmpty())
                        trackList.add(track)
                        preferencesRepository.currentWar.apply {
                            this?.warTracks = trackList
                        }
                    }
                    .onEach { war ->
                        preferencesRepository.currentWar = war
                        firebaseRepository.writeNewWar(war).first()
                        if (MKWar(war).isOver) {
                            firebaseRepository.getUsers().first().filter { it.currentWar == war.mid }.forEach {
                                val new = it.apply { this.currentWar = "-1" }
                                firebaseRepository.writeUser(new).first()
                            }
                            _sharedGoToWarResume.emit(MKWar(war))
                        } else _sharedBackToCurrent.emit(Unit)
                    }.launchIn(viewModelScope)
            }
            onBack.bind(_sharedBack, viewModelScope)
        }
    }
}