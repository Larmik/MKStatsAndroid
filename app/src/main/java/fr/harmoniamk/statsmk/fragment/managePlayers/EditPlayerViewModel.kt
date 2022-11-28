package fr.harmoniamk.statsmk.fragment.managePlayers

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class EditPlayerViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface): ViewModel() {

    private val _sharedPlayerIsMember = MutableSharedFlow<Boolean>()
    private val _sharedPlayerHasAccount = MutableSharedFlow<Boolean>()
    private val _sharedDeleteVisibility = MutableSharedFlow<Boolean>()
    private val _sharedShowDialog = MutableSharedFlow<Boolean>()
    private val _sharedEditRoleVisibility = MutableSharedFlow<Int>()
    private val _sharedRoleSelected = MutableSharedFlow<Int>()
    private val _sharedUserRoleLabel = MutableSharedFlow<String?>()
    private val _sharedButtonEnabled = MutableSharedFlow<Boolean>()
    private val _sharedIdVisible = MutableSharedFlow<String>()
    val sharedButtonEnabled = _sharedButtonEnabled.asSharedFlow()
    val sharedPlayerIsMember = _sharedPlayerIsMember.asSharedFlow()
    val sharedPlayerHasAccount = _sharedPlayerHasAccount.asSharedFlow()
    val sharedDeleteVisibility = _sharedDeleteVisibility.asSharedFlow()
    val sharedEditRoleVisibility = _sharedEditRoleVisibility.asSharedFlow()
    val sharedRoleSelected = _sharedRoleSelected.asSharedFlow()
    val sharedShowDialog = _sharedShowDialog.asSharedFlow()
    val sharedUserRoleLabel = _sharedUserRoleLabel.asSharedFlow()
    val sharedIdVisible = _sharedIdVisible.asSharedFlow()

    fun bind(player: User, onEditClick: Flow<Unit>, onNameEdited: Flow<String>) {
        onEditClick.onEach { _sharedShowDialog.emit(true) }.launchIn(viewModelScope)
        onNameEdited.map { it.isNotEmpty() }.bind(_sharedButtonEnabled, viewModelScope)

        authenticationRepository.userRole
            .onEach {
                _sharedPlayerHasAccount.emit(player.mid?.toLongOrNull() == null)
                _sharedDeleteVisibility.emit(it == UserRole.GOD.ordinal)
                _sharedEditRoleVisibility.emit(when (it >= UserRole.LEADER.ordinal) {
                    true -> View.VISIBLE
                    else -> View.INVISIBLE
                })
                _sharedUserRoleLabel.emit(when (player.role) {
                    UserRole.MEMBER.ordinal -> "Membre"
                    UserRole.LEADER.ordinal -> "Leader"
                    UserRole.ADMIN.ordinal -> "Admin"
                    UserRole.GOD.ordinal -> "Dieu"
                    else -> null
                })
                if (it == UserRole.GOD.ordinal)
                    _sharedIdVisible.emit(player.mid.orEmpty())
            }.launchIn(viewModelScope)

        flowOf(player.mid)
            .flatMapLatest { firebaseRepository.getUser(it) }
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
        }.launchIn(viewModelScope)
    }

}