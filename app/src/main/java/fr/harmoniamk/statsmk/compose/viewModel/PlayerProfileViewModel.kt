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
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.network.MKCFullPlayer
import fr.harmoniamk.statsmk.model.network.MKPlayer
import fr.harmoniamk.statsmk.model.network.NetworkResponse
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip

@FlowPreview
@ExperimentalCoroutinesApi
class PlayerProfileViewModel @AssistedInject constructor(
    @Assisted val id: String,
    mkCentralRepository: MKCentralRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface,
    authenticationRepository: AuthenticationRepositoryInterface
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
        fun viewModel(id: String): PlayerProfileViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).playerProfileViewModel
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
        fun create(id: String): PlayerProfileViewModel
    }

    private val _sharedEmail = MutableStateFlow<String?>(null)
    private val _sharedPlayer = MutableStateFlow<MKCFullPlayer?>(null)
    private val _sharedAllyButton = MutableStateFlow<Pair<String, Boolean>?>(null)
    val sharedEmail = _sharedEmail.asStateFlow()
    val sharedPlayer = _sharedPlayer.asStateFlow()
    val sharedAllyButton = _sharedAllyButton.asStateFlow()
    var player: MKCFullPlayer? = null

    init {
        mkCentralRepository.getPlayer(id)
            .mapNotNull { (it as? NetworkResponse.Success)?.response }
            .filterNotNull()
            .zip(databaseRepository.getNewUser(id)) { fullPlayer, localPlayer ->
                player = fullPlayer
                _sharedPlayer.value = fullPlayer
                _sharedAllyButton.takeIf { authenticationRepository.userRole >= UserRole.ADMIN.ordinal }?.value = when {
                    localPlayer?.mkcId?.isEmpty().isTrue -> Pair(fullPlayer.id.toString(), true)
                    localPlayer?.rosterId == "-1" -> Pair(localPlayer.mkcId, false)
                    else -> null
                }
            }.launchIn(viewModelScope)
    }

    fun onAddAlly() {
      firebaseRepository.writeAlly(player?.id.toString())
            .flatMapLatest { databaseRepository.writeUser(MKPlayer(player)) }
            .onEach { _sharedAllyButton.value = null }
            .launchIn(viewModelScope)
    }

}