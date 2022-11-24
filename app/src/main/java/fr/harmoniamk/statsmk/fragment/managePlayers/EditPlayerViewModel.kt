package fr.harmoniamk.statsmk.fragment.managePlayers

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
    val sharedPlayerIsMember = _sharedPlayerIsMember.asSharedFlow()
    val sharedPlayerHasAccount = _sharedPlayerHasAccount.asSharedFlow()
    val sharedDeleteVisibility = _sharedDeleteVisibility.asSharedFlow()

    fun bind(player: User) {

        authenticationRepository.userRole
            .onEach { _sharedPlayerHasAccount.emit(player.accessCode != "null" && !player.accessCode.isNullOrEmpty()) }
            .map { it == UserRole.GOD.ordinal }
            .bind(_sharedDeleteVisibility, viewModelScope)

        flowOf(player.mid)
            .flatMapLatest { firebaseRepository.getUser(it) }
            .mapNotNull { it?.team == preferencesRepository.currentTeam?.mid }
            .bind(_sharedPlayerIsMember, viewModelScope)
    }

}