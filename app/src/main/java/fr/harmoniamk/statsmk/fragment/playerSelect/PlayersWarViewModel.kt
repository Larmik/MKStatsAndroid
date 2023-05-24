package fr.harmoniamk.statsmk.fragment.playerSelect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class PlayersWarViewModel@Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface): ViewModel() {

    private val _sharedPlayers = MutableSharedFlow<List<UserSelector>>()
    private val _sharedUsersSelected = MutableSharedFlow<List<User>>()
    private val _sharedOfficial = MutableSharedFlow<Boolean>()
    private val _sharedButtonEnabled = MutableSharedFlow<Boolean>()
    val sharedButtonEnabled = _sharedButtonEnabled.asSharedFlow()
    val sharedPlayers = _sharedPlayers.asSharedFlow()
    val sharedUsersSelected = _sharedUsersSelected.asSharedFlow()
    val sharedOfficial = _sharedOfficial.asSharedFlow()

    fun bind(onUserSelected: Flow<UserSelector>, onOfficialChecked: Flow<Boolean>) {
        val usersSelected = mutableListOf<User>()

        databaseRepository.getUsers()
            .map { list ->
                 val temp = mutableListOf<UserSelector>()
                temp.add(UserSelector(isCategory = true))
                temp.addAll(list.filter { user -> user.team == preferencesRepository.currentTeam?.mid }
                    .sortedBy { it.name?.toLowerCase(Locale.ROOT) }
                    .map { UserSelector(it, false) })
                temp.add(UserSelector(isCategory = true))
                temp.addAll(list.filterNot { user -> user.team == preferencesRepository.currentTeam?.mid }
                    .sortedBy { it.name?.toLowerCase(Locale.ROOT) }
                    .map { UserSelector(it, false) })
                temp
            }.bind(_sharedPlayers, viewModelScope)

        onUserSelected
            .onEach {
                if (it.isSelected.isTrue)
                    it.user?.let { user -> usersSelected.add(user) }
                else usersSelected.remove(it.user)
                _sharedUsersSelected.emit(usersSelected)
                _sharedButtonEnabled.emit(usersSelected.size == 6)
            }.launchIn(viewModelScope)

        onOfficialChecked.bind(_sharedOfficial, viewModelScope)
    }
}