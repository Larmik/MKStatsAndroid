package fr.harmoniamk.statsmk.features.trackList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.MainApplication
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class TrackListViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedSearchedItems = MutableSharedFlow<List<Maps>>()
    private val _sharedGoToWarPos = MutableSharedFlow<NewWarTrack>()
    private val _sharedGoToTmPos = MutableSharedFlow<Int>()
    private val _sharedQuit = MutableSharedFlow<Unit>()

    val sharedSearchedItems = _sharedSearchedItems.asSharedFlow()
    val sharedGoToWarPos = _sharedGoToWarPos.asSharedFlow()
    val sharedGoToTmPos = _sharedGoToTmPos.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()


    fun bind(tournamentId: Int? = null, warId: String? = null, onTrackAdded: Flow<Int>, onSearch: Flow<String>,  onBack: Flow<Unit>) {

        onBack.bind(_sharedQuit, viewModelScope)
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

        // A optimiser avec boolean à la place d'id (isTournament ou isWar) //
        tournamentId?.let { onTrackAdded.bind(_sharedGoToTmPos, viewModelScope) }

        warId?.let {
            onTrackAdded
                .mapNotNull {
                    val track = NewWarTrack(mid = System.currentTimeMillis().toString(), trackIndex = it)
                    preferencesRepository.currentWarTrack = track
                    track
                }.bind(_sharedGoToWarPos, viewModelScope)

        }
        //////////////////////////////////////////////////////////////////////
    }

}