package fr.harmoniamk.statsmk.fragment.managePlayers

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
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
class ManagePlayersViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {

    private val _sharedPlayers = MutableSharedFlow<List<ManagePlayersItemViewModel>>()
    private val _sharedAddPlayer = MutableSharedFlow<Unit>()
    private val _sharedAddPlayerVisibility = MutableSharedFlow<Int>()
    private val _sharedEdit = MutableSharedFlow<User>()
    private val _sharedRedirectToSettings = MutableSharedFlow<Unit>()
    private val _sharedShowDialog = MutableSharedFlow<Boolean>()
    private val _sharedTeamName = MutableSharedFlow<String?>()
    private val _sharedTeamEdit = MutableSharedFlow<Team>()

    val sharedPlayers = _sharedPlayers.asSharedFlow()
    val sharedAddPlayer = _sharedAddPlayer.asSharedFlow()
    val sharedAddPlayerVisibility = _sharedAddPlayerVisibility.asSharedFlow()
    val sharedEdit = _sharedEdit.asSharedFlow()
    val sharedRedirectToSettings = _sharedRedirectToSettings.asSharedFlow()
    val sharedShowDialog = _sharedShowDialog.asSharedFlow()
    val sharedTeamName = _sharedTeamName.asSharedFlow()
    val sharedTeamEdit = _sharedTeamEdit.asSharedFlow()

    private val players = mutableListOf<ManagePlayersItemViewModel>()
    private val allPlayers = mutableListOf<ManagePlayersItemViewModel>()

    fun bind(onAdd: Flow<Unit>, onEdit: Flow<User>, onSearch: Flow<String>, onEditTeam: Flow<Unit>) {
        refresh()

        onEditTeam
            .mapNotNull { preferencesRepository.currentTeam }
            .bind(_sharedTeamEdit, viewModelScope)

        onAdd.bind(_sharedAddPlayer, viewModelScope)

        onEdit
            .onEach {
                _sharedEdit.emit(it)
                _sharedShowDialog.emit(true)
            }.launchIn(viewModelScope)

        onSearch
            .map { searched ->
                createPlayersList(modelList = allPlayers.filter { it.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)).isTrue })}
            .bind(_sharedPlayers, viewModelScope)
    }

    fun bindEditDialog(onDelete: Flow<User>, onPlayerEdited: Flow<User>, onTeamLeft: Flow<User>) {

        onDelete
            .filter { it.mid != authenticationRepository.user?.uid }
            .flatMapLatest { firebaseRepository.deleteUser(it) }
            .flatMapLatest {  firebaseRepository.getUsers() }
            .map { createPlayersList(list = it) }
            .filter { authenticationRepository.user != null }
            .onEach {
                _sharedShowDialog.emit(false)
                _sharedPlayers.emit(it)
            }.launchIn(viewModelScope)

        onPlayerEdited
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .flatMapLatest {  firebaseRepository.getUsers() }
            .map { createPlayersList(list = it) }
            .onEach {
                _sharedShowDialog.emit(false)
                _sharedPlayers.emit(it)
            }.launchIn(viewModelScope)

        onTeamLeft
            .filter { it.mid == authenticationRepository.user?.uid }
            .onEach { preferencesRepository.currentTeam = null }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .onEach { _sharedRedirectToSettings.emit(Unit) }
            .launchIn(viewModelScope)

        onTeamLeft
            .filter { it.mid != authenticationRepository.user?.uid }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .flatMapLatest {  firebaseRepository.getUsers() }
            .map { createPlayersList(list = it) }
            .onEach {
                _sharedShowDialog.emit(false)
                _sharedPlayers.emit(it)
            }.launchIn(viewModelScope)

    }

    fun bindAddDialog(onPlayedAdded: Flow<Unit>) {
        onPlayedAdded
            .flatMapLatest {  firebaseRepository.getUsers() }
            .map { createPlayersList(list = it) }
            .filter { authenticationRepository.user != null }
            .onEach { _sharedPlayers.emit(it) }
            .launchIn(viewModelScope)
    }

    fun bindEditTeamDialog(onTeamEdited: Flow<Team>) {
        onTeamEdited
            .onEach { preferencesRepository.currentTeam = it }
            .flatMapLatest { firebaseRepository.writeTeam(it) }
            .onEach { refresh() }
            .launchIn(viewModelScope)
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

    private fun refresh() {
        firebaseRepository.getUsers()
            .map { createPlayersList(list = it) }
            .onEach {
                allPlayers.addAll(it)
                _sharedTeamName.emit(preferencesRepository.currentTeam?.name)
            }.bind(_sharedPlayers, viewModelScope)

        authenticationRepository.userRole
            .mapNotNull { it >= UserRole.LEADER.ordinal }
            .mapNotNull {
                when (it) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
            }.bind(_sharedAddPlayerVisibility, viewModelScope)

    }
}