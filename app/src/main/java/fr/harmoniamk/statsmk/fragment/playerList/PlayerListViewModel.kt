package fr.harmoniamk.statsmk.fragment.playerList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.fragment.settings.managePlayers.ManagePlayersItemViewModel
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@HiltViewModel
class PlayerListViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedPlayerList = MutableSharedFlow<List<ManagePlayersItemViewModel>>()
    private val _sharedAddPlayer = MutableSharedFlow<Unit>()
    private val _sharedEdit = MutableSharedFlow<User>()
    private val _sharedShowDialog = MutableSharedFlow<Boolean>()

    val sharedPlayerList = _sharedPlayerList.asSharedFlow()
    val sharedAddPlayer = _sharedAddPlayer.asSharedFlow()
    val sharedEdit = _sharedEdit.asSharedFlow()
    val sharedShowDialog = _sharedShowDialog.asSharedFlow()


    private val players = mutableListOf<ManagePlayersItemViewModel>()
    private val allPlayers = mutableListOf<ManagePlayersItemViewModel>()

    fun bind(onAdd: Flow<Unit>, onEdit: Flow<User>, onSearch: Flow<String>) {
        refresh()
        onAdd.bind(_sharedAddPlayer, viewModelScope)
        onEdit.onEach {
            _sharedEdit.emit(it)
            _sharedShowDialog.emit(true)
        }.launchIn(viewModelScope)
        onSearch.map { searched ->
            createPlayersList(modelList = allPlayers.filter { it.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)).isTrue })}
        .bind(_sharedPlayerList, viewModelScope)
    }

    private fun refresh() {
        allPlayers.clear()
        firebaseRepository.getUsers()
            .map {
                val role = authenticationRepository.userRole.firstOrNull() ?: 0
                it.filter { user -> user.team == "-1" }
                    .map { ManagePlayersItemViewModel(
                        it, false, preferencesRepository, authenticationRepository
                    ) }
                    .filter { !it.hasAccount || (it.hasAccount && role >= UserRole.LEADER.ordinal)}

            }
            .onEach { allPlayers.addAll(it) }
            .bind(_sharedPlayerList, viewModelScope)
    }

    fun bindAddDialog(onPlayedAdded: Flow<Unit>) {
        onPlayedAdded.onEach { refresh() }.launchIn(viewModelScope)
    }

    fun bindEditDialog(onDelete: Flow<User>, onPlayerEdited: Flow<User>, onTeamIntegrate: Flow<User>) {
        allPlayers.clear()
        onDelete
            .filter { it.mid != authenticationRepository.user?.uid }
            .flatMapLatest { firebaseRepository.deleteUser(it) }
            .onEach {
                _sharedShowDialog.emit(false)
                refresh()
            }.launchIn(viewModelScope)

        onPlayerEdited
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .onEach {
                _sharedShowDialog.emit(false)
                refresh()
            }.launchIn(viewModelScope)

        onTeamIntegrate
            .filter { it.mid != authenticationRepository.user?.uid }
            .map { it.apply { this.team = preferencesRepository.currentTeam?.mid } }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .onEach {
                _sharedShowDialog.emit(false)
                refresh()
            }.launchIn(viewModelScope)
    }

    private fun createPlayersList(list: List<User>? = null, modelList: List<ManagePlayersItemViewModel>? = null): List<ManagePlayersItemViewModel> {
        players.clear()
        players.add(ManagePlayersItemViewModel(isCategory = true))
        list?.let {
            players.addAll(list.map { ManagePlayersItemViewModel(player = it, preferencesRepository = preferencesRepository, authenticationRepository = authenticationRepository) }.filterNot { it.isAlly }.sortedBy { it.name })
            players.add(ManagePlayersItemViewModel(isCategory = true))
            players.addAll(list.map { ManagePlayersItemViewModel(player = it, preferencesRepository = preferencesRepository, authenticationRepository = authenticationRepository) }.filter { it.isAlly }.sortedBy { it.name })
        }
        modelList?.let {
            players.addAll(modelList.filterNot { it.isAlly }.sortedBy { it.name })
            players.add(ManagePlayersItemViewModel(isCategory = true))
            players.addAll(modelList.filter { it.isAlly }.sortedBy { it.name })
        }
        return players
    }

}