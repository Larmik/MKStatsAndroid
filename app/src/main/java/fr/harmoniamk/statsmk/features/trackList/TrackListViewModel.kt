package fr.harmoniamk.statsmk.features.trackList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.MainApplication
import fr.harmoniamk.statsmk.database.firebase.model.WarTrack
import fr.harmoniamk.statsmk.database.room.model.PlayedTrack
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PlayedTrackRepositoryInterface
import fr.harmoniamk.statsmk.repository.TournamentRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class TrackListViewModel @Inject constructor(private val playedTrackRepository: PlayedTrackRepositoryInterface,
                                             private val tournamentRepository: TournamentRepositoryInterface,
                                             private val firebaseRepository: FirebaseRepositoryInterface
) : ViewModel() {

    private val _sharedSearchedItems = MutableSharedFlow<List<Maps>>()
    private val _sharedGoToWarPos = MutableSharedFlow<WarTrack>()
    private val _sharedGoToTmPos = MutableSharedFlow<Int>()

    val sharedSearchedItems = _sharedSearchedItems.asSharedFlow()
    val sharedGoToWarPos = _sharedGoToWarPos.asSharedFlow()
    val sharedGoToTmPos = _sharedGoToTmPos.asSharedFlow()

    fun bind(tournamentId: Int? = null, warId: String? = null, onTrackAdded: Flow<Int>, onSearch: Flow<String>) {
        var chosenTrack: WarTrack? = null

        onSearch
            .map { searched ->
                Maps.values().filter {
                    it.name.toLowerCase(Locale.ROOT)
                        .contains(searched.toLowerCase(Locale.ROOT)) || MainApplication.instance?.applicationContext?.getString(
                        it.label
                    )?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)) ?: true
                }
            }
            .bind(_sharedSearchedItems, viewModelScope)

        tournamentId?.let {
            onTrackAdded.onEach {
                _sharedGoToTmPos.emit(it)
            }.launchIn(viewModelScope)
        }

        warId?.let { id ->
            onTrackAdded
                .map { WarTrack(mid = System.currentTimeMillis().toString(), warId = id, trackIndex = it) }
                .onEach { chosenTrack = it }
                .flatMapLatest {
                    firebaseRepository.writeWarTrack(it)
                }
                .mapNotNull { chosenTrack }
                .bind(_sharedGoToWarPos, viewModelScope)
        }

    }

}