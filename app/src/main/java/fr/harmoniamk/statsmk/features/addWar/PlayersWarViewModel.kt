package fr.harmoniamk.statsmk.features.addWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.User
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class PlayersWarViewModel@Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface): ViewModel() {

    private val _sharedPlayers = MutableSharedFlow<List<UserSelector>>()
    private val _sharedUsersSelected = MutableSharedFlow<List<User>>()

    val sharedPlayers = _sharedPlayers.asSharedFlow()
    val sharedUsersSelected = _sharedUsersSelected.asSharedFlow()

    fun bind(onUserSelected: Flow<UserSelector>) {
        val usersSelected = mutableListOf<User>()

        firebaseRepository.getUsers()
            .map { list -> list
                .filter { user -> user.team == preferencesRepository.currentTeam?.mid }
                .filter { user -> user.mid != preferencesRepository.currentUser?.mid }
                .sortedBy { it.name?.toLowerCase(Locale.ROOT) }
                .map { UserSelector(it, false) }
            }.bind(_sharedPlayers, viewModelScope)

        onUserSelected
            .onEach {
                if (it.isSelected) usersSelected.add(it.user)
                else usersSelected.remove(it.user)
                _sharedUsersSelected.emit(usersSelected)
            }.launchIn(viewModelScope)
    }
}