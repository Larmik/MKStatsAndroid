package fr.harmoniamk.statsmk.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.model.PlayedTrack
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.PlayedTrackRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class EditCurrentTrackViewModel @Inject constructor(private val playedTrackRepository: PlayedTrackRepositoryInterface) : ViewModel() {

    private val _sharedTrack = MutableSharedFlow<PlayedTrack>()
    private val _sharedTrackClick = MutableSharedFlow<Unit>()
    private val _sharedPositionClick = MutableSharedFlow<Unit>()
    val sharedTrack = _sharedTrack.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedPositionClick = _sharedPositionClick.asSharedFlow()

    fun bind(trackId: Int, onTrackClick: Flow<Unit>, onPositionClick: Flow<Unit>) {
        onTrackClick.bind(_sharedTrackClick, viewModelScope)
        onPositionClick.bind(_sharedPositionClick, viewModelScope)
        playedTrackRepository.getById(trackId).bind(_sharedTrack, viewModelScope)
    }

}