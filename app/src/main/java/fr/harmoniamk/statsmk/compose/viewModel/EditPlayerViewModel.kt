package fr.harmoniamk.statsmk.compose.viewModel

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class EditPlayerViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface
): ViewModel() {

    private val _sharedPlayerIsMember = MutableStateFlow(false)
    private val _sharedPlayer = MutableStateFlow<User?>(null)
    private val _sharedPlayerHasAccount = MutableStateFlow(false)
    private val _sharedEditRoleVisibility = MutableStateFlow(View.INVISIBLE)
    private val _sharedLeaveTeamVisibility = MutableStateFlow(false)
    private val _sharedDismiss = MutableSharedFlow<Unit>()

    val sharedPlayerHasAccount = _sharedPlayerHasAccount.asStateFlow()
    val sharedLeaveTeamVisibility = _sharedLeaveTeamVisibility.asStateFlow()
    val sharedDismiss = _sharedDismiss.asSharedFlow()
    val sharedPlayer = _sharedPlayer.asStateFlow()


    fun refresh(playerId: String) {
        _sharedPlayerHasAccount.value = false
        databaseRepository.getUser(playerId)
            .filterNotNull()
            .zip(authenticationRepository.userRole) { player, userRole ->
                _sharedPlayer.value = player
                _sharedPlayerHasAccount.value = player.mid.toLongOrNull() == null
                _sharedPlayerIsMember.value = player.team == preferencesRepository.currentTeam?.mid
                val role = player.role ?: 0
                _sharedLeaveTeamVisibility.value = player.team == preferencesRepository.currentTeam?.mid && player.mid != authenticationRepository.user?.uid && role < UserRole.LEADER.ordinal
                _sharedEditRoleVisibility.emit(when (userRole >= UserRole.LEADER.ordinal) {
                    true -> View.VISIBLE
                    else -> View.INVISIBLE
                })
            }.launchIn(viewModelScope)
    }

    fun onPlayerEdited(name: String, role: Int?) {
        _sharedPlayer.value?.copy(name = name, role = role)?.let { user ->
            firebaseRepository.writeUser(user)
                .onEach {
                    _sharedDismiss.emit(Unit)
                }.launchIn(viewModelScope)
        }
    }

    fun onRemoveFromTeam(player: User?) {
        viewModelScope.launch {
            val formerTeams = mutableListOf<String?>()
            formerTeams.addAll(player?.formerTeams.orEmpty())
            formerTeams.add(preferencesRepository.currentTeam?.mid)
            player?.formerTeams?.takeIf { it.isNotEmpty() }?.let {
                it.forEach {
                    val wars = firebaseRepository.getNewWars(it)
                        .map { list -> list.map {  MKWar(it) } }
                        .first()
                    val finalList = wars.withName(databaseRepository).first()
                    databaseRepository.writeWars(finalList).first()
                }
            }
            player?.copy(
                team = "-1",
                formerTeams = formerTeams.distinct().filterNotNull(),
                role = UserRole.MEMBER.ordinal
            )?.let {
                firebaseRepository.writeUser(it).bind(_sharedDismiss, viewModelScope)
            }
        }
    }

}