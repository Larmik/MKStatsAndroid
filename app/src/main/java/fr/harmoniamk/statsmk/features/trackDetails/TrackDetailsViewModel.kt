package fr.harmoniamk.statsmk.features.trackDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.model.WarPosition
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class TrackDetailsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface): ViewModel() {

    private val _sharedPositions = MutableSharedFlow<List<WarPosition>>()
    val sharedPositions = _sharedPositions.asSharedFlow()

    fun bind(warTrackId: String?) {
        firebaseRepository.getWarPositions()
            .map { it.filter { position -> position.warTrackId == warTrackId }.sortedBy { it.position } }
            .bind(_sharedPositions, viewModelScope)
    }

}