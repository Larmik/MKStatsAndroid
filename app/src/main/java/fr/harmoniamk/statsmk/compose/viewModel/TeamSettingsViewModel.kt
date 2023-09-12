package fr.harmoniamk.statsmk.compose.viewModel

import android.net.Uri
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.local.ManagePlayersItemViewModel
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.PictureResponse
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.UploadPictureResponse
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.repository.StorageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class TeamSettingsViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val storageRepository: StorageRepository,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface
) : ViewModel() {


    private val _sharedPlayers = MutableStateFlow<SnapshotStateList<ManagePlayersItemViewModel>>(SnapshotStateList())
    private val _sharedRedirectToSettings = MutableSharedFlow<Unit>()
    private val _sharedTeamName = MutableStateFlow<String?>(null)
    private val _sharedManageVisible = MutableSharedFlow<Boolean>()
    private val _sharedPictureLoaded = MutableStateFlow<String?>(null)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)

    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedTeamName = _sharedTeamName.asStateFlow()
    val sharedPictureLoaded =_sharedPictureLoaded.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asSharedFlow()

    private val players = SnapshotStateList<ManagePlayersItemViewModel>()
    private val allPlayers = mutableListOf<ManagePlayersItemViewModel>()

    init {
        databaseRepository.getUsers()
            .onEach { delay(100) }
            .flatMapLatest { createPlayersList(list = it) }
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

    fun onEditTeam() {
        preferencesRepository.currentTeam?.mid?.let {
            _sharedBottomSheetValue.value = MKBottomSheetState.EditTeam(it)
        }
    }

    fun onAddPlayer() {
        _sharedBottomSheetValue.value = MKBottomSheetState.AddPlayer()
    }

    fun onEditPlayer(player: User) {
        _sharedBottomSheetValue.value = MKBottomSheetState.EditPlayer(player.mid)
    }

    fun onPictureEdited(pictureUri: Uri?) {
        pictureUri?.let { uri ->
            var url: String? = null
            storageRepository.uploadPicture(preferencesRepository.currentTeam?.mid, uri)
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
    }

    fun onSearch(searched: String) {

    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
        _sharedTeamName.value = preferencesRepository.currentTeam?.name
    }


    fun bindEditDialog(onPlayerEdited: Flow<User>, onTeamLeft: Flow<User>) {

        onPlayerEdited
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .flatMapLatest {  databaseRepository.getUsers() }
            .flatMapLatest { createPlayersList(list = it) }
            .bind(_sharedPlayers, viewModelScope)

        onTeamLeft
            .filter { it.mid == authenticationRepository.user?.uid }
            .flatMapLatest { writeFormerTeams(it) }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .onEach {
                preferencesRepository.currentTeam = null
                _sharedRedirectToSettings.emit(Unit)
            }.launchIn(viewModelScope)

        onTeamLeft
            .filter { it.mid != authenticationRepository.user?.uid }
            .flatMapLatest { writeFormerTeams(it) }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .flatMapLatest {  databaseRepository.getUsers() }
            .flatMapLatest { createPlayersList(list = it) }
            .bind(_sharedPlayers, viewModelScope)

    }

    fun onTeamEdited(team: Team) {
        firebaseRepository.writeTeam(team)
            .onEach { preferencesRepository.currentTeam = team }
            .launchIn(viewModelScope)
    }

    private fun createPlayersList(list: List<User>? = null): Flow<SnapshotStateList<ManagePlayersItemViewModel>> = flow {
        players.clear()
        list?.forEach { player ->
            authenticationRepository.userRole.map {
                authenticationRepository.user?.uid != player.mid
                        && networkRepository.networkAvailable
                        && ((player.mid.toLongOrNull() != null && it >= UserRole.ADMIN.ordinal)
                        || it >= UserRole.LEADER.ordinal)
            }.onEach { canEdit ->
                player.takeIf { it.team == preferencesRepository.currentTeam?.mid }?.let {
                    players.add(ManagePlayersItemViewModel(player = it, canEdit = canEdit))
                }
            }.launchIn(viewModelScope)
        }
        emit(players)
    }

    private fun writeFormerTeams(user: User): Flow<User> = flow {
        val formerTeams = mutableListOf<String?>()
        formerTeams.addAll(user.formerTeams.orEmpty())
        formerTeams.add(preferencesRepository.currentTeam?.mid)
        user.formerTeams?.takeIf { it.isNotEmpty() }?.let {
            it.forEach {
                val wars = firebaseRepository.getNewWars(it)
                    .map { list -> list.map {  MKWar(it) } }
                    .first()
                val finalList = wars.withName(databaseRepository).first()
                databaseRepository.writeWars(finalList).first()
            }
        }
        emit(user.apply {
            this.team = "-1"
            this.formerTeams = formerTeams.distinct().filterNotNull()
            this.role = UserRole.MEMBER.ordinal
        })
    }
}