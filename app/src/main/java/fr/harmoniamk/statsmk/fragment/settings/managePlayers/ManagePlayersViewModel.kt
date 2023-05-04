package fr.harmoniamk.statsmk.fragment.settings.managePlayers

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.PictureResponse
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.UploadPictureResponse
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ManagePlayersViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val storageRepository: StorageRepository, private val authenticationRepository: AuthenticationRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface, private val networkRepository: NetworkRepositoryInterface) : ViewModel() {

    private val _sharedPlayers = MutableSharedFlow<List<ManagePlayersItemViewModel>>()
    private val _sharedAddPlayer = MutableSharedFlow<Unit>()
    private val _sharedAddPlayerVisibility = MutableSharedFlow<Int>()
    private val _sharedEdit = MutableSharedFlow<User>()
    private val _sharedRedirectToSettings = MutableSharedFlow<Unit>()
    private val _sharedTeamName = MutableSharedFlow<String?>()
    private val _sharedTeamEdit = MutableSharedFlow<Team>()
    private val _sharedEditPicture = MutableSharedFlow<Unit>()
    private val _sharedManageVisible = MutableSharedFlow<Boolean>()
    private val _sharedPictureLoaded = MutableSharedFlow<String?>()

    val sharedPlayers = _sharedPlayers.asSharedFlow()
    val sharedAddPlayer = _sharedAddPlayer.asSharedFlow()
    val sharedEditTeamVisibility = _sharedAddPlayerVisibility.asSharedFlow()
    val sharedEdit = _sharedEdit.asSharedFlow()
    val sharedRedirectToSettings = _sharedRedirectToSettings.asSharedFlow()
    val sharedTeamName = _sharedTeamName.asSharedFlow()
    val sharedTeamEdit = _sharedTeamEdit.asSharedFlow()
    val sharedEditPicture =_sharedEditPicture.asSharedFlow()
    val sharedPictureLoaded =_sharedPictureLoaded.asSharedFlow()
    val sharedManageVisible =_sharedManageVisible.asSharedFlow()

    private val players = mutableListOf<ManagePlayersItemViewModel>()
    private val allPlayers = mutableListOf<ManagePlayersItemViewModel>()

    fun bind(onPictureClick: Flow<Unit>, onPictureEdited: Flow<String>, onAdd: Flow<Unit>, onEdit: Flow<User>, onSearch: Flow<String>, onEditTeam: Flow<Unit>) {
        var url: String? = null

        refresh()
        onAdd.bind(_sharedAddPlayer, viewModelScope)
        onEdit.bind(_sharedEdit, viewModelScope)
        onEditTeam
            .mapNotNull { preferencesRepository.currentTeam }
            .bind(_sharedTeamEdit, viewModelScope)
        onSearch
            .map { searched ->
                createPlayersList(modelList = allPlayers.filter { it.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)).isTrue })}
            .bind(_sharedPlayers, viewModelScope)

        onPictureClick.bind(_sharedEditPicture, viewModelScope)


        onPictureEdited
            .flatMapLatest { storageRepository.uploadPicture(preferencesRepository.currentTeam?.mid, Uri.parse(it)) }
            .filter { it is UploadPictureResponse.Success }
            .mapNotNull { preferencesRepository.currentTeam?.mid }
            .flatMapLatest { storageRepository.getPicture(it) }
            .mapNotNull { url = (it as? PictureResponse.Success)?.url; url }
            .onEach { preferencesRepository.currentTeam = preferencesRepository.currentTeam?.apply { this.picture = url } }
            .mapNotNull { preferencesRepository.currentTeam }
            .flatMapLatest { firebaseRepository.writeTeam(it) }
            .onEach { _sharedPictureLoaded.emit(url) }
            .launchIn(viewModelScope)
    }

    fun bindEditDialog(onDelete: Flow<User>, onPlayerEdited: Flow<User>, onTeamLeft: Flow<User>) {

        onDelete
            .filter { it.mid != authenticationRepository.user?.uid }
            .flatMapLatest { firebaseRepository.deleteUser(it) }
            .flatMapLatest {  databaseRepository.getUsers() }
            .map { createPlayersList(list = it) }
            .filter { authenticationRepository.user != null }
            .bind(_sharedPlayers, viewModelScope)

        onPlayerEdited
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .flatMapLatest {  databaseRepository.getUsers() }
            .map { createPlayersList(list = it) }
            .bind(_sharedPlayers, viewModelScope)

        onTeamLeft
            .filter { it.mid == authenticationRepository.user?.uid }
            .onEach { preferencesRepository.currentTeam = null }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .onEach { _sharedRedirectToSettings.emit(Unit) }
            .launchIn(viewModelScope)

        onTeamLeft
            .filter { it.mid != authenticationRepository.user?.uid }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .flatMapLatest {  databaseRepository.getUsers() }
            .map { createPlayersList(list = it) }
            .bind(_sharedPlayers, viewModelScope)

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
        players.add(ManagePlayersItemViewModel(isCategory = true, isConnected = networkRepository.networkAvailable))
        list?.let {
            players.addAll(list.map { ManagePlayersItemViewModel(player = it, preferencesRepository = preferencesRepository, authenticationRepository = authenticationRepository, isConnected = networkRepository.networkAvailable) }.filterNot { it.isAlly }.sortedBy { it.name })
            players.add(ManagePlayersItemViewModel(isCategory = true, isConnected = networkRepository.networkAvailable))
            players.addAll(list.map { ManagePlayersItemViewModel(player = it, preferencesRepository = preferencesRepository, authenticationRepository = authenticationRepository, isConnected = networkRepository.networkAvailable) }.filter { it.isAlly }.sortedBy { it.name })
        }
        modelList?.let {
            players.addAll(modelList.filterNot { it.isAlly }.sortedBy { it.name })
            players.add(ManagePlayersItemViewModel(isCategory = true, isConnected = networkRepository.networkAvailable))
            players.addAll(modelList.filter { it.isAlly }.sortedBy { it.name })
        }
        return players
    }

    private fun refresh() {
        databaseRepository.getUsers()
            .map { createPlayersList(list = it) }
            .onEach {
                allPlayers.addAll(it)
                _sharedTeamName.emit(preferencesRepository.currentTeam?.name)
                _sharedPlayers.emit(it)
            }
            .flatMapLatest { storageRepository.getPicture(preferencesRepository.currentTeam?.mid) }
            .onEach { _sharedPictureLoaded.emit((it as? PictureResponse.Success)?.url) }
            .launchIn(viewModelScope)

        authenticationRepository.userRole
            .mapNotNull { it >= UserRole.LEADER.ordinal && networkRepository.networkAvailable }
           .bind(_sharedManageVisible, viewModelScope)

    }
}