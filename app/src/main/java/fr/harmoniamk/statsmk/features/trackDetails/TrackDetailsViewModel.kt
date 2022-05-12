package fr.harmoniamk.statsmk.features.trackDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TrackDetailsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface): ViewModel() {

    private val _sharedPositions = MutableSharedFlow<List<NewWarPositions>>()
    val sharedPositions = _sharedPositions.asSharedFlow()

    fun bind(track: NewWarTrack) {
        flowOf(track.warPositions)
            .filterNotNull()
            .onEach { delay(20) }
            .bind(_sharedPositions, viewModelScope)
    }

}