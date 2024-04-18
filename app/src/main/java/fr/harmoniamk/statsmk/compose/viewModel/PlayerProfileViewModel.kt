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
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.network.MKCFullPlayer
import fr.harmoniamk.statsmk.model.network.MKCPlayer
import fr.harmoniamk.statsmk.model.network.MKPlayer
import fr.harmoniamk.statsmk.model.network.NetworkResponse
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
class PlayerProfileViewModel @AssistedInject constructor(
    @Assisted val id: String,
    mkCentralRepository: MKCentralRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface
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
    private val _sharedAdminButton = MutableStateFlow<Boolean?>(null)
    private val _sharedRole = MutableStateFlow<String?>(null)
    val sharedEmail = _sharedEmail.asStateFlow()
    val sharedPlayer = _sharedPlayer.asStateFlow()
    val sharedAllyButton = _sharedAllyButton.asStateFlow()
    val sharedAdminButton = _sharedAdminButton.asStateFlow()
    val sharedRole = _sharedRole.asStateFlow()

    var player: MKCFullPlayer? = null
    var localPlayer: MKPlayer? = null


    init {
        mkCentralRepository.getPlayer(id)
            .mapNotNull { (it as? NetworkResponse.Success)?.response }
            .filterNotNull()
            .onEach {
                player = it
                _sharedPlayer.value = it
                _sharedRole.value = null
            }
            .flatMapLatest { databaseRepository.getNewUser(id)  }
            .onEach { localPlayer ->
                this.localPlayer = localPlayer
                _sharedAllyButton.takeIf { authenticationRepository.userRole >= UserRole.ADMIN.ordinal }?.value = when {
                    localPlayer?.mkcId?.isEmpty().isTrue -> Pair(player?.id.toString(), true)
                    localPlayer?.rosterId == "-1" -> Pair(localPlayer.mkcId, false)
                    else -> null
                }
                localPlayer?.takeIf { it.mid.isNotEmpty() && it.mid.toIntOrNull() == null && it.rosterId != "-1" }?.let {player ->
                    _sharedAdminButton
                        .takeIf { authenticationRepository.userRole >= UserRole.LEADER.ordinal && player.role  < 2 }
                        ?.value = when (player.role) {
                            1 -> true
                            0 -> false
                            else -> null
                        }
                    _sharedRole.value = when {
                        player.role == 1 -> "Admin"
                        player.role == 2 -> "Leader"
                        player.role == 3 -> "Dieu"
                        else -> "Membre"
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun onAddAlly() {
        val teamId = preferencesRepository.mkcTeam?.primary_team_id ?: preferencesRepository.mkcTeam?.id
      firebaseRepository.writeAlly(teamId.toString(), player?.id.toString())
            .flatMapLatest { databaseRepository.writeUser(MKPlayer(player).copy(rosterId = "-1")) }
            .onEach { _sharedAllyButton.value = null }
            .launchIn(viewModelScope)
    }

    fun onAdmin(admin: Boolean) {
        val newRole = when (admin) {
            true -> 1
            else -> 0
        }
        localPlayer?.mid?.let {
            firebaseRepository.getUser(it)
                .mapNotNull {  it?.copy(role = newRole) }
                .flatMapLatest { firebaseRepository.writeUser(it) }
                .mapNotNull { localPlayer?.copy(role = newRole) }
                .onEach {
                    _sharedAdminButton.takeIf {
                        authenticationRepository.userRole >= UserRole.LEADER.ordinal
                                && localPlayer?.mid?.toIntOrNull() == null
                                && (localPlayer?.role ?: 0) < 2
                    }?.value = when (localPlayer?.role) {
                        1 -> true
                        0 -> false
                        else -> null
                    }
                    _sharedRole.value = when {
                        it.mid.toIntOrNull() != null -> "Non inscrit"
                        it.role == 1 -> "Admin"
                        it.role == 2 -> "Leader"
                        it.role == 3 -> "Dieu"
                        else -> "Membre"
                    }
                }
                .flatMapLatest { databaseRepository.writeUser(it) }
                .launchIn(viewModelScope)
        }
    }

}