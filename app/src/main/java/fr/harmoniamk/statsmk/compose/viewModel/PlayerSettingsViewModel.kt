package fr.harmoniamk.statsmk.compose.viewModel

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
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
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
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class PlayerSettingsViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface
) : ViewModel() {

    private val _sharedPlayers =
        MutableStateFlow<SnapshotStateList<ManagePlayersItemViewModel>>(SnapshotStateList())
    private val _sharedPlayersWithoutAccount =
        MutableStateFlow<SnapshotStateList<ManagePlayersItemViewModel>>(SnapshotStateList())
    private val _sharedDismiss = MutableSharedFlow<Unit>()
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)
    private val _sharedAddPlayerVisibility = MutableStateFlow(false)

    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedPlayersWithoutAccount = _sharedPlayersWithoutAccount.asStateFlow()
    val sharedDismiss = _sharedDismiss.asSharedFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asStateFlow()
    val sharedAddPlayerVisibility = _sharedAddPlayerVisibility.asStateFlow()

    private val players = mutableListOf<User>()

    init {
        databaseRepository.getUsers()
            .onEach {
                players.clear()
                players.addAll(it)
            }
            .flatMapLatest {
                createPlayersList(it, true).zip(createPlayersList(it, false)) { reals, dummies ->
                    _sharedPlayers.value = reals
                    _sharedPlayersWithoutAccount.value = dummies
                }
            }
            .launchIn(viewModelScope)

        authenticationRepository.takeIf { preferencesRepository.currentTeam != null }
            ?.userRole
            ?.mapNotNull { it >= UserRole.ADMIN.ordinal && networkRepository.networkAvailable }
            ?.onEach { _sharedAddPlayerVisibility.value = it }
            ?.launchIn(viewModelScope)
    }

    fun onSearch(searched: String) {
        val list = when (searched.isNotEmpty()) {
            true -> players.filter { it.name?.lowercase()?.contains(searched.lowercase()).isTrue }
            else -> players
        }
        createPlayersList(list, true)
            .zip(createPlayersList(list, false)) { reals, dummies ->
                _sharedPlayers.value = reals
                _sharedPlayersWithoutAccount.value = dummies
            }.launchIn(viewModelScope)
    }

    fun onAddToTeam(player: User?, ally: Boolean) {
        when (ally) {
            true -> {
                val allyTeams = player?.allyTeams.orEmpty().toMutableList()
                allyTeams.add(preferencesRepository.currentTeam?.mid.orEmpty())
                player?.copy(allyTeams = allyTeams)?.let {
                    firebaseRepository.writeUser(it)
                        .bind(_sharedDismiss, viewModelScope)
                }
            }

            else -> player?.copy(team = preferencesRepository.currentTeam?.mid)?.let {
                firebaseRepository.writeUser(it)
                    .bind(_sharedDismiss, viewModelScope)
            }
        }

    }

    fun onCreatePlayer() {
        _sharedBottomSheetValue.value = MKBottomSheetState.CreatePlayer()
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
    }

    private fun createPlayersList(
        list: List<User>? = null,
        realPlayers: Boolean = true
    ): Flow<SnapshotStateList<ManagePlayersItemViewModel>> = flow {
        val newPlayers = SnapshotStateList<ManagePlayersItemViewModel>()
        list?.sortedBy { it.name }?.forEach { player ->
            authenticationRepository.userRole.map {
                val isUser = authenticationRepository.user?.uid == player.mid
                val hasAccount = player.mid.toLongOrNull() == null
                val isUserAdmin = it >= UserRole.ADMIN.ordinal
                networkRepository.networkAvailable
                        && !isUser
                        && !hasAccount
                        && isUserAdmin
                        && preferencesRepository.currentTeam != null

            }.onEach { canEdit ->
                player.takeIf {
                    it.team == "-1" && (
                            realPlayers && player.mid.toLongOrNull() == null
                                    || !realPlayers && player.mid.toLongOrNull() != null
                            )
                }?.let {
                    newPlayers.add(ManagePlayersItemViewModel(player = it, canEdit = canEdit))
                }
            }.launchIn(viewModelScope)
        }
        emit(newPlayers)
    }

    fun onEditPlayer(player: User) {
        _sharedBottomSheetValue.value = MKBottomSheetState.EditPlayer(player.mid)
    }

    private fun writeFormerTeams(user: User): Flow<User> = flow {
        val formerTeams = mutableListOf<String?>()
        formerTeams.addAll(user.formerTeams.orEmpty())
        formerTeams.add(preferencesRepository.currentTeam?.mid)
        user.formerTeams?.takeIf { it.isNotEmpty() }?.let {
            it.forEach {
                val wars = firebaseRepository.getNewWars(it)
                    .map { list -> list.map { MKWar(it) } }
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