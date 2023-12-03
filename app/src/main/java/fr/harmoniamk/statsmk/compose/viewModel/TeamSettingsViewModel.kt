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
import fr.harmoniamk.statsmk.extension.isTrue
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
    private val _sharedAllies = MutableStateFlow<SnapshotStateList<ManagePlayersItemViewModel>>(SnapshotStateList())
    private val _sharedTeamName = MutableStateFlow<String?>(null)
    private val _sharedPictureLoaded = MutableStateFlow<String?>(null)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)
    private val _sharedManageVisible = MutableStateFlow(false)

    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedAllies = _sharedAllies.asStateFlow()
    val sharedTeamName = _sharedTeamName.asStateFlow()
    val sharedPictureLoaded =_sharedPictureLoaded.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asSharedFlow()
    val sharedManageVisible = _sharedManageVisible.asStateFlow()

    private val players = SnapshotStateList<ManagePlayersItemViewModel>()
    private val allys = SnapshotStateList<ManagePlayersItemViewModel>()
    private val allPlayers = mutableListOf<User>()

    init {
        databaseRepository.getUsers()
            .onEach {
                _sharedTeamName.emit(preferencesRepository.currentTeam?.name)
                allPlayers.clear()
                allPlayers.addAll(it)
                _sharedPlayers.emit(createPlayersList(list = it).first())
                _sharedAllies.emit(createAllyList(list = it).first())
            }
            .flatMapLatest { storageRepository.getPicture(preferencesRepository.currentTeam?.mid) }
            .onEach { _sharedPictureLoaded.emit((it as? PictureResponse.Success)?.url) }
            .launchIn(viewModelScope)

        authenticationRepository.userRole
            .mapNotNull { it >= UserRole.LEADER.ordinal && networkRepository.networkAvailable }
            .onEach { _sharedManageVisible.value = it }
            .launchIn(viewModelScope)
    }

    fun onEditTeam() {
        preferencesRepository.currentTeam?.mid?.let {
            _sharedBottomSheetValue.value = MKBottomSheetState.EditTeam(it)
        }
    }

    fun onAddPlayer(ally: Boolean) {
        _sharedBottomSheetValue.value = MKBottomSheetState.AddPlayer(ally)
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
        createPlayersList(
            when (searched.isNotEmpty()) {
                true -> allPlayers.filter { it.name?.lowercase()?.contains(searched.lowercase()).isTrue }
                else -> allPlayers
            }
        ).onEach {
            _sharedPlayers.value = it
        }.launchIn(viewModelScope)
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
        _sharedTeamName.value = preferencesRepository.currentTeam?.name
    }



    private fun createPlayersList(list: List<User>? = null): Flow<SnapshotStateList<ManagePlayersItemViewModel>> = flow {
        players.clear()
        list?.sortedBy { it.name }?.forEach { player ->
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
    private fun createAllyList(list: List<User>? = null): Flow<SnapshotStateList<ManagePlayersItemViewModel>> = flow {
        allys.clear()
        list?.sortedBy { it.name }?.forEach { player ->
            authenticationRepository.userRole.map {
                authenticationRepository.user?.uid != player.mid
                        && networkRepository.networkAvailable
                        && ((player.mid.toLongOrNull() != null && it >= UserRole.ADMIN.ordinal)
                        || it >= UserRole.LEADER.ordinal)
            }.onEach { canEdit ->
                player.takeIf { it.allyTeams?.contains(preferencesRepository.currentTeam?.mid.orEmpty()).isTrue }?.let {
                    allys.add(ManagePlayersItemViewModel(player = it, canEdit = canEdit))
                }
            }.launchIn(viewModelScope)
        }
        emit(allys)
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