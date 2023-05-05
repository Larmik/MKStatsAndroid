package fr.harmoniamk.statsmk.fragment.settings.managePlayers

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class EditPlayerViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
): ViewModel() {

    private val _sharedPlayerIsMember = MutableSharedFlow<Boolean>()
    private val _sharedPlayerHasAccount = MutableSharedFlow<Boolean>()
    private val _sharedDeleteVisibility = MutableSharedFlow<Boolean>()
    private val _sharedShowDialog = MutableSharedFlow<Boolean>()
    private val _sharedEditRoleVisibility = MutableSharedFlow<Int>()
    private val _sharedLeaveTeamVisibility = MutableSharedFlow<Boolean>()
    private val _sharedRoleSelected = MutableSharedFlow<Int>()
    private val _sharedUserRoleLabel = MutableSharedFlow<String?>()
    private val _sharedButtonEnabled = MutableSharedFlow<Boolean>()

    val sharedButtonEnabled = _sharedButtonEnabled.asSharedFlow()
    val sharedPlayerIsMember = _sharedPlayerIsMember.asSharedFlow()
    val sharedPlayerHasAccount = _sharedPlayerHasAccount.asSharedFlow()
    val sharedDeleteVisibility = _sharedDeleteVisibility.asSharedFlow()
    val sharedEditRoleVisibility = _sharedEditRoleVisibility.asSharedFlow()
    val sharedLeaveTeamVisibility = _sharedLeaveTeamVisibility.asSharedFlow()
    val sharedRoleSelected = _sharedRoleSelected.asSharedFlow()
    val sharedShowDialog = _sharedShowDialog.asSharedFlow()
    val sharedUserRoleLabel = _sharedUserRoleLabel.asSharedFlow()

    fun bind(player: User, onEditClick: Flow<Unit>, onNameEdited: Flow<String>) {
        onEditClick.onEach { _sharedShowDialog.emit(true) }.launchIn(viewModelScope)
        onNameEdited.map { it.isNotEmpty() }.bind(_sharedButtonEnabled, viewModelScope)

        authenticationRepository.userRole
            .onEach { userRole ->
                _sharedPlayerHasAccount.emit(player.mid.toLongOrNull() == null)
                _sharedDeleteVisibility.emit(userRole == UserRole.GOD.ordinal)
                player.role?.let {
                    _sharedLeaveTeamVisibility.emit(player.mid != authenticationRepository.user?.uid && it < UserRole.LEADER.ordinal)
                }

                _sharedEditRoleVisibility.emit(when (userRole >= UserRole.LEADER.ordinal) {
                    true -> View.VISIBLE
                    else -> View.INVISIBLE
                })
                _sharedUserRoleLabel.emit(when (player.role) {
                    UserRole.MEMBER.ordinal -> "Membre"
                    UserRole.LEADER.ordinal -> "Leader"
                    UserRole.ADMIN.ordinal -> "Admin"
                    UserRole.GOD.ordinal -> "Leader"
                    else -> null
                })
            }.launchIn(viewModelScope)

        flowOf(player.mid)
            .flatMapLatest { databaseRepository.getUser(it) }
            .mapNotNull { it?.team == preferencesRepository.currentTeam?.mid }
            .bind(_sharedPlayerIsMember, viewModelScope)
    }

    fun bindDialog(onRoleSelected: Flow<Int>) {
        onRoleSelected.onEach {
            _sharedShowDialog.emit(false)
            _sharedRoleSelected.emit(it)
            _sharedUserRoleLabel.emit(when (it) {
                UserRole.MEMBER.ordinal -> "Membre"
                UserRole.LEADER.ordinal -> "Leader"
                UserRole.ADMIN.ordinal -> "Admin"
                else -> null
            })
            _sharedButtonEnabled.emit(true)
        }.launchIn(viewModelScope)
    }

}