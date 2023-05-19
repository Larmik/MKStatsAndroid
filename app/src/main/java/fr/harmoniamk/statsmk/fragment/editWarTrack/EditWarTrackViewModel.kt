package fr.harmoniamk.statsmk.fragment.editWarTrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.application.MainApplication
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class EditWarTrackViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    private val _sharedDismiss = MutableSharedFlow<Int>()
    private val _sharedSearchedItems = MutableSharedFlow<List<Maps>>()

    val sharedDismiss = _sharedDismiss.asSharedFlow()
    val sharedSearchedItems = _sharedSearchedItems.asSharedFlow()

    fun bind(war: NewWar, index: Int, onTrackEdited: Flow<Int>, onSearch: Flow<String>) {
        var trackIndex = -1
        onTrackEdited
            .mapNotNull {
                trackIndex = it
                war.warTracks?.get(index)?.let { track ->
                    val newTrackList: MutableList<NewWarTrack>? = war.warTracks?.toMutableList()
                    newTrackList?.remove(track)
                    newTrackList?.add(index, track.apply { this.trackIndex = it })
                    war.apply { this.warTracks = newTrackList }
                }
            }.flatMapLatest { firebaseRepository.writeNewWar(it) }
            .onEach { _sharedDismiss.emit(trackIndex) }
            .launchIn(viewModelScope)

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
    }

}