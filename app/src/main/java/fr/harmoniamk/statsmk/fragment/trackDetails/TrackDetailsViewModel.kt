package fr.harmoniamk.statsmk.fragment.trackDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
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
class TrackDetailsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface): ViewModel() {

    private val _sharedPositions = MutableSharedFlow<List<NewWarPositions>>()
    private val _sharedEditTrackClick = MutableSharedFlow<Unit>()
    private val _sharedButtonsVisible = MutableSharedFlow<Boolean>()
    val sharedPositions = _sharedPositions.asSharedFlow()
    val sharedEditTrackClick = _sharedEditTrackClick.asSharedFlow()
    val sharedButtonsVisible = _sharedButtonsVisible.asSharedFlow()

    fun bind(track: NewWarTrack, onEditTrack: Flow<Unit>, onEditPositions: Flow<Unit>) {

        flowOf(track.warPositions)
            .filterNotNull()
            .onEach {
                delay(20)
                _sharedPositions.emit(it)
                _sharedButtonsVisible.emit(preferencesRepository.currentUser?.isAdmin.isTrue)
            }.launchIn(viewModelScope)

        onEditTrack.bind(_sharedEditTrackClick, viewModelScope)


    }

}