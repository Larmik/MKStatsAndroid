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
import fr.harmoniamk.statsmk.compose.ViewModelFactoryProvider
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
class TeamProfileViewModel @AssistedInject constructor(
    @Assisted val id: String,
    private val mkCentralRepository: MKCentralRepositoryInterface
) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            id: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(id) as T
            }
        }

        @Composable
        fun viewModel(id: String): TeamProfileViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).teamProfileViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    id = id
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(id: String): TeamProfileViewModel
    }

    private val _sharedPlayers = MutableStateFlow<List<MKCLightPlayer>>(listOf())
    private val _sharedTeam = MutableStateFlow<MKCFullTeam?>(null)
    private val _sharedPictureLoaded = MutableStateFlow<String?>(null)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)

    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedTeam = _sharedTeam.asStateFlow()
    val sharedPictureLoaded =_sharedPictureLoaded.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asSharedFlow()


    fun init() {
        mkCentralRepository.getTeam(id)
            .onEach {
                _sharedTeam.emit(it)
                _sharedPictureLoaded.emit(it.logoUrl)
                _sharedPlayers.emit(it.rosterList.orEmpty())
            }.launchIn(viewModelScope)
    }

}