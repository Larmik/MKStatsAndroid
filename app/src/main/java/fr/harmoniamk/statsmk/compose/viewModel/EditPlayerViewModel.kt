package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class EditPlayerViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface
): ViewModel() {

    private val _sharedPlayer = MutableStateFlow<User?>(null)
    private val _sharedPlayerHasAccount = MutableStateFlow(false)
    private val _sharedDismiss = MutableSharedFlow<Unit>()

    val sharedPlayerHasAccount = _sharedPlayerHasAccount.asStateFlow()
    val sharedDismiss = _sharedDismiss.asSharedFlow()
    val sharedPlayer = _sharedPlayer.asStateFlow()

    private var user: MKCLightPlayer? = null

    fun refresh(playerId: String) {
        firebaseRepository.getUser(playerId)
            .filterNotNull()
            .onEach { _sharedPlayer.value = it }
            .flatMapLatest { databaseRepository.getNewUser(it.mkcId) }
            .onEach { user = it }
            .launchIn(viewModelScope)
    }

    fun onPlayerEdited(name: String, role: Int?) {
        _sharedPlayer.value?.copy(name = name, role = role)?.let { player ->
            firebaseRepository.writeUser(User(user, player.mid, player.discordId))
                .mapNotNull { user }
                .flatMapLatest { databaseRepository.writeUser(it) }
                .launchIn(viewModelScope)
        }
    }

}