package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import fr.harmoniamk.statsmk.application.MainApplication
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TrackListViewModel @AssistedInject constructor(@Assisted private val editing: Boolean, private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            editing: Boolean
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(editing) as T
            }
        }

        @Composable
        fun viewModel(editing: Boolean): TrackListViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).trackListViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    editing = editing
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(editing: Boolean): TrackListViewModel
    }

    private val _sharedSearchedItems = MutableStateFlow(Maps.values().toList())
    private val _sharedQuit = MutableSharedFlow<Unit>()
    private val _sharedCurrentWar = MutableStateFlow<MKWar?>(null)

    val sharedSearchedItems = _sharedSearchedItems.asStateFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asStateFlow()
    init {
        firebaseRepository.takeIf { editing }?.listenToCurrentWar()?.bind(_sharedCurrentWar, viewModelScope)
    }

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

    fun editTrack(war: NewWar?, indexInList: Int, newTrackIndex: Int) {
        flowOf(war?.warTracks?.get(indexInList))
            .filterNotNull()
            .map { track ->
                val newTrackList: MutableList<NewWarTrack>? = war?.warTracks?.toMutableList()
                newTrackList?.remove(track)
                newTrackList?.add(indexInList, track.apply { this.trackIndex = newTrackIndex })
                war?.apply { this.warTracks = newTrackList }
            }
            .filterNotNull()
            .onEach { preferencesRepository.currentWar = it }
            .flatMapLatest { firebaseRepository.writeCurrentWar(it) }
            .bind(_sharedQuit, viewModelScope)
    }

}