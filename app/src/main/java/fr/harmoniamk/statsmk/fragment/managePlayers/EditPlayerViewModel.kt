package fr.harmoniamk.statsmk.fragment.managePlayers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _sharedIsAdmin = MutableSharedFlow<Boolean>()
    private val _sharedIsMember = MutableSharedFlow<Boolean>()
    val sharedIsAdmin = _sharedIsAdmin.asSharedFlow()
    val sharedIsMember = _sharedIsMember.asSharedFlow()

    fun bind(player: User) {
        authenticationRepository.isAdmin.bind(_sharedIsAdmin, viewModelScope)
        flowOf(player.mid)
            .flatMapLatest { firebaseRepository.getUser(it) }
            .mapNotNull { it?.team == preferencesRepository.currentTeam?.mid }
            .bind(_sharedIsMember, viewModelScope)
    }

}