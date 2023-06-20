package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.application.MainApplication
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TrackListViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedSearchedItems = MutableStateFlow(Maps.values().toList())
    private val _sharedQuit = MutableSharedFlow<Unit>()

    val sharedSearchedItems = _sharedSearchedItems.asStateFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()

    fun search(searched: String) {
        _sharedSearchedItems.value = Maps.values().filter {
            it.name.toLowerCase(Locale.ROOT)
                .contains(searched.toLowerCase(Locale.ROOT)) || MainApplication.instance?.applicationContext?.getString(
                it.label
            )?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)) ?: true
        }
    }

    fun addTrack(index: Int) {
        preferencesRepository.currentWarTrack = NewWarTrack(mid = System.currentTimeMillis().toString(), trackIndex = index)
    }

}