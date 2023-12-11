package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.ManagePlayersItemViewModel
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val mkCentralRepository: MKCentralRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface
) : ViewModel() {

    private val _sharedPlayers = MutableStateFlow<List<MKCLightPlayer>>(listOf())
    private val _sharedAllies = MutableStateFlow<SnapshotStateList<ManagePlayersItemViewModel>>(SnapshotStateList())
    private val _sharedTeam = MutableStateFlow<MKCFullTeam?>(null)
    private val _sharedPictureLoaded = MutableStateFlow<String?>(null)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)
    private val _sharedManageVisible = MutableStateFlow(false)

    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedAllies = _sharedAllies.asStateFlow()
    val sharedTeam = _sharedTeam.asStateFlow()
    val sharedPictureLoaded =_sharedPictureLoaded.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asSharedFlow()
    val sharedManageVisible = _sharedManageVisible.asStateFlow()

    private val allys = SnapshotStateList<ManagePlayersItemViewModel>()

    fun init() {
        mkCentralRepository.getTeam("874")
            .onEach {
                _sharedTeam.emit(it)
                _sharedPictureLoaded.emit(it.logoUrl)
                _sharedPlayers.emit(it.rosterList.orEmpty())
            }.launchIn(viewModelScope)

        databaseRepository.getUsers()
            .onEach { _sharedAllies.emit(createAllyList(list = it).first()) }
            .launchIn(viewModelScope)

    }

    fun onAddPlayer(ally: Boolean) {
        _sharedBottomSheetValue.value = MKBottomSheetState.AddPlayer(ally)
    }

    fun onEditPlayer(player: User) {
        _sharedBottomSheetValue.value = MKBottomSheetState.EditPlayer(player.mkcId.orEmpty())
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
    }


    private fun createAllyList(list: List<User>? = null): Flow<SnapshotStateList<ManagePlayersItemViewModel>> = flow {
        allys.clear()
        list?.sortedBy { it.name }?.forEach { player ->
            authenticationRepository.userRole.map {
                preferencesRepository.mkcPlayer?.id.toString() != player.mkcId
                        && networkRepository.networkAvailable
                        && player.mid.toLongOrNull() != null
                        && ((player.mid.toLongOrNull() != null && it >= UserRole.ADMIN.ordinal)
                        || it >= UserRole.LEADER.ordinal)
            }.onEach { canEdit ->
                player.takeIf { it.allyTeams?.contains(preferencesRepository.mkcTeam?.id.orEmpty()).isTrue }?.let {
                    allys.add(ManagePlayersItemViewModel(player = it, canEdit = canEdit))
                }
            }.launchIn(viewModelScope)
        }
        emit(allys)
    }

}